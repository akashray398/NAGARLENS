# NAGARLENS — Dataset Architecture Specification

> **Dataset Schema Version:** `v1.0.0`  
> **Target Storage:** S3 / App-Private Local Ingestion Engine  

---

## 1. Dataset Design Principles

The NAGARLENS Dataset Architecture enforces strict reproducibility, data provenance, and leakage prevention:
1. **Zero Data Leakage:** Images captured during the same inspection session, location, or incident must remain in the same split (Train, Validation, or Test) to prevent data leakage between training and evaluation.
2. **Immutability & Provenance:** Every image record references the original SHA-256 evidence hash digest.
3. **Quality Gate Filtering:** Unusable images (blurry, dark, low resolution <224x224) are flagged and excluded from training splits.

---

## 2. Dataset Split Ratio & Strategy

- **Training Split (70%):** Model parameter optimization.
- **Validation Split (15%):** Hyperparameter tuning, early stopping, threshold selection.
- **Testing Split (15%):** Final benchmark reporting. Never used for hyperparameter tuning.

Splits are generated using **Incident-Grouped Partitioning** based on `location_group_id` / `incident_id`.

---

## 3. Annotation Guidelines & Sourcing

Field teams acquire drainage asset imagery during routine municipal audits. Images are annotated using standard bounding box coordinates normalized to `[0.0, 1.0]`:
- `x_min`, `y_min`, `x_max`, `y_max`

Annotations must be verified by a secondary human auditor before being marked `VERIFIED`.
