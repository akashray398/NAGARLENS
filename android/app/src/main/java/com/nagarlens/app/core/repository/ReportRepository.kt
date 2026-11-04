package com.nagarlens.app.core.repository

import com.nagarlens.app.core.model.InfrastructureReport
import com.nagarlens.app.core.model.ReportFormat
import com.nagarlens.app.core.model.ReportType
import kotlinx.coroutines.flow.Flow

interface ReportRepository {
    fun observeAllReports(): Flow<List<InfrastructureReport>>
    suspend fun generateReport(type: ReportType, format: ReportFormat): Result<InfrastructureReport>
}
