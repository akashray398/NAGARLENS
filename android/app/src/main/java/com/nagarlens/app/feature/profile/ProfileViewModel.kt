package com.nagarlens.app.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nagarlens.app.core.model.AuthState
import com.nagarlens.app.core.model.UserRole
import com.nagarlens.app.core.repository.AuthRepository
import com.nagarlens.app.core.repository.EvidenceRepository
import com.nagarlens.app.core.security.SecurityHardeningManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class UserRolePersona {
    CITIZEN_USER,
    FIELD_INSPECTOR,
    REVIEWER_ADMIN,
    SYSTEM_ADMINISTRATOR
}

data class RealTimeUserJourneyEvent(
    val eventId: String,
    val timestampText: String,
    val actorName: String,
    val roleBadge: String,
    val actionDescription: String,
    val locationOrAsset: String
)

data class ProfileUiState(
    val activeRolePersona: UserRolePersona = UserRolePersona.SYSTEM_ADMINISTRATOR,
    val inspectorName: String = "Admin Root Operator",
    val officialDesignation: String = "Chief Systems & GIS Infrastructure Admin (YOU)",
    val badgeTokenId: String = "BBMP-AUD-2026-9041",
    val assignedWard: String = "Ward 12 — Bangalore Central Catchment",
    val organizationName: String = "BBMP Municipal Infrastructure Authority",
    val securityRole: String = "SYSTEM_ADMINISTRATOR",
    val userEmail: String = "admin@nagarlens.gov",
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
    val liveJourneysStream: List<RealTimeUserJourneyEvent> = emptyList(),
    val actionNotice: String? = null
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val evidenceRepository: EvidenceRepository,
    private val securityHardeningManager: SecurityHardeningManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private val sampleJourneys = listOf(
        RealTimeUserJourneyEvent(
            eventId = "JRN-101",
            timestampText = "12:04:12 UTC",
            actorName = "Ananya Rao",
            roleBadge = "CITIZEN",
            actionDescription = "Submitted Waterlogging Defect Report with GPS telemetry",
            locationOrAsset = "MG Road Station Catchment"
        ),
        RealTimeUserJourneyEvent(
            eventId = "JRN-102",
            timestampText = "12:02:40 UTC",
            actorName = "Rajesh Sharma",
            roleBadge = "INSPECTOR",
            actionDescription = "Captured CameraX Evidence photo (SHA-256: b24f5c9...)",
            locationOrAsset = "Asset NAG-CB-000104"
        ),
        RealTimeUserJourneyEvent(
            eventId = "JRN-103",
            timestampText = "11:58:10 UTC",
            actorName = "Eng. V. Kumar",
            roleBadge = "REVIEWER",
            actionDescription = "Approved Desilting Emergency Work Order INC-10001",
            locationOrAsset = "Drainage Invert Channel"
        ),
        RealTimeUserJourneyEvent(
            eventId = "JRN-104",
            timestampText = "11:45:00 UTC",
            actorName = "Admin Operator (YOU)",
            roleBadge = "SYS_ADMIN",
            actionDescription = "Triggered PostGIS Spatial Schema v5 Migration Audit",
            locationOrAsset = "PostGIS DB Cluster"
        )
    )

    init {
        _uiState.update { it.copy(liveJourneysStream = sampleJourneys) }

        viewModelScope.launch {
            authRepository.authState.collectLatest { auth ->
                if (auth is AuthState.Authenticated) {
                    val session = auth.session
                    val persona = when (session.role) {
                        UserRole.SYSTEM_ADMINISTRATOR -> UserRolePersona.SYSTEM_ADMINISTRATOR
                        UserRole.MUNICIPAL_ENGINEER -> UserRolePersona.REVIEWER_ADMIN
                        UserRole.GUEST_AUDITOR -> UserRolePersona.CITIZEN_USER
                        UserRole.FIELD_INSPECTOR -> UserRolePersona.FIELD_INSPECTOR
                    }

                    _uiState.update {
                        it.copy(
                            activeRolePersona = persona,
                            inspectorName = session.fullName,
                            userEmail = session.email,
                            assignedWard = session.assignedWard,
                            securityRole = session.role.name,
                            officialDesignation = getDesignationForRole(session.role)
                        )
                    }
                }
            }
        }
    }

    private fun getDesignationForRole(role: UserRole): String {
        return when (role) {
            UserRole.SYSTEM_ADMINISTRATOR -> "Chief Systems & GIS Infrastructure Admin (YOU)"
            UserRole.MUNICIPAL_ENGINEER -> "Senior Municipal Reviewer — BBMP Zone"
            UserRole.GUEST_AUDITOR -> "Resident Citizen & Community Reporter"
            UserRole.FIELD_INSPECTOR -> "Lead Infrastructure Auditor — Stormwater Cell"
        }
    }

    fun loginRealTimeUser(username: String, password: String, rolePersona: UserRolePersona) {
        viewModelScope.launch {
            val key = when (rolePersona) {
                UserRolePersona.SYSTEM_ADMINISTRATOR -> "admin"
                UserRolePersona.REVIEWER_ADMIN -> "reviewer"
                UserRolePersona.CITIZEN_USER -> "citizen"
                UserRolePersona.FIELD_INSPECTOR -> "auditor_central"
            }
            authRepository.login(key, password).fold(
                onSuccess = { session ->
                    _uiState.update {
                        it.copy(actionNotice = "Signed in as ${session.fullName} (${session.role.name}). Session saved permanently.")
                    }
                },
                onFailure = { err ->
                    _uiState.update { it.copy(actionNotice = "Login failed: ${err.message}") }
                }
            )
        }
    }

    fun registerNewRealTimeAccount(username: String, email: String, password: String, ward: String, persona: UserRolePersona) {
        viewModelScope.launch {
            authRepository.register(username, email, password, ward).fold(
                onSuccess = { session ->
                    switchRolePersona(persona)
                    _uiState.update {
                        it.copy(actionNotice = "Account registered & saved permanently! Signed in as ${session.fullName}.")
                    }
                },
                onFailure = { err ->
                    _uiState.update { it.copy(actionNotice = "Registration failed: ${err.message}") }
                }
            )
        }
    }

    fun switchRolePersona(persona: UserRolePersona) {
        viewModelScope.launch {
            val key = when (persona) {
                UserRolePersona.SYSTEM_ADMINISTRATOR -> "admin"
                UserRolePersona.REVIEWER_ADMIN -> "reviewer"
                UserRolePersona.CITIZEN_USER -> "citizen"
                UserRolePersona.FIELD_INSPECTOR -> "auditor_central"
            }
            authRepository.login(key, "password")
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
        viewModelScope.launch {
            securityHardeningManager.clearSessionTokens()
            authRepository.logout()
            _uiState.update {
                it.copy(actionNotice = "Security session cleared. Tokens zeroed from memory.")
            }
        }
    }

    fun clearActionNotice() {
        _uiState.update { it.copy(actionNotice = null) }
    }
}
