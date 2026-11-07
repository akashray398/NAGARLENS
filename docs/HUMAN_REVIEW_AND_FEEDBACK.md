# NAGARLENS — Human Review & Feedback Loop Specification

> **Document Version:** 1.0.0  
> **Status:** Production Specification  

---

## 1. Human Feedback Workflow

- Authorized municipal inspectors can review AI predictions and submit feedback:
  - `CONFIRMED`: Verified defect prediction and severity.
  - `REJECTED_FALSE_POSITIVE`: Flagged false positive detection.
  - `SEVERITY_ADJUSTED`: Manually adjusted severity index.
- Feedback records are persisted with reviewer ID, notes, and timestamp for future model fine-tuning without overwriting original AI outputs.
