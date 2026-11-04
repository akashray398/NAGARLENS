# NAGARLENS — AI Assessment Contract Specification

> **Contract Version:** `v1.0.0`  
> **Schema Standard:** JSON Schema & Pydantic v2 / Kotlin Data Class  

---

## 1. Contract Overview

The AI Assessment Contract provides a stable, model-independent JSON interface representing AI predictions generated from evidence imagery.

### Key Rules:
1. **Decoupling from Physical Evidence:** An `AiAssessment` is an analytical prediction; it **never modifies** the original `Evidence` record or human-verified asset facts.
2. **Uncertainty Representation:** Unclear images or missing model binaries explicitly set `processingStatus` to `MODEL_UNAVAILABLE` or `INSUFFICIENT_EVIDENCE` with a confidence score of `0.0`.

---

## 2. JSON Schema Example

```json
{
  "assessment_id": "8f3b2d10-4e5a-4b2c-9a1b-3c4d5e6f7a8b",
  "evidence_id": "1a2b3c4d-5e6f-7a8b-9c0d-1e2f3a4b5c6d",
  "model_name": "MobileNetV3-Small-DrainDefect",
  "model_version": "v1.0.0",
  "taxonomy_version": "v1.0.0",
  "predicted_category": "SOLID_WASTE",
  "confidence_score": 0.942,
  "bounding_boxes": [
    {
      "category": "SOLID_WASTE",
      "confidence": 0.942,
      "x_min": 0.25,
      "y_min": 0.30,
      "x_max": 0.65,
      "y_max": 0.75
    }
  ],
  "image_quality_status": "PASS",
  "processing_status": "COMPLETED",
  "message": "Defect classification completed successfully.",
  "inference_timestamp_utc": 1711987200000
}
```
