# NAGARLENS — Infrastructure Risk Model Specification

> **Document Version:** 1.0.0  
> **Status:** Production Specification  

---

## 1. Risk Classification Bands

| Risk Band | Score Range | Operational Meaning | Recommended Response Window |
| :--- | :--- | :--- | :--- |
| **INFORMATIONAL** | `0.00 - 0.24` | Baseline asset condition; normal operation. | Routine monthly monitoring |
| **LOW** | `0.25 - 0.44` | Minor defect observed; no hydraulic impact. | Inspect within 14 days |
| **MODERATE** | `0.45 - 0.64` | Moderate accumulation or structural wear. | Add to weekly maintenance queue |
| **HIGH** | `0.65 - 0.79` | High blockage or structural damage; risk of local failure. | Dispatch work order within 48 hours |
| **CRITICAL** | `0.80 - 1.00` | Severe blockage, cover missing, or severe overflow. | Immediate dispatch within 12 hours |
