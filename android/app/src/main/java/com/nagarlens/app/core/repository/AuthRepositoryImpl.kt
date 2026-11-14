package com.nagarlens.app.core.repository

import com.nagarlens.app.core.model.AuthState
import com.nagarlens.app.core.model.UserRole
import com.nagarlens.app.core.model.UserSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor() : AuthRepository {

    // Registered user accounts database in memory / persistent cache
    private val registeredAccounts = mutableMapOf<String, UserSession>(
        "admin" to UserSession(
            userId = "USR-ADMIN-001",
            username = "admin",
            fullName = "Admin Root Operator",
            email = "admin@nagarlens.gov",
            role = UserRole.SYSTEM_ADMINISTRATOR,
            assignedWard = "Ward 12 — Bangalore Central",
            authToken = "jwt_nagarlens_admin_token_v100"
        ),
        "reviewer" to UserSession(
            userId = "USR-REV-002",
            username = "reviewer",
            fullName = "Eng. V. Kumar",
            email = "v.kumar@bbmp.gov.in",
            role = UserRole.MUNICIPAL_ENGINEER,
            assignedWard = "Ward 12 — Bangalore Central",
            authToken = "jwt_nagarlens_reviewer_token_v100"
        ),
        "auditor_central" to UserSession(
            userId = "INSP-00124",
            username = "auditor_central",
            fullName = "Rajesh Sharma",
            email = "r.sharma@bbmp.gov.in",
            role = UserRole.FIELD_INSPECTOR,
            assignedWard = "Ward 12 — Bangalore Central",
            authToken = "jwt_nagarlens_field_token_v100"
        ),
        "citizen" to UserSession(
            userId = "USR-CIT-004",
            username = "citizen",
            fullName = "Ananya Rao",
            email = "ananya.rao@gmail.com",
            role = UserRole.GUEST_AUDITOR,
            assignedWard = "Ward 12 — Bangalore Central",
            authToken = "jwt_nagarlens_citizen_token_v100"
        )
    )

    private val _authState = MutableStateFlow<AuthState>(
        AuthState.Authenticated(registeredAccounts["admin"]!!)
    )
    override val authState: StateFlow<AuthState> = _authState.asStateFlow()

    override suspend fun login(username: String, password: String): Result<UserSession> {
        val key = username.trim().lowercase()
        val session = registeredAccounts[key] ?: UserSession(
            userId = "USR-${UUID.randomUUID().toString().take(6).uppercase()}",
            username = username,
            fullName = username.replaceFirstChar { it.uppercase() },
            email = "$username@nagarlens.gov",
            role = if (key.contains("admin")) UserRole.SYSTEM_ADMINISTRATOR else UserRole.FIELD_INSPECTOR,
            assignedWard = "Ward 12 — Bangalore Central",
            authToken = "jwt_token_${System.currentTimeMillis()}"
        )

        registeredAccounts[key] = session
        _authState.value = AuthState.Authenticated(session)
        return Result.success(session)
    }

    override suspend fun register(
        username: String,
        email: String,
        password: String,
        ward: String
    ): Result<UserSession> {
        if (username.isBlank() || email.isBlank() || password.isBlank()) {
            return Result.failure(IllegalArgumentException("All registration fields are required."))
        }

        val role = when {
            username.lowercase().contains("admin") -> UserRole.SYSTEM_ADMINISTRATOR
            username.lowercase().contains("reviewer") -> UserRole.MUNICIPAL_ENGINEER
            username.lowercase().contains("citizen") -> UserRole.GUEST_AUDITOR
            else -> UserRole.FIELD_INSPECTOR
        }

        val session = UserSession(
            userId = "USR-${UUID.randomUUID().toString().take(6).uppercase()}",
            username = username,
            fullName = username.replaceFirstChar { it.uppercase() },
            email = email,
            role = role,
            assignedWard = ward.ifBlank { "Ward 12 — Bangalore Central" },
            authToken = "jwt_token_${System.currentTimeMillis()}"
        )

        registeredAccounts[username.trim().lowercase()] = session
        _authState.value = AuthState.Authenticated(session)
        return Result.success(session)
    }

    override suspend fun logout() {
        _authState.value = AuthState.Unauthenticated
    }

    override suspend fun resetPassword(email: String): Result<Unit> {
        if (email.isBlank()) return Result.failure(IllegalArgumentException("Email is required."))
        return Result.success(Unit)
    }
}
