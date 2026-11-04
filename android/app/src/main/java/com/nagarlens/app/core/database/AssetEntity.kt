package com.nagarlens.app.core.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.nagarlens.app.core.model.Asset
import com.nagarlens.app.core.model.AssetCategory
import com.nagarlens.app.core.model.AssetLifecycleStatus
import com.nagarlens.app.core.model.AssetVerificationStatus

@Entity(tableName = "assets_records")
data class AssetEntity(
    @PrimaryKey val id: String,
    val assetCode: String,
    val assetCategory: String,
    val lifecycleStatus: String = AssetLifecycleStatus.ACTIVE.name,
    val verificationStatus: String = AssetVerificationStatus.UNVERIFIED.name,
    val latitude: Double,
    val longitude: Double,
    val altitudeMeters: Float? = null,
    val description: String? = null,
    val version: Int = 1,
    val createdBy: String = "field_worker",
    val createdTimestampUtc: Long = System.currentTimeMillis(),
    val updatedTimestampUtc: Long = System.currentTimeMillis()
)

fun AssetEntity.toDomainModel(): Asset {
    return Asset(
        id = id,
        assetCode = assetCode,
        assetCategory = runCatching { AssetCategory.valueOf(assetCategory) }.getOrDefault(AssetCategory.UNKNOWN),
        lifecycleStatus = runCatching { AssetLifecycleStatus.valueOf(lifecycleStatus) }.getOrDefault(AssetLifecycleStatus.ACTIVE),
        verificationStatus = runCatching { AssetVerificationStatus.valueOf(verificationStatus) }.getOrDefault(AssetVerificationStatus.UNVERIFIED),
        latitude = latitude,
        longitude = longitude,
        altitudeMeters = altitudeMeters,
        description = description,
        version = version,
        createdBy = createdBy,
        createdTimestampUtc = createdTimestampUtc,
        updatedTimestampUtc = updatedTimestampUtc
    )
}

fun Asset.toEntity(): AssetEntity {
    return AssetEntity(
        id = id,
        assetCode = assetCode,
        assetCategory = assetCategory.name,
        lifecycleStatus = lifecycleStatus.name,
        verificationStatus = verificationStatus.name,
        latitude = latitude,
        longitude = longitude,
        altitudeMeters = altitudeMeters,
        description = description,
        version = version,
        createdBy = createdBy,
        createdTimestampUtc = createdTimestampUtc,
        updatedTimestampUtc = updatedTimestampUtc
    )
}
