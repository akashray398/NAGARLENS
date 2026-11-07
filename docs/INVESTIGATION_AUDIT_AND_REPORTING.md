# NAGARLENS — Investigation Audit Trail & Reporting

> **Document Version:** 1.0.0  
> **Status:** Production Specification  

---

## 1. Audit Trail Immutability

- Timeline events (`TimelineEventSchema`) are stored append-only.
- Captures event type, actor ID, UTC timestamp, title, description, and metadata.
