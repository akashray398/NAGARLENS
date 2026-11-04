# NAGARLENS — Asset Memory Timeline Workflow

> **Chronological Asset Memory Flow**  

---

## 1. Timeline Events & Progression

1. **Asset Registration Event:** Asset is created with permanent UUID and `asset_code`.
2. **Field Inspection Observation:** Field engineer inspects asset, records `BLOCKAGE_REPORT` or `WATERLOGGING_REPORT` with condition severity (`CRITICAL`), and links captured photo evidence.
3. **Intervention Scheduling & Execution:** Maintenance team initiates `SILT_REMOVAL` intervention (`IN_PROGRESS` $\rightarrow$ `COMPLETED`) with `BEFORE_ACTION` and `AFTER_ACTION` photos attached.
4. **Outcome Verification:** Reviewer performs follow-up inspection, recording `IMPROVED` outcome verification linked to the verification observation.
