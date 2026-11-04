package com.nagarlens.app.feature.profile

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

data class ProfileUiState(
    val inspectorName: String = "Field Auditor #402",
    val assignedWard: String = "Ward 12 — Central Drainage Zone",
    val queuedObservationCount: Int = 4,
    val encryptedCacheSizeMb: Double = 18.4,
    val buildVersion: String = "NAGARLENS v0.4.0 (Build 4)"
)

@HiltViewModel
class ProfileViewModel @Inject constructor() : ViewModel() {
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()
}
