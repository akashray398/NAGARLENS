# NAGARLENS — AI Model Security & Decompression Protection

> **Document Version:** 1.0.0  
> **Status:** Production Specification  

---

## 1. Input Image Safety Controls

- **Decompression Bomb Protection**: `Image.MAX_IMAGE_PIXELS = 25_000_000` (Max 25 Megapixels).
- **Format Verification**: `image.verify()` verifies image header integrity before processing.
