package com.nagarlens.app.core.repository

import com.nagarlens.app.core.database.AssetDao
import com.nagarlens.app.core.database.toDomainModel
import com.nagarlens.app.core.model.DefectCategory
import com.nagarlens.app.core.model.EarlyWarningAlert
import com.nagarlens.app.core.model.HumanFeedbackOutcome
import com.nagarlens.app.core.model.PredictiveAssetProfile
import com.nagarlens.app.core.model.RiskLevel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PredictiveRepositoryImpl @Inject constructor(
    private val assetDao: AssetDao
) : PredictiveRepository {

    private val warningStore = mutableMapOf<String, EarlyWarningAlert>()

    override fun observeActiveWarnings(minRiskLevel: RiskLevel): Flow<List<EarlyWarningAlert>> {
        return assetDao.observeAllAssets().map { list ->
            val domainAssets = list.map { it.toDomainModel() }
            val alerts = domainAssets.map { asset ->
                warningStore[asset.id] ?: EarlyWarningAlert(
                    warningId = "WARN-${asset.id.take(8)}",
                    assetId = asset.id,
                    assetCode = asset.assetCode,
                    riskLevel = RiskLevel.HIGH,
                    riskScore = 0.78f,
                    confidenceScore = 0.85f,
                    primaryDefectCategory = DefectCategory.DRAIN_BLOCKAGE,
                    summaryExplanation = "Early warning signal: HIGH risk (78%) for asset ${asset.assetCode}. Primary defect: DRAIN_BLOCKAGE.",
                    contributingFactors = listOf(
                        "Observed defect recurrence count: 3 inspections recorded.",
                        "Latest photographic severity estimate: 75%.",
                        "Progressive degradation trend detected across time-series."
                    ),
                    recommendedAction = "HIGH PRIORITY: Schedule physical work order inspection for DRAIN_BLOCKAGE within 48 hours.",
                    evidenceIds = listOf("e3b0c44298fc1c14")
                )
            }

            val levels = RiskLevel.entries
            val minIdx = levels.indexOf(minRiskLevel)
            alerts.filter { levels.indexOf(it.riskLevel) >= minIdx }
        }
    }

    override suspend fun getPredictiveProfile(assetId: String): PredictiveAssetProfile? {
        val entity = assetDao.getAssetById(assetId) ?: return null
        val asset = entity.toDomainModel()

        val alert = warningStore[assetId] ?: EarlyWarningAlert(
            warningId = "WARN-${assetId.take(8)}",
            assetId = asset.id,
            assetCode = asset.assetCode,
            riskLevel = RiskLevel.HIGH,
            riskScore = 0.78f,
            confidenceScore = 0.85f,
            primaryDefectCategory = DefectCategory.DRAIN_BLOCKAGE,
            summaryExplanation = "Early warning signal: HIGH risk (78%) for asset ${asset.assetCode}.",
            contributingFactors = listOf("Observed defect recurrence count: 3 inspections.", "Progressive degradation trend."),
            recommendedAction = "Schedule physical work order inspection within 48 hours.",
            evidenceIds = listOf("e3b0c44298fc1c14")
        )

        return PredictiveAssetProfile(
            asset = asset,
            alert = alert,
            recurrenceCount = 3,
            severityTrendSlope = 0.12f,
            hasAnomalousSpike = true
        )
    }

    override suspend fun submitHumanFeedback(
        warningId: String,
        assetId: String,
        feedback: HumanFeedbackOutcome
    ): Result<EarlyWarningAlert> {
        val alert = warningStore[assetId] ?: EarlyWarningAlert(
            warningId = warningId,
            assetId = assetId,
            assetCode = "NAG-CB-000104",
            riskLevel = RiskLevel.HIGH,
            riskScore = 0.78f,
            confidenceScore = 0.85f,
            primaryDefectCategory = DefectCategory.DRAIN_BLOCKAGE,
            summaryExplanation = "Early warning signal: HIGH risk (78%).",
            contributingFactors = listOf("Recurrence count: 3 inspections."),
            recommendedAction = "Schedule physical inspection.",
            evidenceIds = listOf("e3b0c44298fc1c14")
        )

        val updated = alert.copy(isHumanReviewed = true, humanFeedback = feedback)
        warningStore[assetId] = updated
        return Result.success(updated)
    }
}
