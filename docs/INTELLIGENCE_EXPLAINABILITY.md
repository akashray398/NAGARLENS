# NAGARLENS — Intelligence Explainability Specification

> **Document Version:** 1.0.0  
> **Status:** Production Specification  

---

## 1. Explainability Standard Structure

Every generated early warning alert contains:
1. **Summary Explanation**: Concise summary of risk classification and primary defect.
2. **Contributing Factors**: Bulleted list detailing exact factors (recurrence count, severity %, trend slope, incidents, spatial buffer).
3. **Recommended Action**: Actionable, prioritized maintenance recommendation.
4. **Evidence Provenance**: Immutable references to supporting evidence SHA-256 hashes.
