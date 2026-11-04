package com.nagarlens.app.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface InterventionDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertIntervention(entity: InterventionEntity): Long

    @Update
    suspend fun updateIntervention(entity: InterventionEntity)

    @Query("SELECT * FROM interventions_records WHERE id = :id")
    suspend fun getInterventionById(id: String): InterventionEntity?

    @Query("SELECT * FROM interventions_records WHERE assetId = :assetId ORDER BY createdAtUtc ASC")
    fun observeInterventionsForAsset(assetId: String): Flow<List<InterventionEntity>>

    @Query("SELECT * FROM interventions_records WHERE assetId = :assetId ORDER BY createdAtUtc ASC")
    suspend fun getInterventionsForAssetSync(assetId: String): List<InterventionEntity>
}
