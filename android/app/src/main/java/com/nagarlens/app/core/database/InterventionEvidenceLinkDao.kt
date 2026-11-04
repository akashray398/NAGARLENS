package com.nagarlens.app.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface InterventionEvidenceLinkDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertLink(entity: InterventionEvidenceLinkEntity): Long

    @Query("SELECT * FROM intervention_evidence_link_records WHERE interventionId = :interventionId")
    fun observeLinksForIntervention(interventionId: String): Flow<List<InterventionEvidenceLinkEntity>>

    @Query("SELECT * FROM intervention_evidence_link_records WHERE interventionId = :interventionId")
    suspend fun getLinksForInterventionSync(interventionId: String): List<InterventionEvidenceLinkEntity>
}
