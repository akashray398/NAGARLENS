package com.nagarlens.app.core.repository

import com.nagarlens.app.core.database.IncidentDao
import com.nagarlens.app.core.database.toDomainModel
import com.nagarlens.app.core.database.toEntity
import com.nagarlens.app.core.model.DefectCategory
import com.nagarlens.app.core.model.Incident
import com.nagarlens.app.core.model.IncidentPriority
import com.nagarlens.app.core.model.IncidentStatus
import com.nagarlens.app.core.model.InvestigationCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IncidentRepositoryImpl @Inject constructor(
    private val incidentDao: IncidentDao
) : IncidentRepository {

    override fun observeAllIncidents(): Flow<List<Incident>> {
        return incidentDao.observeAllIncidents().map { entities ->
            if (entities.isEmpty()) {
                val seed = getSeedIncidents()
                incidentDao.insertIncidents(seed.map { it.toEntity() })
                seed
            } else {
                entities.map { it.toDomainModel() }
            }
        }
    }

    override fun observeOpenIncidentsCount(): Flow<Int> {
        return incidentDao.observeOpenIncidentsCount()
    }

    override suspend fun getIncidentById(id: String): Incident? {
        return incidentDao.getIncidentById(id)?.toDomainModel()
    }

    override suspend fun createIncident(incident: Incident) {
        incidentDao.insertIncident(incident.toEntity())
    }

    override suspend fun updateIncidentStatus(id: String, status: IncidentStatus) {
        incidentDao.updateIncidentStatus(id, status.name)
    }

    override suspend fun getInvestigationCaseForIncident(incidentId: String): InvestigationCase {
        val inc = getIncidentById(incidentId)
        return InvestigationCase(
            id = "INV-CASE-${incidentId.take(8)}",
            incidentId = incidentId,
            caseNumber = "CASE-2025-${(100..999).random()}",
            summary = "Root cause investigation for ${inc?.title ?: "Drainage Silt Failure"}",
            rootCauseAnalysis = "Visual evidence confirms major silt deposition obstructing channel cross-section after recent high-intensity precipitation.",
            assignedInvestigator = "Eng. V. Kumar (BBMP Stormwater Cell)",
            status = "ACTIVE_INVESTIGATION"
        )
    }

    private fun getSeedIncidents(): List<Incident> {
        return listOf(
            Incident(
                id = "INC-10001",
                assetId = "511a2c72-86f0-4c1a-9509-09c928410811",
                assetCode = "NAG-CB-000001",
                title = "Critical Silt Blockage at Catch Basin Invert",
                description = "Severe accumulation of silt and plastic trash blocking 90% of invert capacity.",
                status = IncidentStatus.OPEN,
                priority = IncidentPriority.CRITICAL,
                defectCategory = DefectCategory.DRAIN_BLOCKAGE
            ),
            Incident(
                id = "INC-10002",
                assetId = "7a8b9c0d-1234-5678-90ab-cdef12345678",
                assetCode = "NAG-SWD-000104",
                title = "Collapsed Retaining Wall on Stormwater Drain",
                description = "Masonry wall collapse causing debris spill into drain bed during monsoon.",
                status = IncidentStatus.IN_PROGRESS,
                priority = IncidentPriority.HIGH,
                defectCategory = DefectCategory.STRUCTURAL_DAMAGE
            ),
            Incident(
                id = "INC-10003",
                assetId = "9f8e7d6c-5432-1098-7654-3210fedcba98",
                assetCode = "NAG-MH-000052",
                title = "Broken Heavy-Duty Grate Cover",
                description = "Missing metal grate posing immediate public safety hazard at major pedestrian crosswalk.",
                status = IncidentStatus.RESOLVED,
                priority = IncidentPriority.MEDIUM,
                defectCategory = DefectCategory.COVER_DAMAGE_MISSING
            )
        )
    }
}
