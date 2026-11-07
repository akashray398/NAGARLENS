package com.nagarlens.app

import com.nagarlens.app.core.database.AssetDao
import com.nagarlens.app.core.database.AssetEntity
import com.nagarlens.app.core.model.HumanFeedbackOutcome
import com.nagarlens.app.core.model.RiskLevel
import com.nagarlens.app.core.repository.PredictiveRepositoryImpl
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PredictiveEngineTest {

    @Test
    fun `predictiveRepository observes active early warning alerts`() = runTest {
        val fakeAssetDao = FakeAssetDaoForPredictive()
        val repository = PredictiveRepositoryImpl(fakeAssetDao)

        val alerts = repository.observeActiveWarnings(RiskLevel.INFORMATIONAL).first()
        assertEquals(1, alerts.size)
        val alert = alerts[0]
        assertEquals("NAG-CB-000104", alert.assetCode)
        assertEquals(RiskLevel.HIGH, alert.riskLevel)
        assertTrue(alert.riskScore >= 0.65f)
    }

    @Test
    fun `predictiveRepository submits human review feedback`() = runTest {
        val fakeAssetDao = FakeAssetDaoForPredictive()
        val repository = PredictiveRepositoryImpl(fakeAssetDao)

        val result = repository.submitHumanFeedback("WARN-AST-101", "AST-101", HumanFeedbackOutcome.CONFIRMED)
        assertTrue(result.isSuccess)
        val updatedAlert = result.getOrNull()
        assertNotNull(updatedAlert)
        assertTrue(updatedAlert!!.isHumanReviewed)
        assertEquals(HumanFeedbackOutcome.CONFIRMED, updatedAlert.humanFeedback)
    }

    private class FakeAssetDaoForPredictive : AssetDao {
        override fun observeAllAssets(): Flow<List<AssetEntity>> = flowOf(
            listOf(
                AssetEntity(
                    id = "AST-101",
                    assetCode = "NAG-CB-000104",
                    assetCategory = "CATCH_BASIN",
                    latitude = 12.9716,
                    longitude = 77.5946,
                    verificationStatus = "VERIFIED"
                )
            )
        )
        override fun observeAssetsInViewport(minLat: Double, minLon: Double, maxLat: Double, maxLon: Double): Flow<List<AssetEntity>> = flowOf(emptyList())
        override fun observeAssetById(id: String): Flow<AssetEntity?> = flowOf(null)
        override suspend fun getAssetById(id: String): AssetEntity = AssetEntity(id = id, assetCode = "NAG-CB-000104", assetCategory = "CATCH_BASIN", latitude = 12.9716, longitude = 77.5946)
        override suspend fun getAssetCountByCategory(category: String): Int = 1
        override suspend fun insertAsset(entity: AssetEntity): Long = 1L
        override suspend fun updateAsset(entity: AssetEntity) {}
        override suspend fun deleteAsset(id: String) {}
    }
}
