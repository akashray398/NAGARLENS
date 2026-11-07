# NAGARLENS — Synchronization & Conflict Resolution Architecture

> **Document Version:** 1.0.0  
> **Status:** Production Sync Specification  

---

## 1. Idempotency & Conflict Resolution Rules

1. **Client-Side UUID Generation**: Observations and evidence records are assigned a client-generated UUIDv4 upon field capture.
2. **Idempotent Retry Handling**: Re-submitting an existing UUID is processed idempotently by checking existing record hashes.
3. **Timestamp Ordering**: Immutable append-only observation logs ensure historical evidence is never overwritten. Updates create new observation event nodes.
4. **Stale Recovery**: `recoverStaleUploadingRecords` recovers stale `UPLOADING` state after 30 seconds of network interruption.
