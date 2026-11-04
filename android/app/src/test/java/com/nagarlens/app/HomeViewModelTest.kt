package com.nagarlens.app

import com.nagarlens.app.core.model.AuthState
import com.nagarlens.app.core.model.ConditionDistribution
import com.nagarlens.app.core.model.DefectDistribution
import com.nagarlens.app.core.model.InfrastructureMetrics
import com.nagarlens.app.core.model.UserRole
import com.nagarlens.app.core.model.UserSession
import com.nagarlens.app.core.repository.AnalyticsRepository
import com.nagarlens.app.core.repository.AuthRepository
import com.nagarlens.app.feature.home.HomeViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class HomeViewModelTest {

    private class FakeAnalyticsRepository : AnalyticsRepository {
        override fun observeMetrics(): Flow<InfrastructureMetrics> = flowOf(
            InfrastructureMetrics(
                totalAssets = 10,
                verifiedAssets = 8,
                pendingReviewAssets = 2,
                unverifiedAssets = 0,
                totalObservations = 20,
                totalEvidenceFiles = 5,
                openIncidentsCount = 2,
                resolvedIncidentsCount = 5,
                averageResolutionTimeHours = 12f
            )
        )

        override fun observeConditionDistribution(): Flow<List<ConditionDistribution>> = flowOf(emptyList())
        override fun observeDefectDistribution(): Flow<List<DefectDistribution>> = flowOf(emptyList())
    }

    private class FakeAuthRepository : AuthRepository {
        private val session = UserSession(
            userId = "U-1",
            username = "auditor",
            fullName = "Rajesh Sharma",
            email = "r@bbmp.gov.in",
            role = UserRole.FIELD_INSPECTOR,
            assignedWard = "Ward 12 — Bangalore Central"
        )
        override val authState: StateFlow<AuthState> = MutableStateFlow(AuthState.Authenticated(session))
        override suspend fun login(username: String, password: String) = Result.success(session)
        override suspend fun register(username: String, email: String, password: String, ward: String) = Result.success(session)
        override suspend fun logout() {}
        override suspend fun resetPassword(email: String) = Result.success(Unit)
    }

    @Test
    fun `initial uiState contains correct product identity`() {
        val viewModel = HomeViewModel(
            analyticsRepository = FakeAnalyticsRepository(),
            authRepository = FakeAuthRepository()
        )
        val state = viewModel.uiState.value

        assertEquals("NAGARLENS", state.appTitle)
        assertEquals("Infrastructure Intelligence Network", state.category)
        assertEquals("Giving the physical world a living memory.", state.tagline)
    }
}
