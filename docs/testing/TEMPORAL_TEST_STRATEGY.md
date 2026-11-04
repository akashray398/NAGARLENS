# NAGARLENS — Temporal Test Strategy Specification

> **Testing Scope:** Backend Pytest & Android Room v3 Migration Test Suites  

---

## 1. Test Coverage Goals

- **Append-Only Memory Integrity:** Verify historical observations are never overwritten or deleted.
- **Late-Arriving Offline Timestamps:** Verify `observedAtUtc` retains field capture timestamp regardless of network upload time.
- **Intervention & Outcome Linking:** Verify outcome verification requires valid intervention and verification observation IDs.
- **Room Migration 2 -> 3:** Test that Room v2 assets and evidence records survive migration `2 -> 3` intact.
