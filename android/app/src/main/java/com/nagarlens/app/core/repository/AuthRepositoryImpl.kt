package com.nagarlens.app.core.repository

import com.nagarlens.app.core.model.AuthState
import com.nagarlens.app.core.model.UserRole
import com.nagarlens.app.core.model.UserSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor() : AuthRepository {

    private val defaultSession = UserSession(
        userId = "INSP-00124",
        username = "auditor_central",
        fullName = "Rajesh Sharma",
        email = "r.sharma@bbmp.gov.in",
        role = UserRole.FIELD_INSPECTOR,
        assignedWard = "Ward 12 — Bangalore Central",
        authToken = "jwt_nagarlens_field_token_v02"
    )

    private val _authState = MutableStateFlow<AuthState>(AuthState.Authenticated(defaultSession))
    override val authState: StateFlow<AuthState> = _authState.asStateFlow()

    override suspend fun login(username: String, password: String): Result<UserSession> {
        if (username.isBlank() || password.isBlank()) {
            return Result.failure(IllegalArgumentException("Username and password are required."))
        }
        val session = defaultSession.copy(username = username)
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
        val session = defaultSession.copy(
            username = username,
            email = email,
            assignedWard = ward.ifBlank { "Ward 12 — Bangalore Central" }
        )
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
