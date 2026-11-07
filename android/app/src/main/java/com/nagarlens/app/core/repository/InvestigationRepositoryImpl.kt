package com.nagarlens.app.core.repository

import com.nagarlens.app.core.model.EvidenceRequestRecord
import com.nagarlens.app.core.model.IncidentPriority
import com.nagarlens.app.core.model.InvestigationCase
import com.nagarlens.app.core.model.InvestigationStatus
import com.nagarlens.app.core.model.InvestigationTimelineEvent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InvestigationRepositoryImpl @Inject constructor() : InvestigationRepository {

    private val casesStore = MutableStateFlow<List<InvestigationCase>>(
        listOf(
            InvestigationCase(
                id = "INV-10001",
                title = "Investigation: Silt Accumulation at Ward 12 Drain",
                description = "Evidence-driven root cause investigation into recurring silt accumulation.",
                assetId = "AST-10001",
                assetCode = "NAG-CB-000104",
                incidentId = "INC-10001",
                status = InvestigationStatus.INVESTIGATING,
                priority = IncidentPriority.HIGH,
                assignedInvestigator = "usr_dev_auditor_001",
                timelineEvents = listOf(
                    InvestigationTimelineEvent(
                        eventId = "EVT-001",
                        investigationId = "INV-10001",
                        eventType = "INVESTIGATION_CREATED",
                        actorId = "usr_dev_auditor_001",
                        timestampUtc = System.currentTimeMillis() - 7200000,
                        title = "Investigation Case Created",
                        description = "Investigation created from Incident INC-10001."
                    )
                )
            )
        )
    )

    override fun observeInvestigations(): Flow<List<InvestigationCase>> = casesStore.asStateFlow()

    override suspend fun getInvestigationById(id: String): InvestigationCase? {
        return casesStore.value.find { it.id == id }
    }

    override suspend fun createInvestigation(
        title: String,
        assetId: String,
        assetCode: String,
        description: String?
    ): Result<InvestigationCase> {
        val now = System.currentTimeMillis()
        val invId = "INV-${(10000 + casesStore.value.size + 1)}"

        val newCase = InvestigationCase(
            id = invId,
            title = title,
            description = description,
            assetId = assetId,
            assetCode = assetCode,
            status = InvestigationStatus.OPEN,
            createdTimestampUtc = now,
            updatedTimestampUtc = now,
            timelineEvents = listOf(
                InvestigationTimelineEvent(
                    eventId = "EVT-${now % 10000}",
                    investigationId = invId,
                    eventType = "INVESTIGATION_CREATED",
                    actorId = "usr_dev_auditor_001",
                    timestampUtc = now,
                    title = "Investigation Case Created",
                    description = description
                )
            )
        )

        casesStore.update { it + newCase }
        return Result.success(newCase)
    }

    override suspend fun updateInvestigationStatus(
        id: String,
        targetStatus: InvestigationStatus,
        comment: String?
    ): Result<InvestigationCase> {
        val currentCase = casesStore.value.find { it.id == id }
            ?: return Result.failure(Exception("Investigation '$id' not found."))

        val now = System.currentTimeMillis()
        val event = InvestigationTimelineEvent(
            eventId = "EVT-${now % 10000}",
            investigationId = id,
            eventType = "STATUS_CHANGED",
            actorId = "usr_dev_auditor_001",
            timestampUtc = now,
            title = "Status Advanced to ${targetStatus.name}",
            description = comment ?: "Status changed from ${currentCase.status.name} to ${targetStatus.name}."
        )

        val updatedCase = currentCase.copy(
            status = targetStatus,
            updatedTimestampUtc = now,
            timelineEvents = currentCase.timelineEvents + event
        )

        casesStore.update { list -> list.map { if (it.id == id) updatedCase else it } }
        return Result.success(updatedCase)
    }

    override suspend fun requestEvidence(
        investigationId: String,
        assetId: String,
        evidenceType: String,
        instructions: String
    ): Result<EvidenceRequestRecord> {
        val now = System.currentTimeMillis()
        val req = EvidenceRequestRecord(
            requestId = "REQ-${now % 10000}",
            investigationId = investigationId,
            assetId = assetId,
            requestedEvidenceType = evidenceType,
            instructions = instructions,
            requestedBy = "usr_dev_auditor_001",
            assignedTo = "usr_field_inspector",
            createdTimestampUtc = now
        )

        updateInvestigationStatus(
            id = investigationId,
            targetStatus = InvestigationStatus.AWAITING_EVIDENCE,
            comment = "Issued evidence request: $evidenceType. Instructions: $instructions"
        )

        return Result.success(req)
    }
}
