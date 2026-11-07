# NAGARLENS — Action Execution & Bounded Retry Policy

> **Document Version:** 1.0.0  
> **Status:** Production Specification  

---

## 1. Action Execution Safety

- **Idempotency Keys**: Action executions generate `execution_id` (`EXEC-<timestamp>`).
- **Bounded Retries**: Max 3 attempts with exponential backoff before failing safely.
