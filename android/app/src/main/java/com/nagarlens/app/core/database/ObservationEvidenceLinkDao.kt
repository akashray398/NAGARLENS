package com.nagarlens.app.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ObservationEvidenceLinkDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertLink(entity: ObservationEvidenceLinkEntity): Long

    @Query("SELECT * FROM observation_evidence_link_records WHERE observationId = :observationId")
    fun observeLinksForObservation(observationId: String): Flow<List<ObservationEvidenceLinkEntity>>

    @Query("SELECT * FROM observation_evidence_link_records WHERE observationId = :observationId")
    suspend fun getLinksForObservationSync(observationId: String): List<ObservationEvidenceLinkEntity>
}
