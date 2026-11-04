package com.nagarlens.app.core.repository

import com.nagarlens.app.core.model.AuthState
import com.nagarlens.app.core.model.UserRole
import com.nagarlens.app.core.model.UserSession
import kotlinx.coroutines.flow.StateFlow

interface AuthRepository {
    val authState: StateFlow<AuthState>
    suspend fun login(username: String, password: String): Result<UserSession>
    suspend fun register(username: String, email: String, password: String, ward: String): Result<UserSession>
    suspend fun logout()
    suspend fun resetPassword(email: String): Result<Unit>
}
