# NAGARLENS — Controlled Automation Engine Architecture

> **Document Version:** 1.0.0  
> **Status:** Production Specification  

---

## 1. Controlled Automation Principles

1. **No Autonomous Consequential Side-Effects**: High-impact actions (dispatching maintenance crews, changing municipal status, closing investigations) REQUIRE explicit prior human approval.
2. **Deterministic Execution**: Triggers evaluate typed conditions; duplicate trigger execution is blocked via idempotency keys.
