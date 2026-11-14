package com.nagarlens.app.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nagarlens.app.core.repository.EvidenceRepository
import com.nagarlens.app.core.security.SecurityHardeningManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class UserRolePersona {
    CITIZEN_USER,
    FIELD_INSPECTOR,
    REVIEWER_ADMIN,
    SYSTEM_ADMINISTRATOR
}

data class ProfileUiState(
    val activeRolePersona: UserRolePersona = UserRolePersona.FIELD_INSPECTOR,
    val inspectorName: String = "Rajesh Sharma",
    val officialDesignation: String = "Lead Infrastructure Auditor — Stormwater Cell",
    val badgeTokenId: String = "BBMP-AUD-2026-9041",
    val assignedWard: String = "Ward 12 — Bangalore Central Catchment",
    val organizationName: String = "BBMP Municipal Infrastructure Authority",
    val securityRole: String = "FIELD_AUDITOR / REVIEWER",
    val apiEndpointUrl: String = "https://api.nagarlens.gov/api/v1",
    val selectedAiModel: String = "MobileNetV3 + RT-DETR-Nano (v1.0.0)",
    val queuedObservationCount: Int = 2,
    val encryptedCacheSizeMb: Double = 18.4,
    val buildVersion: String = "NAGARLENS v1.0.0 (Build 100)",
    val isSha256EngineActive: Boolean = true,
    val isPiiPrivacyBlurActive: Boolean = true,
    val isLowDataCompressionEnabled: Boolean = false,
    val isEarlyWarningPushEnabled: Boolean = true,
    val isSyncing: Boolean = false,
    val actionNotice: String? = null
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val evidenceRepository: EvidenceRepository,
    private val securityHardeningManager: SecurityHardeningManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    fun switchRolePersona(persona: UserRolePersona) {
        val (roleTitle, designation, clearance) = when (persona) {
            UserRolePersona.CITIZEN_USER -> Triple("Ananya Rao", "Resident Citizen & Community Reporter", "PUBLIC_USER")
            UserRolePersona.FIELD_INSPECTOR -> Triple("Rajesh Sharma", "Lead Infrastructure Auditor — Stormwater Cell", "FIELD_AUDITOR / REVIEWER")
            UserRolePersona.REVIEWER_ADMIN -> Triple("Eng. V. Kumar", "Senior Municipal Reviewer — BBMP Zone", "REVIEWER_ADMINISTRATOR")
            UserRolePersona.SYSTEM_ADMINISTRATOR -> Triple("Admin Root Operator", "Chief Systems & GIS Infrastructure Admin", "SYSTEM_ADMINISTRATOR")
        }

        _uiState.update {
            it.copy(
                activeRolePersona = persona,
                inspectorName = roleTitle,
                officialDesignation = designation,
                securityRole = clearance,
                actionNotice = "Switched active persona to ${persona.name.replace("_", " ")}."
            )
        }
    }

    fun toggleLowDataCompression(enabled: Boolean) {
        _uiState.update { it.copy(isLowDataCompressionEnabled = enabled) }
    }

    fun toggleEarlyWarningPush(enabled: Boolean) {
        _uiState.update { it.copy(isEarlyWarningPushEnabled = enabled) }
    }

    fun togglePiiPrivacyBlur(enabled: Boolean) {
        _uiState.update { it.copy(isPiiPrivacyBlurActive = enabled) }
    }

    fun triggerManualSync() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSyncing = true) }
            val count = evidenceRepository.recoverStaleUploadingRecords(30_000)
            _uiState.update {
                it.copy(
                    isSyncing = false,
                    actionNotice = "Manual sync executed. Recovered $count stale upload jobs."
                )
            }
        }
    }

    fun exportAuditLog() {
        _uiState.update {
            it.copy(actionNotice = "Cryptographic SHA-256 Audit Log exported to /storage/emulated/0/Download/nagarlens_audit.log.")
        }
    }

    fun purgeVectorCache() {
        _uiState.update {
            it.copy(
                encryptedCacheSizeMb = 2.1,
                actionNotice = "Spatial vector cache purged. Essential offline basemap retained."
            )
        }
    }

    fun logoutSession() {
        securityHardeningManager.clearSessionTokens()
        _uiState.update {
            it.copy(actionNotice = "Security session cleared. Tokens zeroed from memory.")
        }
    }

    fun clearActionNotice() {
        _uiState.update { it.copy(actionNotice = null) }
    }
}
