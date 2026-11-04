package com.nagarlens.app.core.repository

import com.nagarlens.app.core.model.AssetTimeline
import com.nagarlens.app.core.model.ConditionSeverity
import com.nagarlens.app.core.model.DefectCategory
import com.nagarlens.app.core.model.InfrastructureObservation
import com.nagarlens.app.core.model.Intervention
import com.nagarlens.app.core.model.InterventionType
import com.nagarlens.app.core.model.ObservationType
import com.nagarlens.app.core.model.OutcomeVerification
import com.nagarlens.app.core.model.VerificationOutcome
import kotlinx.coroutines.flow.Flow

interface TemporalRepository {
    fun getObservationsForAsset(assetId: String): Flow<List<InfrastructureObservation>>
    fun getInterventionsForAsset(assetId: String): Flow<List<Intervention>>
    fun getOutcomeVerificationsForAsset(assetId: String): Flow<List<OutcomeVerification>>
    fun getAssetTimeline(assetId: String, assetCode: String): Flow<AssetTimeline>

    suspend fun recordObservation(
        assetId: String,
        type: ObservationType,
        category: DefectCategory,
        severity: ConditionSeverity,
        description: String?,
        latitude: Double,
        longitude: Double,
        evidenceId: String? = null
    ): Result<InfrastructureObservation>

    suspend fun recordIntervention(
        assetId: String,
        type: InterventionType,
        title: String,
        description: String?
    ): Result<Intervention>

    suspend fun submitOutcomeVerification(
        interventionId: String,
        assetId: String,
        verificationObservationId: String,
        outcome: VerificationOutcome,
        description: String?
    ): Result<OutcomeVerification>
}
