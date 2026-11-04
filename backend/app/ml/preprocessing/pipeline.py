"""NAGARLENS — Modular Image Quality & Hardened Preprocessing Pipeline."""

import io
from typing import Optional, Tuple
from PIL import Image, ImageOps

# Protect against decompression bomb denial of service attacks (Max 25 Megapixels)
Image.MAX_IMAGE_PIXELS = 25_000_000

MAX_INPUT_BYTES = 50 * 1024 * 1024  # 50 MB limit


class PreprocessingResult:

    def __init__(
        self,
        is_valid: bool,
        quality_status: str,
        processed_image_bytes: Optional[bytes] = None,
        dhash: Optional[str] = None,
        error_message: Optional[str] = None,
    ):
        self.is_valid = is_valid
        self.quality_status = quality_status
        self.processed_image_bytes = processed_image_bytes
        self.dhash = dhash
        self.error_message = error_message


class ImagePreprocessor:
    """Modular, Hardened Image Preprocessing Engine for Drainage Defect Analysis."""

    def __init__(
        self,
        target_size: Tuple[int, int] = (224, 224),
        min_size: Tuple[int, int] = (224, 224),
        dhash_distance_threshold: int = 10,
    ):
        self.target_width, self.target_height = target_size
        self.min_width, self.min_height = min_size
        self.dhash_distance_threshold = dhash_distance_threshold

    def compute_dhash(self, image: Image.Image, hash_size: int = 8) -> str:
        """Compute 64-bit Difference Hash (dHash) for duplicate detection."""
        # Convert to grayscale and resize to (hash_size + 1, hash_size)
        resized = image.convert("L").resize((hash_size + 1, hash_size), Image.Resampling.LANCZOS)
        difference = []
        for row in range(hash_size):
            for col in range(hash_size):
                pixel_left = resized.getpixel((col, row))
                pixel_right = resized.getpixel((col + 1, row))
                difference.append(pixel_left > pixel_right)

        decimal_value = 0
        hex_string = []
        for index, value in enumerate(difference):
            if value:
                decimal_value += 2 ** (index % 8)
            if (index % 8) == 7:
                hex_string.append(f"{decimal_value:02x}")
                decimal_value = 0
        return "".join(hex_string)

    def process_image_bytes(self, raw_bytes: bytes) -> PreprocessingResult:
        if not raw_bytes or len(raw_bytes) == 0:
            return PreprocessingResult(
                is_valid=False,
                quality_status="FAIL_EMPTY_FILE",
                error_message="Image file bytes are empty.",
            )

        if len(raw_bytes) > MAX_INPUT_BYTES:
            return PreprocessingResult(
                is_valid=False,
                quality_status="FAIL_OVERSIZED_FILE",
                error_message=f"Image byte size ({len(raw_bytes)} bytes) exceeds maximum limit ({MAX_INPUT_BYTES} bytes).",
            )

        try:
            image = Image.open(io.BytesIO(raw_bytes))
            image.verify()  # Verify header integrity

            # Re-open after verify()
            image = Image.open(io.BytesIO(raw_bytes))
        except Exception as exc:
            return PreprocessingResult(
                is_valid=False,
                quality_status="FAIL_CORRUPTED",
                error_message=f"Image decoding failed or corrupted: {str(exc)}",
            )

        # Handle EXIF orientation tags correctly
        try:
            image = ImageOps.exif_transpose(image)
        except Exception:
            pass  # Fall back to un-oriented image if EXIF parsing fails

        # Check resolution
        width, height = image.size
        if width < self.min_width or height < self.min_height:
            return PreprocessingResult(
                is_valid=False,
                quality_status="FAIL_LOW_RESOLUTION",
                error_message=f"Resolution {width}x{height} below minimum required {self.min_width}x{self.min_height}.",
            )

        # Convert RGBA / P / L / CMYK modes to RGB cleanly
        if image.mode != "RGB":
            if image.mode in ("RGBA", "LA") or (image.mode == "P" and "transparency" in image.info):
                # Composite over black background to preserve transparent regions
                background = Image.new("RGB", image.size, (0, 0, 0))
                background.paste(image, mask=image.split()[-1])
                image = background
            else:
                image = image.convert("RGB")

        # Compute dHash on decoded and oriented image
        dhash_str = self.compute_dhash(image)

        # Letterbox Resize (Aspect-Ratio Preserving with Black Padding)
        padded_image = self.letterbox_resize(image, (self.target_width, self.target_height))

        # Save to processed JPEG bytes
        output_buffer = io.BytesIO()
        padded_image.save(output_buffer, format="JPEG", quality=92)
        processed_bytes = output_buffer.getvalue()

        return PreprocessingResult(
            is_valid=True,
            quality_status="PASS",
            processed_image_bytes=processed_bytes,
            dhash=dhash_str,
            error_message=None,
        )

    def letterbox_resize(self, image: Image.Image, target_size: Tuple[int, int]) -> Image.Image:
        target_w, target_h = target_size
        orig_w, orig_h = image.size

        scale = min(target_w / orig_w, target_h / orig_h)
        new_w = max(1, int(orig_w * scale))
        new_h = max(1, int(orig_h * scale))

        resized = image.resize((new_w, new_h), Image.Resampling.LANCZOS)
        padded = Image.new("RGB", (target_w, target_h), (0, 0, 0))

        paste_x = (target_w - new_w) // 2
        paste_y = (target_h - new_h) // 2
        padded.paste(resized, (paste_x, paste_y))

        return padded
