package com.nagarlens.app

import com.nagarlens.app.feature.home.HomeViewModel
import org.junit.Assert.assertEquals
import org.junit.Test

class HomeViewModelTest {

    @Test
    fun `initial uiState contains correct product identity`() {
        val viewModel = HomeViewModel()
        val state = viewModel.uiState.value

        assertEquals("NAGARLENS", state.appTitle)
        assertEquals("Infrastructure Intelligence Network", state.category)
        assertEquals("Giving the physical world a living memory.", state.tagline)
        assertEquals("Urban drainage failures & waterlogging", state.activeDomain)
    }
}
