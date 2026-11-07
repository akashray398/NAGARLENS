# NAGARLENS — Anomaly Detection Engine Specification

> **Document Version:** 1.0.0  
> **Status:** Production Specification  

---

## 1. Z-Score Statistical Anomaly Formulation

$$Z = \frac{S_{\text{latest}} - \mu_{\text{baseline}}}{\sigma_{\text{baseline}}}$$

Where:
- $\mu_{\text{baseline}}$ = Mean severity score of historical observations excluding latest
- $\sigma_{\text{baseline}}$ = Standard deviation of historical severity scores
- Anomaly flagged when $Z \ge 2.0$ (Sudden Severity Spike).
