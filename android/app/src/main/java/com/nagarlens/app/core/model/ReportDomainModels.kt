package com.nagarlens.app.core.model

enum class ReportType {
    MUNICIPAL_INFRASTRUCTURE_AUDIT,
    INCIDENT_SUMMARY_REPORT,
    EVIDENCE_CHAIN_OF_CUSTODY,
    ASSET_CONDITION_ASSESSMENT
}

enum class ReportFormat {
    PDF,
    CSV,
    JSON
}

data class InfrastructureReport(
    val id: String,
    val title: String,
    val reportType: ReportType,
    val format: ReportFormat,
    val generatedBy: String,
    val totalRecordsIncluded: Int,
    val generatedTimestampUtc: Long = System.currentTimeMillis(),
    val downloadUrl: String? = null
)
