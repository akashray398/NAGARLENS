# NAGARLENS — Asset Registration & Verification Workflow

> **Field Operation & Verification Process**  

---

## 1. Field Registration Flow

1. Field auditor opens the **Registration** screen.
2. Selects Asset Category (e.g., `CATCH_BASIN`).
3. Captures GNSS coordinates (or manually specifies location on map).
4. Enters optional description and links captured photo evidence.
5. Submits registration. An offline UUID and asset code (`NAG-CB-000104`) are assigned immediately.
6. Record is saved locally in Room database with status `UNVERIFIED` and enqueued for WorkManager sync.

---

## 2. Administrator Verification Flow

1. Authorized auditor or municipal engineer reviews pending asset registrations.
2. Inspects location coordinates, asset category, and linked SHA-256 evidence.
3. Submits verification decision: `APPROVED` (transitions status to `VERIFIED`), `REJECTED`, or `CORRECTION_REQUIRED`.
4. Decision record is saved in `asset_verification_records` with reviewer attribution and timestamp.
