package com.nagarlens.app.core.repository

import com.nagarlens.app.core.model.Asset
import com.nagarlens.app.core.model.AssetCategory
import com.nagarlens.app.core.model.VerificationDecision
import kotlinx.coroutines.flow.Flow

interface AssetRepository {
    fun getAllAssets(): Flow<List<Asset>>
    fun getAssetById(id: String): Flow<Asset?>
    fun getAssetsInViewport(minLat: Double, minLon: Double, maxLat: Double, maxLon: Double): Flow<List<Asset>>
    suspend fun registerAsset(
        category: AssetCategory,
        latitude: Double,
        longitude: Double,
        altitudeMeters: Float? = null,
        description: String? = null,
        evidenceId: String? = null
    ): Result<Asset>
    suspend fun submitVerification(
        assetId: String,
        decision: VerificationDecision,
        reviewerId: String,
        reason: String? = null
    ): Result<Asset>
}
