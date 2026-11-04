package com.nagarlens.app.core.model

enum class UserRole {
    FIELD_INSPECTOR,
    MUNICIPAL_ENGINEER,
    SYSTEM_ADMINISTRATOR,
    GUEST_AUDITOR
}

data class UserSession(
    val userId: String,
    val username: String,
    val fullName: String,
    val email: String,
    val role: UserRole,
    val assignedWard: String,
    val authToken: String? = null,
    val isLoggedIn: Boolean = true
)

sealed class AuthState {
    data object Unauthenticated : AuthState()
    data object Loading : AuthState()
    data class Authenticated(val session: UserSession) : AuthState()
    data class Error(val message: String) : AuthState()
}
