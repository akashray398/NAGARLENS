package com.nagarlens.app.core.repository

import com.nagarlens.app.core.database.AssetDao
import com.nagarlens.app.core.database.toDomainModel
import com.nagarlens.app.core.database.toEntity
import com.nagarlens.app.core.model.Asset
import com.nagarlens.app.core.model.AssetCategory
import com.nagarlens.app.core.model.AssetLifecycleStatus
import com.nagarlens.app.core.model.AssetVerificationStatus
import com.nagarlens.app.core.model.VerificationDecision
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AssetRepositoryImpl @Inject constructor(
    private val assetDao: AssetDao
) : AssetRepository {

    override fun getAllAssets(): Flow<List<Asset>> {
        return assetDao.observeAllAssets().map { list ->
            if (list.isEmpty()) {
                val seed = getSeedAssets()
                seed.forEach { assetDao.insertAsset(it.toEntity()) }
                seed
            } else {
                list.map { it.toDomainModel() }
            }
        }
    }

    override fun getAssetById(id: String): Flow<Asset?> {
        return assetDao.observeAssetById(id).map { entity ->
            entity?.toDomainModel()
        }
    }

    override fun getAssetsInViewport(
        minLat: Double,
        minLon: Double,
        maxLat: Double,
        maxLon: Double
    ): Flow<List<Asset>> {
        return assetDao.observeAssetsInViewport(minLat, minLon, maxLat, maxLon).map { list ->
            list.map { it.toDomainModel() }
        }
    }

    override suspend fun registerAsset(
        category: AssetCategory,
        latitude: Double,
        longitude: Double,
        altitudeMeters: Float?,
        description: String?,
        evidenceId: String?
    ): Result<Asset> = withContext(Dispatchers.IO) {
        try {
            val assetId = UUID.randomUUID().toString()
            val prefix = AssetCategory.getPrefix(category)
            val currentCount = assetDao.getAssetCountByCategory(category.name)
            val seqNum = currentCount + 1
            val assetCode = "NAG-$prefix-${"%06d".format(seqNum)}"

            val now = System.currentTimeMillis()

            val asset = Asset(
                id = assetId,
                assetCode = assetCode,
                assetCategory = category,
                lifecycleStatus = AssetLifecycleStatus.ACTIVE,
                verificationStatus = AssetVerificationStatus.UNVERIFIED,
                latitude = latitude,
                longitude = longitude,
                altitudeMeters = altitudeMeters,
                description = description,
                version = 1,
                createdBy = "field_worker",
                createdTimestampUtc = now,
                updatedTimestampUtc = now
            )

            assetDao.insertAsset(asset.toEntity())
            Result.success(asset)
        } catch (exc: Exception) {
            Result.failure(exc)
        }
    }

    override suspend fun submitVerification(
        assetId: String,
        decision: VerificationDecision,
        reviewerId: String,
        reason: String?
    ): Result<Asset> = withContext(Dispatchers.IO) {
        try {
            val existing = assetDao.getAssetById(assetId) ?: return@withContext Result.failure(IllegalArgumentException("Asset $assetId not found"))
            val now = System.currentTimeMillis()

            val newVerificationStatus = when (decision) {
                VerificationDecision.APPROVED -> AssetVerificationStatus.VERIFIED
                VerificationDecision.REJECTED -> AssetVerificationStatus.REJECTED
                VerificationDecision.CORRECTION_REQUESTED -> AssetVerificationStatus.CORRECTION_REQUIRED
            }

            val updatedEntity = existing.copy(
                verificationStatus = newVerificationStatus.name,
                version = existing.version + 1,
                updatedTimestampUtc = now
            )

            assetDao.updateAsset(updatedEntity)
            Result.success(updatedEntity.toDomainModel())
        } catch (exc: Exception) {
            Result.failure(exc)
        }
    }

    override suspend fun deleteAsset(assetId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            assetDao.deleteAsset(assetId)
            Result.success(Unit)
        } catch (exc: Exception) {
            Result.failure(exc)
        }
    }

    private fun getSeedAssets(): List<Asset> {
        val now = System.currentTimeMillis()
        return listOf(
            Asset(
                id = "AST-10001",
                assetCode = "NAG-CB-000104",
                assetCategory = AssetCategory.CATCH_BASIN,
                lifecycleStatus = AssetLifecycleStatus.ACTIVE,
                verificationStatus = AssetVerificationStatus.UNVERIFIED,
                latitude = 12.9716,
                longitude = 77.5946,
                description = "Catch Basin Invert - Ward 12 Main Drain",
                createdTimestampUtc = now,
                updatedTimestampUtc = now
            ),
            Asset(
                id = "AST-10002",
                assetCode = "NAG-SWD-000201",
                assetCategory = AssetCategory.STORM_WATER_DRAIN,
                lifecycleStatus = AssetLifecycleStatus.ACTIVE,
                verificationStatus = AssetVerificationStatus.VERIFIED,
                latitude = 12.9740,
                longitude = 77.5980,
                description = "Primary Arterial Stormwater Conduit",
                createdTimestampUtc = now - 3600000,
                updatedTimestampUtc = now - 3600000
            ),
            Asset(
                id = "AST-10003",
                assetCode = "NAG-MH-000052",
                assetCategory = AssetCategory.MANHOLE,
                lifecycleStatus = AssetLifecycleStatus.ACTIVE,
                verificationStatus = AssetVerificationStatus.VERIFIED,
                latitude = 12.9680,
                longitude = 77.5910,
                description = "Sewer Interceptor Junction Manhole",
                createdTimestampUtc = now - 7200000,
                updatedTimestampUtc = now - 7200000
            ),
            Asset(
                id = "AST-10004",
                assetCode = "NAG-CVT-000012",
                assetCategory = AssetCategory.CULVERT,
                lifecycleStatus = AssetLifecycleStatus.ACTIVE,
                verificationStatus = AssetVerificationStatus.CORRECTION_REQUIRED,
                latitude = 12.9700,
                longitude = 77.5960,
                description = "Box Culvert Outfall Channel",
                createdTimestampUtc = now - 10800000,
                updatedTimestampUtc = now - 10800000
            )
        )
    }
}
