package com.nagarlens.app.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nagarlens.app.core.model.AuthState
import com.nagarlens.app.core.repository.AnalyticsRepository
import com.nagarlens.app.core.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val analyticsRepository: AnalyticsRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            analyticsRepository.observeMetrics().collect { m ->
                _uiState.value = _uiState.value.copy(metrics = m)
            }
        }
        viewModelScope.launch {
            authRepository.authState.collect { auth ->
                if (auth is AuthState.Authenticated) {
                    _uiState.value = _uiState.value.copy(
                        userName = auth.session.fullName,
                        activeWard = auth.session.assignedWard
                    )
                }
            }
        }
    }
}
