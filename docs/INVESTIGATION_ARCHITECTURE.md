# NAGARLENS — Investigation Engine Architecture Specification

> **Product:** NAGARLENS Infrastructure Intelligence Network  
> **Document Version:** 1.0.0  
> **Status:** Active Production Specification  

---

## 1. High-Level Investigation Engine Topology

The Investigation Engine links field evidence collections, computer vision defect findings, predictive risk signals, municipal work order incidents, human approval gates, and controlled action executions into a unified investigation workspace.

```mermaid
flowchart TD
    subgraph Inputs ["Multi-Source Evidence & Signal Inputs"]
        INC["Municipal Incidents\n(Work Orders)"]
        PRED["Predictive Risk Alerts\n(Early Warning Signals)"]
        EVID["Field Evidence\n(SHA-256 Verified Media)"]
    end

    subgraph Investigation_Engine ["Investigation Lifecycle Engine"]
        INV["Investigation Workspace\n(INV-10001)"]
        TL["Append-Only Timeline\n(InvestigationTimelineEvent)"]
        REQ["Evidence Collection Request\n(EvidenceRequestRecord)"]
    end

    subgraph Automation_Governance ["Controlled Automation & Approval Gates"]
        RULES["Automation Rule Engine\n(TriggerEvent & Conditions)"]
        APPROVAL["Human Approval Gate\n(Reviewer / Administrator Approval)"]
        EXEC["Action Execution Engine\n(Idempotent Task Dispatch)"]
    end

    INC --> INV
    PRED --> INV
    EVID --> INV

    INV --> TL
    INV --> REQ

    INV --> RULES
    RULES --> APPROVAL
    APPROVAL --> EXEC
```

---

## 2. Component Index

- **`InvestigationStatus`**: 14-state lifecycle state machine (`DRAFT`, `OPEN`, `TRIAGED`, `INVESTIGATING`, `AWAITING_EVIDENCE`, `AWAITING_REVIEW`, `ACTION_PROPOSED`, `AWAITING_APPROVAL`, `ACTION_IN_PROGRESS`, `RESOLUTION_REVIEW`, `RESOLVED`, `CLOSED`, `REJECTED`, `REOPENED`).
- **`TimelineEventSchema`**: Immutable append-only audit event history.
- **`AutomationRuleSchema`**: Versioned trigger-condition-action definitions.
- **`ApprovalRequestSchema`**: Mandatory human-in-the-loop approval gate.
