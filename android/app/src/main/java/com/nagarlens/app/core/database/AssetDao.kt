package com.nagarlens.app.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AssetDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAsset(entity: AssetEntity): Long

    @Update
    suspend fun updateAsset(entity: AssetEntity)

    @Query("SELECT * FROM assets_records WHERE id = :id")
    suspend fun getAssetById(id: String): AssetEntity?

    @Query("SELECT * FROM assets_records WHERE id = :id")
    fun observeAssetById(id: String): Flow<AssetEntity?>

    @Query("SELECT * FROM assets_records ORDER BY createdTimestampUtc DESC")
    fun observeAllAssets(): Flow<List<AssetEntity>>

    @Query("SELECT * FROM assets_records WHERE latitude >= :minLat AND latitude <= :maxLat AND longitude >= :minLon AND longitude <= :maxLon ORDER BY createdTimestampUtc DESC")
    fun observeAssetsInViewport(minLat: Double, minLon: Double, maxLat: Double, maxLon: Double): Flow<List<AssetEntity>>

    @Query("SELECT COUNT(*) FROM assets_records WHERE assetCategory = :category")
    suspend fun getAssetCountByCategory(category: String): Int

    @Query("DELETE FROM assets_records WHERE id = :id")
    suspend fun deleteAsset(id: String)
}
