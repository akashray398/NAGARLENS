package com.nagarlens.app.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface IncidentDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIncident(incident: IncidentEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIncidents(incidents: List<IncidentEntity>)

    @Query("SELECT * FROM incidents_records ORDER BY createdTimestampUtc DESC")
    fun observeAllIncidents(): Flow<List<IncidentEntity>>

    @Query("SELECT * FROM incidents_records WHERE assetId = :assetId ORDER BY createdTimestampUtc DESC")
    fun observeIncidentsForAsset(assetId: String): Flow<List<IncidentEntity>>

    @Query("SELECT * FROM incidents_records WHERE id = :id")
    suspend fun getIncidentById(id: String): IncidentEntity?

    @Query("UPDATE incidents_records SET status = :status, updatedTimestampUtc = :updatedAt WHERE id = :id")
    suspend fun updateIncidentStatus(id: String, status: String, updatedAt: Long = System.currentTimeMillis())

    @Query("SELECT COUNT(*) FROM incidents_records WHERE status != 'RESOLVED' AND status != 'CLOSED'")
    fun observeOpenIncidentsCount(): Flow<Int>
}
