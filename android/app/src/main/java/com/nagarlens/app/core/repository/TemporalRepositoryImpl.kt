package com.nagarlens.app.core.repository

import com.nagarlens.app.core.database.InterventionDao
import com.nagarlens.app.core.database.InterventionEvidenceLinkDao
import com.nagarlens.app.core.database.ObservationDao
import com.nagarlens.app.core.database.ObservationEvidenceLinkDao
import com.nagarlens.app.core.database.OutcomeVerificationDao
import com.nagarlens.app.core.database.toDomainModel
import com.nagarlens.app.core.database.toEntity
import com.nagarlens.app.core.model.AssetTimeline
import com.nagarlens.app.core.model.ConditionSeverity
import com.nagarlens.app.core.model.DefectCategory
import com.nagarlens.app.core.model.InfrastructureObservation
import com.nagarlens.app.core.model.Intervention
import com.nagarlens.app.core.model.InterventionEvidenceLink
import com.nagarlens.app.core.model.InterventionEvidenceType
import com.nagarlens.app.core.model.InterventionStatus
import com.nagarlens.app.core.model.InterventionType
import com.nagarlens.app.core.model.ObservationEvidenceLink
import com.nagarlens.app.core.model.ObservationType
import com.nagarlens.app.core.model.OutcomeVerification
import com.nagarlens.app.core.model.SyncStatus
import com.nagarlens.app.core.model.TimelineEvent
import com.nagarlens.app.core.model.VerificationOutcome
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TemporalRepositoryImpl @Inject constructor(
    private val observationDao: ObservationDao,
    private val observationEvidenceLinkDao: ObservationEvidenceLinkDao,
    private val interventionDao: InterventionDao,
    private val interventionEvidenceLinkDao: InterventionEvidenceLinkDao,
    private val outcomeVerificationDao: OutcomeVerificationDao
) : TemporalRepository {

    override fun getObservationsForAsset(assetId: String): Flow<List<InfrastructureObservation>> {
        return observationDao.observeObservationsForAsset(assetId).map { list ->
            list.map { it.toDomainModel() }
        }
    }

    override fun getInterventionsForAsset(assetId: String): Flow<List<Intervention>> {
        return interventionDao.observeInterventionsForAsset(assetId).map { list ->
            list.map { it.toDomainModel() }
        }
    }

    override fun getOutcomeVerificationsForAsset(assetId: String): Flow<List<OutcomeVerification>> {
        return outcomeVerificationDao.observeOutcomeVerificationsForAsset(assetId).map { list ->
            list.map { it.toDomainModel() }
        }
    }

    override fun getAssetTimeline(assetId: String, assetCode: String): Flow<AssetTimeline> {
        return combine(
            getObservationsForAsset(assetId),
            getInterventionsForAsset(assetId),
            getOutcomeVerificationsForAsset(assetId)
        ) { obsList, intList, verList ->
            val events = mutableListOf<TimelineEvent>()

            obsList.forEach { obs ->
                events.add(
                    TimelineEvent(
                        eventId = obs.id,
                        eventType = "OBSERVATION",
                        timestampUtc = obs.observedAtUtc,
                        title = "Observation: ${obs.observationType.name}",
                        description = obs.description ?: "Condition Category: ${obs.conditionCategory.name}",
                        actorId = obs.observedBy,
                        severity = obs.conditionSeverity.name
                    )
                )
            }

            intList.forEach { interv ->
                events.add(
                    TimelineEvent(
                        eventId = interv.id,
                        eventType = "INTERVENTION",
                        timestampUtc = interv.createdAtUtc,
                        title = "Intervention: ${interv.title}",
                        description = interv.description ?: "Type: ${interv.interventionType.name} • Status: ${interv.status.name}",
                        actorId = interv.recordedBy
                    )
                )
            }

            verList.forEach { ver ->
                events.add(
                    TimelineEvent(
                        eventId = ver.id,
                        eventType = "VERIFICATION",
                        timestampUtc = ver.verifiedAtUtc,
                        title = "Outcome Verification: ${ver.outcome.name}",
                        description = ver.description ?: "Verified outcome for intervention ${ver.interventionId}",
                        actorId = ver.verifiedBy,
                        outcome = ver.outcome.name
                    )
                )
            }

            // Deterministic sorting tie-breaker: (timestampUtc, priority, eventId)
            val eventPriority = mapOf("REGISTRATION" to 0, "OBSERVATION" to 1, "INTERVENTION" to 2, "VERIFICATION" to 3)
            events.sortWith(compareBy({ it.timestampUtc }, { eventPriority.getOrDefault(it.eventType, 9) }, { it.eventId }))

            AssetTimeline(
                assetId = assetId,
                assetCode = assetCode,
                totalEvents = events.size,
                events = events
            )
        }
    }

    override suspend fun recordObservation(
        assetId: String,
        type: ObservationType,
        category: DefectCategory,
        severity: ConditionSeverity,
        description: String?,
        latitude: Double,
        longitude: Double,
        evidenceId: String?
    ): Result<InfrastructureObservation> = withContext(Dispatchers.IO) {
        try {
            val obsId = UUID.randomUUID().toString()
            val now = System.currentTimeMillis()

            val obs = InfrastructureObservation(
                id = obsId,
                assetId = assetId,
                observationType = type,
                conditionCategory = category,
                conditionSeverity = severity,
                description = description,
                observedAtUtc = now,
                recordedAtUtc = now,
                observedBy = "field_worker",
                latitude = latitude,
                longitude = longitude,
                source = "FIELD_INSPECTION",
                syncStatus = SyncStatus.LOCAL_ONLY
            )

            observationDao.insertObservation(obs.toEntity())

            if (evidenceId != null) {
                val link = ObservationEvidenceLink(
                    id = UUID.randomUUID().toString(),
                    observationId = obsId,
                    evidenceId = evidenceId,
                    relationshipType = "CONDITION_PROOF",
                    linkedAtUtc = now
                )
                observationEvidenceLinkDao.insertLink(link.toEntity())
            }

            Result.success(obs)
        } catch (exc: Exception) {
            Result.failure(exc)
        }
    }

    override suspend fun recordIntervention(
        assetId: String,
        type: InterventionType,
        title: String,
        description: String?
    ): Result<Intervention> = withContext(Dispatchers.IO) {
        try {
            val intervId = UUID.randomUUID().toString()
            val now = System.currentTimeMillis()

            val interv = Intervention(
                id = intervId,
                assetId = assetId,
                interventionType = type,
                title = title,
                description = description,
                status = InterventionStatus.PROPOSED,
                recordedBy = "field_worker",
                createdAtUtc = now,
                updatedAtUtc = now,
                syncStatus = SyncStatus.LOCAL_ONLY
            )

            interventionDao.insertIntervention(interv.toEntity())
            Result.success(interv)
        } catch (exc: Exception) {
            Result.failure(exc)
        }
    }

    override suspend fun submitOutcomeVerification(
        interventionId: String,
        assetId: String,
        verificationObservationId: String,
        outcome: VerificationOutcome,
        description: String?
    ): Result<OutcomeVerification> = withContext(Dispatchers.IO) {
        try {
            val verId = UUID.randomUUID().toString()
            val now = System.currentTimeMillis()

            val ver = OutcomeVerification(
                id = verId,
                interventionId = interventionId,
                assetId = assetId,
                verificationObservationId = verificationObservationId,
                outcome = outcome,
                description = description,
                verifiedAtUtc = now,
                verifiedBy = "reviewer",
                createdAtUtc = now,
                syncStatus = SyncStatus.LOCAL_ONLY
            )

            outcomeVerificationDao.insertOutcomeVerification(ver.toEntity())
            Result.success(ver)
        } catch (exc: Exception) {
            Result.failure(exc)
        }
    }
}
