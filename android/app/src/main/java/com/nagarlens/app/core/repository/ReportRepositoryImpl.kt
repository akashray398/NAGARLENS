package com.nagarlens.app.core.repository

import com.nagarlens.app.core.model.InfrastructureReport
import com.nagarlens.app.core.model.ReportFormat
import com.nagarlens.app.core.model.ReportType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReportRepositoryImpl @Inject constructor() : ReportRepository {

    private val seedReports = mutableListOf(
        InfrastructureReport(
            id = "REP-2025-001",
            title = "Ward 12 Municipal Infrastructure Drainage Audit",
            reportType = ReportType.MUNICIPAL_INFRASTRUCTURE_AUDIT,
            format = ReportFormat.PDF,
            generatedBy = "BBMP Stormwater Engineer",
            totalRecordsIncluded = 24,
            generatedTimestampUtc = System.currentTimeMillis() - (1000 * 3600 * 24 * 2)
        ),
        InfrastructureReport(
            id = "REP-2025-002",
            title = "Monsoon Blockage Incident Summary",
            reportType = ReportType.INCIDENT_SUMMARY_REPORT,
            format = ReportFormat.CSV,
            generatedBy = "System Auditor",
            totalRecordsIncluded = 12,
            generatedTimestampUtc = System.currentTimeMillis() - (1000 * 3600 * 24 * 5)
        )
    )

    private val _reportsFlow = MutableStateFlow<List<InfrastructureReport>>(seedReports.toList())

    override fun observeAllReports(): Flow<List<InfrastructureReport>> {
        return _reportsFlow.asStateFlow()
    }

    override suspend fun generateReport(
        type: ReportType,
        format: ReportFormat
    ): Result<InfrastructureReport> {
        val newReport = InfrastructureReport(
            id = "REP-2025-${(100..999).random()}",
            title = "${type.name.replace("_", " ")} (${format.name})",
            reportType = type,
            format = format,
            generatedBy = "Field Auditor (Local Boundary)",
            totalRecordsIncluded = 18,
            generatedTimestampUtc = System.currentTimeMillis()
        )
        seedReports.add(0, newReport)
        _reportsFlow.value = seedReports.toList()
        return Result.success(newReport)
    }
}
