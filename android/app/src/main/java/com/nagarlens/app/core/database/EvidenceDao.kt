package com.nagarlens.app.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface EvidenceDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertEvidence(entity: EvidenceEntity): Long

    @Update
    suspend fun updateEvidence(entity: EvidenceEntity)

    @Query("SELECT * FROM evidence_records WHERE id = :id")
    suspend fun getEvidenceById(id: String): EvidenceEntity?

    @Query("SELECT * FROM evidence_records WHERE id = :id")
    fun observeEvidenceById(id: String): Flow<EvidenceEntity?>

    @Query("SELECT * FROM evidence_records ORDER BY captureTimestampUtc DESC")
    fun observeAllEvidence(): Flow<List<EvidenceEntity>>

    @Query("SELECT * FROM evidence_records WHERE syncStatus = :syncStatus ORDER BY captureTimestampUtc DESC")
    fun observeEvidenceBySyncStatus(syncStatus: String): Flow<List<EvidenceEntity>>

    @Query("SELECT * FROM evidence_records WHERE syncStatus = :syncStatus AND updatedTimestampUtc < :cutoffTimestamp")
    suspend fun getStaleRecordsBySyncStatus(syncStatus: String, cutoffTimestamp: Long): List<EvidenceEntity>

    @Query("UPDATE evidence_records SET syncStatus = :syncStatus, updatedTimestampUtc = :updatedAt WHERE id = :id")
    suspend fun updateSyncStatus(id: String, syncStatus: String, updatedAt: Long)

    @Query("UPDATE evidence_records SET syncStatus = :syncStatus, retryCount = retryCount + 1, lastSyncError = :error, updatedTimestampUtc = :updatedAt WHERE id = :id")
    suspend fun recordSyncFailure(id: String, syncStatus: String, error: String, updatedAt: Long)

    @Query("DELETE FROM evidence_records WHERE id = :id")
    suspend fun deleteEvidence(id: String)
}
