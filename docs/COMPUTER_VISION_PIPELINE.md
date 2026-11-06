# NAGARLENS — Computer Vision Processing Pipeline

> **Document Version:** 1.0.0  
> **Status:** Production Pipeline Architecture  

---

## 1. End-to-End Processing Flow

```text
  Raw Photo / Video Frame
             │
             ▼
  ┌────────────────────────┐
  │  ImageInputValidator   │  ──► Validates header, file size (<50MB), 25MP limit
  └──────────┬─────────────┘
             ▼
  ┌────────────────────────┐
  │   ImagePreprocessor    │  ──► EXIF transpose, RGB convert, 64-bit dHash, letterbox resize
  └──────────┬─────────────┘
             ▼
  ┌────────────────────────┐
  │ VisionInferenceEngine  │  ──► Model inference dispatch (ONNX / LiteRT / PyTorch)
  └──────────┬─────────────┘
             ▼
  ┌────────────────────────┐
  │      NmsProcessor      │  ──► Non-Maximum Suppression (IoU < 0.45)
  └──────────┬─────────────┘
             ▼
  ┌────────────────────────┐
  │ SeverityAssessmentEng. │  ──► 2D area coverage & category urgency severity index (0.0-1.0)
  └──────────┬─────────────┘
             ▼
  ┌────────────────────────┐
  │  AiAssessment Contract │  ──► JSON DTO persisted in database & rendered in Compose UI
  └────────────────────────┘
```

---

## 2. On-Device Edge Execution vs. Server Processing

| Execution Environment | Hardware / Engine | Use Case | Latency Target |
| :--- | :--- | :--- | :--- |
| **Android Edge Client** | ONNX Runtime Android SDK / LiteRT | Immediate field preview & preliminary defect detection during evidence capture. | <150 ms |
| **Server Backend Platform** | FastAPI PyTorch ONNX Engine | Full batch verification, historical change detection, and high-resolution audit analysis. | <500 ms |
