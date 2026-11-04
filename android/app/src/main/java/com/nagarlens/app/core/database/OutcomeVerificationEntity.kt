package com.nagarlens.app.core.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.nagarlens.app.core.model.OutcomeVerification
import com.nagarlens.app.core.model.SyncStatus
import com.nagarlens.app.core.model.VerificationOutcome

@Entity(tableName = "outcome_verifications_records")
data class OutcomeVerificationEntity(
    @PrimaryKey val id: String,
    val interventionId: String,
    val assetId: String,
    val verificationObservationId: String,
    val outcome: String,
    val description: String? = null,
    val verifiedAtUtc: Long = System.currentTimeMillis(),
    val verifiedBy: String = "reviewer",
    val createdAtUtc: Long = System.currentTimeMillis(),
    val syncStatus: String = SyncStatus.LOCAL_ONLY.name
)

fun OutcomeVerificationEntity.toDomainModel(): OutcomeVerification {
    return OutcomeVerification(
        id = id,
        interventionId = interventionId,
        assetId = assetId,
        verificationObservationId = verificationObservationId,
        outcome = runCatching { VerificationOutcome.valueOf(outcome) }.getOrDefault(VerificationOutcome.INCONCLUSIVE),
        description = description,
        verifiedAtUtc = verifiedAtUtc,
        verifiedBy = verifiedBy,
        createdAtUtc = createdAtUtc,
        syncStatus = runCatching { SyncStatus.valueOf(syncStatus) }.getOrDefault(SyncStatus.LOCAL_ONLY)
    )
}

fun OutcomeVerification.toEntity(): OutcomeVerificationEntity {
    return OutcomeVerificationEntity(
        id = id,
        interventionId = interventionId,
        assetId = assetId,
        verificationObservationId = verificationObservationId,
        outcome = outcome.name,
        description = description,
        verifiedAtUtc = verifiedAtUtc,
        verifiedBy = verifiedBy,
        createdAtUtc = createdAtUtc,
        syncStatus = syncStatus.name
    )
}
