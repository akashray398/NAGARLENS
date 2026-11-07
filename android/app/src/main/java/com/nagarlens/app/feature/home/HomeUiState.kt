package com.nagarlens.app.feature.home

import com.nagarlens.app.core.model.InfrastructureMetrics

data class HomeUiState(
    val appTitle: String = "NAGARLENS",
    val category: String = "Infrastructure Intelligence Network",
    val tagline: String = "Giving the physical world a living memory.",
    val activeDomain: String = "Urban drainage failures & localized waterlogging",
    val activeWard: String = "Ward 12 — Bangalore Central",
    val userName: String = "Rajesh Sharma",
    val metrics: InfrastructureMetrics? = null,
    val statusText: String = "NAGARLENS v1.0.0 • Infrastructure Intelligence Network"
)
