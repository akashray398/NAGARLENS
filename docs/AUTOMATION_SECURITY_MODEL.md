# NAGARLENS — Automation Security & Authorization Model

> **Document Version:** 1.0.0  
> **Status:** Production Security Specification  

---

## 1. Security & Privilege Boundaries

- Client-side code cannot bypass approval requirements.
- Backend verifies JWT bearer token role claims before processing approval submissions or action executions.
