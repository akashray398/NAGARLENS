package com.nagarlens.app.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ObservationDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertObservation(entity: ObservationEntity): Long

    @Update
    suspend fun updateObservation(entity: ObservationEntity)

    @Query("SELECT * FROM observations_records WHERE id = :id")
    suspend fun getObservationById(id: String): ObservationEntity?

    @Query("SELECT * FROM observations_records WHERE assetId = :assetId ORDER BY observedAtUtc ASC")
    fun observeObservationsForAsset(assetId: String): Flow<List<ObservationEntity>>

    @Query("SELECT * FROM observations_records WHERE assetId = :assetId ORDER BY observedAtUtc ASC")
    suspend fun getObservationsForAssetSync(assetId: String): List<ObservationEntity>
}
