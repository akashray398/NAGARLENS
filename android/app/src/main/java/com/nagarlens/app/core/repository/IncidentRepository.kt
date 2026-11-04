package com.nagarlens.app.core.repository

import com.nagarlens.app.core.model.Incident
import com.nagarlens.app.core.model.IncidentStatus
import com.nagarlens.app.core.model.InvestigationCase
import kotlinx.coroutines.flow.Flow

interface IncidentRepository {
    fun observeAllIncidents(): Flow<List<Incident>>
    fun observeOpenIncidentsCount(): Flow<Int>
    suspend fun getIncidentById(id: String): Incident?
    suspend fun createIncident(incident: Incident)
    suspend fun updateIncidentStatus(id: String, status: IncidentStatus)
    suspend fun getInvestigationCaseForIncident(incidentId: String): InvestigationCase
}
