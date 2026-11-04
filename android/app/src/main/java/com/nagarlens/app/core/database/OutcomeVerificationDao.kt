package com.nagarlens.app.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface OutcomeVerificationDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertOutcomeVerification(entity: OutcomeVerificationEntity): Long

    @Query("SELECT * FROM outcome_verifications_records WHERE assetId = :assetId ORDER BY verifiedAtUtc ASC")
    fun observeOutcomeVerificationsForAsset(assetId: String): Flow<List<OutcomeVerificationEntity>>

    @Query("SELECT * FROM outcome_verifications_records WHERE assetId = :assetId ORDER BY verifiedAtUtc ASC")
    suspend fun getOutcomeVerificationsForAssetSync(assetId: String): List<OutcomeVerificationEntity>
}
