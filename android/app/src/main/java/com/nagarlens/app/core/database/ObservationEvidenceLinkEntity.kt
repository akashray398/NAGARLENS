package com.nagarlens.app.core.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.nagarlens.app.core.model.ObservationEvidenceLink

@Entity(tableName = "observation_evidence_link_records")
data class ObservationEvidenceLinkEntity(
    @PrimaryKey val id: String,
    val observationId: String,
    val evidenceId: String,
    val relationshipType: String = "CONDITION_PROOF",
    val linkedAtUtc: Long = System.currentTimeMillis()
)

fun ObservationEvidenceLinkEntity.toDomainModel(): ObservationEvidenceLink {
    return ObservationEvidenceLink(
        id = id,
        observationId = observationId,
        evidenceId = evidenceId,
        relationshipType = relationshipType,
        linkedAtUtc = linkedAtUtc
    )
}

fun ObservationEvidenceLink.toEntity(): ObservationEvidenceLinkEntity {
    return ObservationEvidenceLinkEntity(
        id = id,
        observationId = observationId,
        evidenceId = evidenceId,
        relationshipType = relationshipType,
        linkedAtUtc = linkedAtUtc
    )
}
