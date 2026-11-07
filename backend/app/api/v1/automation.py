"""NAGARLENS — Controlled Automation Rules, Approvals & Action Execution Router."""

import time
import uuid
from typing import List, Optional
from fastapi import APIRouter, Depends, HTTPException, status
from pydantic import BaseModel

from app.core.security import AuthenticatedUser, get_current_user, get_current_reviewer
from app.schemas.automation import (
    ActionExecutionResponseSchema,
    ActionExecutionStatus,
    ApprovalRequestSchema,
    ApprovalStatus,
    ApprovalSubmissionSchema,
    AutomationRuleSchema,
    TriggerEvent,
)

router = APIRouter(prefix="/automation", tags=["Controlled Automation & Approval Gates"])

_RULES_DB: List[AutomationRuleSchema] = [
    AutomationRuleSchema(
        rule_id="RULE-001",
        name="Auto-Escalate Critical Risk Alert to Investigation",
        description="Creates investigation when predictive risk score >= 0.80.",
        trigger_event=TriggerEvent.HIGH_RISK_THRESHOLD_EXCEEDED,
        is_enabled=True,
        requires_approval=True
    ),
    AutomationRuleSchema(
        rule_id="RULE-002",
        name="Desilting Emergency Dispatch Gate",
        description="Requires municipal reviewer approval before dispatching emergency work order.",
        trigger_event=TriggerEvent.NEW_CRITICAL_INCIDENT,
        is_enabled=True,
        requires_approval=True
    )
]

_APPROVAL_DB: dict[str, ApprovalRequestSchema] = {
    "APP-1001": ApprovalRequestSchema(
        approval_id="APP-1001",
        investigation_id="INV-10001",
        proposed_action="Dispatch Emergency Desilting Work Order Crew",
        reason="Predictive risk score 78% with 4 recurring silt observations.",
        requested_by="usr_dev_auditor_001",
        status=ApprovalStatus.PENDING,
        created_timestamp_utc=int(time.time() * 1000) - 1800000
    )
}


@router.get("/rules", response_model=List[AutomationRuleSchema], summary="List all registered automation rules")
async def list_automation_rules():
    return _RULES_DB


@router.get("/approvals", response_model=List[ApprovalRequestSchema], summary="List pending human approval requests")
async def list_pending_approvals(current_user: AuthenticatedUser = Depends(get_current_user)):
    return list(_APPROVAL_DB.values())


@router.post("/approvals/{approval_id}", response_model=ApprovalRequestSchema, summary="Submit human reviewer approval or rejection decision")
async def submit_approval_decision(
    approval_id: str,
    submission: ApprovalSubmissionSchema,
    current_user: AuthenticatedUser = Depends(get_current_reviewer)
):
    approval = _APPROVAL_DB.get(approval_id)
    if not approval:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail=f"Approval request '{approval_id}' not found.")

    approval.status = submission.status
    approval.reviewer_id = current_user.user_id
    approval.reviewer_notes = submission.reviewer_notes
    return approval


@router.post("/execute", response_model=ActionExecutionResponseSchema, summary="Execute authorized action workflow following approval gate verification")
async def execute_action(
    rule_id: str,
    investigation_id: str,
    approval_id: Optional[str] = None,
    current_user: AuthenticatedUser = Depends(get_current_user)
):
    rule = next((r for r in _RULES_DB if r.rule_id == rule_id), None)
    if not rule:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail=f"Rule '{rule_id}' not found.")

    if rule.requires_approval:
        if not approval_id or approval_id not in _APPROVAL_DB:
            raise HTTPException(
                status_code=status.HTTP_403_FORBIDDEN,
                detail=f"Forbidden: Action for rule '{rule_id}' requires prior human approval. Missing approval ID."
            )
        approval = _APPROVAL_DB[approval_id]
        if approval.status != ApprovalStatus.APPROVED:
            raise HTTPException(
                status_code=status.HTTP_403_FORBIDDEN,
                detail=f"Forbidden: Cannot execute action. Approval status is '{approval.status.value}', expected 'APPROVED'."
            )

    now = int(time.time() * 1000)
    exec_id = f"EXEC-{now % 100000}"

    return ActionExecutionResponseSchema(
        execution_id=exec_id,
        rule_id=rule_id,
        investigation_id=investigation_id,
        action_type=rule.name,
        status=ActionExecutionStatus.SUCCEEDED,
        attempts=1,
        result_summary=f"Action '{rule.name}' executed successfully by {current_user.user_id}.",
        executed_timestamp_utc=now
    )
