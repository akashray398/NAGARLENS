package com.nagarlens.app.core.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.nagarlens.app.core.model.InterventionEvidenceLink
import com.nagarlens.app.core.model.InterventionEvidenceType

@Entity(tableName = "intervention_evidence_link_records")
data class InterventionEvidenceLinkEntity(
    @PrimaryKey val id: String,
    val interventionId: String,
    val evidenceId: String,
    val relationshipType: String = InterventionEvidenceType.BEFORE_ACTION.name,
    val linkedAtUtc: Long = System.currentTimeMillis()
)

fun InterventionEvidenceLinkEntity.toDomainModel(): InterventionEvidenceLink {
    return InterventionEvidenceLink(
        id = id,
        interventionId = interventionId,
        evidenceId = evidenceId,
        relationshipType = runCatching { InterventionEvidenceType.valueOf(relationshipType) }.getOrDefault(InterventionEvidenceType.BEFORE_ACTION),
        linkedAtUtc = linkedAtUtc
    )
}

fun InterventionEvidenceLink.toEntity(): InterventionEvidenceLinkEntity {
    return InterventionEvidenceLinkEntity(
        id = id,
        interventionId = interventionId,
        evidenceId = evidenceId,
        relationshipType = relationshipType.name,
        linkedAtUtc = linkedAtUtc
    )
}
