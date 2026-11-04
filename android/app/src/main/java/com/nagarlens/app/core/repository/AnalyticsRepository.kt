package com.nagarlens.app.core.repository

import com.nagarlens.app.core.model.ConditionDistribution
import com.nagarlens.app.core.model.DefectDistribution
import com.nagarlens.app.core.model.InfrastructureMetrics
import kotlinx.coroutines.flow.Flow

interface AnalyticsRepository {
    fun observeMetrics(): Flow<InfrastructureMetrics>
    fun observeConditionDistribution(): Flow<List<ConditionDistribution>>
    fun observeDefectDistribution(): Flow<List<DefectDistribution>>
}
