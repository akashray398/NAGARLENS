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
            list.map { it.toDomainModel() }
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
}
