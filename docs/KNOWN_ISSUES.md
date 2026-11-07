# NAGARLENS — Known Issues & Residual Risk Register

> **Document Version:** 1.0.0  
> **Status:** Active Inventory  

---

## 1. Residual Risk Matrix

| Risk ID | Category | Severity | Description | Mitigation Strategy |
| :--- | :--- | :--- | :--- | :--- |
| **RR-01** | AI Vision | **MEDIUM** | Model binary weights (`.onnx`/`.pt`) are pending final training on municipal datasets. Returns explicit `MODEL_UNAVAILABLE` contract when weights are absent. | Deploy trained ONNX/PyTorch model artifacts during Master Prompt 10 launch. |
| **RR-02** | Hardware | **LOW** | Low-cost Android physical devices may experience GPS jitter under dense urban tree canopy. | Display GNSS accuracy radius (meters) and require manual map-pin verification. |
