package com.nagarlens.app

import com.nagarlens.app.feature.activity.ActivityViewModel
import com.nagarlens.app.feature.capture.CaptureViewModel
import com.nagarlens.app.feature.map.MapViewModel
import com.nagarlens.app.feature.profile.ProfileViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class FeatureViewModelsTest {

    @Test
    fun `mapViewModel initializes state with active catchment bounds`() {
        val viewModel = MapViewModel()
        val state = viewModel.uiState.value
        assertNotNull(state.activeWard)
        assertEquals(4, state.activeFilters.size)

        viewModel.selectFilter("Catch Basins")
        assertEquals("Catch Basins", viewModel.uiState.value.selectedFilter)
    }

    @Test
    fun `captureViewModel initializes state with GNSS lock metadata`() {
        val viewModel = CaptureViewModel()
        val state = viewModel.uiState.value
        assertNotNull(state.gpsAccuracy)
        assertNotNull(state.cryptoHashNotice)
    }

    @Test
    fun `activityViewModel initializes state with observation timeline items`() {
        val viewModel = ActivityViewModel()
        val state = viewModel.uiState.value
        assertEquals(3, state.recentActivities.size)

        viewModel.selectFilter("Observations")
        assertEquals("Observations", viewModel.uiState.value.activeFilter)
    }

    @Test
    fun `profileViewModel initializes state with auditor identity`() {
        val viewModel = ProfileViewModel()
        val state = viewModel.uiState.value
        assertNotNull(state.inspectorName)
        assertNotNull(state.assignedWard)
    }
}
