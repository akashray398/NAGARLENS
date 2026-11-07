package com.nagarlens.app.core.repository

import com.nagarlens.app.core.model.EvidenceRequestRecord
import com.nagarlens.app.core.model.InvestigationCase
import com.nagarlens.app.core.model.InvestigationStatus
import kotlinx.coroutines.flow.Flow

interface InvestigationRepository {
    fun observeInvestigations(): Flow<List<InvestigationCase>>
    suspend fun getInvestigationById(id: String): InvestigationCase?
    suspend fun createInvestigation(title: String, assetId: String, assetCode: String, description: String?): Result<InvestigationCase>
    suspend fun updateInvestigationStatus(id: String, targetStatus: InvestigationStatus, comment: String?): Result<InvestigationCase>
    suspend fun requestEvidence(investigationId: String, assetId: String, evidenceType: String, instructions: String): Result<EvidenceRequestRecord>
}
