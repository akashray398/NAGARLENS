"""NAGARLENS — AI Privacy Blurring Pipeline for PII Redaction."""

import io
from PIL import Image, ImageFilter


class PrivacyBlurProcessor:
    """Redacts sensitive PII (faces, vehicle registration plates) from evidence photos."""

    def __init__(self, blur_radius: int = 15):
        self.blur_radius = blur_radius

    def apply_privacy_blur(self, image_bytes: bytes, bounding_boxes: list[dict] = None) -> bytes:
        if not image_bytes:
            return image_bytes

        try:
            image = Image.open(io.BytesIO(image_bytes)).convert("RGB")
        except Exception:
            return image_bytes

        if not bounding_boxes:
            # Return original if no privacy-sensitive bounding boxes identified
            return image_bytes

        width, height = image.size
        blurred_image = image.copy()

        for box in bounding_boxes:
            category = box.get("category", "")
            if category in ("PERSON_FACE", "VEHICLE_LICENSE_PLATE"):
                x_min = int(box.get("x_min", 0) * width)
                y_min = int(box.get("y_min", 0) * height)
                x_max = int(box.get("x_max", 1) * width)
                y_max = int(box.get("y_max", 1) * height)

                box_region = image.crop((x_min, y_min, x_max, y_max))
                blurred_region = box_region.filter(ImageFilter.GaussianBlur(radius=self.blur_radius))
                blurred_image.paste(blurred_region, (x_min, y_min))

        output_buffer = io.BytesIO()
        blurred_image.save(output_buffer, format="JPEG", quality=90)
        return output_buffer.getvalue()


privacy_blur_processor = PrivacyBlurProcessor()
