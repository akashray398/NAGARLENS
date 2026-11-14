package com.nagarlens.app.feature.home

import com.nagarlens.app.core.model.EarlyWarningAlert
import com.nagarlens.app.core.model.InfrastructureMetrics

data class HomeUiState(
    val appTitle: String = "NAGARLENS",
    val experienceName: String = "LIVING CITY ENGINE",
    val category: String = "Infrastructure Intelligence Network",
    val tagline: String = "See the city. Understand its infrastructure. Anticipate its future.",
    val activeDomain: String = "Urban drainage failures & localized waterlogging",
    val activeWard: String = "Ward 12 — Bangalore Central",
    val locationBadge: String = "Bengaluru, KA • Live",
    val userName: String = "Rajesh Sharma",
    val metrics: InfrastructureMetrics? = null,
    val cityNodes: List<CityMarkerNode> = emptyList(),
    val selectedMarkerNode: CityMarkerNode? = null,
    val recentInsights: List<CityInsightEvent> = emptyList(),
    val activeWarnings: List<EarlyWarningAlert> = emptyList(),
    val overallHealthPercent: Float = 98.5f,
    val statusText: String = "NAGARLENS v1.0.0 • Infrastructure Intelligence Network"
)
