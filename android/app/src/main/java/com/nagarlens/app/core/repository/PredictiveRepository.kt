package com.nagarlens.app.core.repository

import com.nagarlens.app.core.model.EarlyWarningAlert
import com.nagarlens.app.core.model.HumanFeedbackOutcome
import com.nagarlens.app.core.model.PredictiveAssetProfile
import com.nagarlens.app.core.model.RiskLevel
import kotlinx.coroutines.flow.Flow

interface PredictiveRepository {
    fun observeActiveWarnings(minRiskLevel: RiskLevel = RiskLevel.INFORMATIONAL): Flow<List<EarlyWarningAlert>>
    suspend fun getPredictiveProfile(assetId: String): PredictiveAssetProfile?
    suspend fun submitHumanFeedback(warningId: String, assetId: String, feedback: HumanFeedbackOutcome): Result<EarlyWarningAlert>
}
