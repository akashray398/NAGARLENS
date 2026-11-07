# NAGARLENS — Investigation Lifecycle & State Transitions

> **Document Version:** 1.0.0  
> **Status:** Active State Machine Specification  

---

## 1. Lifecycle State Machine Matrix

```text
  [DRAFT] ──> [OPEN] ──> [TRIAGED] ──> [INVESTIGATING] ──┬──> [AWAITING_EVIDENCE]
                                         │              │
  [CLOSED] <── [RESOLVED] <── [RESOLUTION_REVIEW]       ├──> [AWAITING_REVIEW]
      │             ▲                    │              │
      └── [REOPENED]┴────────────────────┴── [ACTION_PROPOSED] ──> [AWAITING_APPROVAL] ──> [ACTION_IN_PROGRESS]
```

### Invalid Transitions Protection:
- Transitions attempting to bypass mandatory states (e.g. `TRIAGED` directly to `RESOLVED`) are rejected with `HTTP 422 Unprocessable Content`.
