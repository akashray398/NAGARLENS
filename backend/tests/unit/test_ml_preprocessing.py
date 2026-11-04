"""NAGARLENS — Unit Tests for Hardened Image Preprocessing Pipeline."""

import io
from PIL import Image
from app.ml.preprocessing.pipeline import ImagePreprocessor, MAX_INPUT_BYTES


def test_preprocessor_handles_corrupt_bytes():
    preprocessor = ImagePreprocessor()
    corrupt_bytes = b"NOT_A_REAL_IMAGE_BINARY"

    result = preprocessor.process_image_bytes(corrupt_bytes)
    assert not result.is_valid
    assert result.quality_status == "FAIL_CORRUPTED"
    assert result.processed_image_bytes is None


def test_preprocessor_handles_oversized_byte_input():
    preprocessor = ImagePreprocessor()
    oversized_bytes = b"0" * (MAX_INPUT_BYTES + 1)

    result = preprocessor.process_image_bytes(oversized_bytes)
    assert not result.is_valid
    assert result.quality_status == "FAIL_OVERSIZED_FILE"


def test_preprocessor_handles_low_resolution():
    preprocessor = ImagePreprocessor(min_size=(224, 224))

    # Create 100x100 tiny image
    img = Image.new("RGB", (100, 100), color="red")
    buf = io.BytesIO()
    img.save(buf, format="JPEG")
    tiny_bytes = buf.getvalue()

    result = preprocessor.process_image_bytes(tiny_bytes)
    assert not result.is_valid
    assert result.quality_status == "FAIL_LOW_RESOLUTION"


def test_preprocessor_converts_rgba_and_grayscale_to_rgb():
    preprocessor = ImagePreprocessor(target_size=(224, 224), min_size=(224, 224))

    # Create RGBA image with transparency
    rgba_img = Image.new("RGBA", (300, 300), color=(255, 0, 0, 128))
    buf = io.BytesIO()
    rgba_img.save(buf, format="PNG")
    rgba_bytes = buf.getvalue()

    result = preprocessor.process_image_bytes(rgba_bytes)
    assert result.is_valid
    assert result.quality_status == "PASS"

    # Verify processed image is RGB and 224x224
    processed_img = Image.open(io.BytesIO(result.processed_image_bytes))
    assert processed_img.mode == "RGB"
    assert processed_img.size == (224, 224)


def test_preprocessor_letterbox_resizes_valid_image_and_computes_dhash():
    preprocessor = ImagePreprocessor(target_size=(224, 224), min_size=(224, 224))

    # Create valid 640x480 image
    img = Image.new("RGB", (640, 480), color="blue")
    buf = io.BytesIO()
    img.save(buf, format="JPEG")
    valid_bytes = buf.getvalue()

    result = preprocessor.process_image_bytes(valid_bytes)
    assert result.is_valid
    assert result.quality_status == "PASS"
    assert result.processed_image_bytes is not None
    assert len(result.dhash) == 16  # 64-bit dhash in 16 hex chars

    # Verify processed image is exactly 224x224
    processed_img = Image.open(io.BytesIO(result.processed_image_bytes))
    assert processed_img.size == (224, 224)
