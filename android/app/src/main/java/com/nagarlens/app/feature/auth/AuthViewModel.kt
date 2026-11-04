package com.nagarlens.app.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nagarlens.app.core.model.AuthState
import com.nagarlens.app.core.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class AuthMode {
    LOGIN,
    REGISTER,
    FORGOT_PASSWORD
}

data class AuthUiState(
    val authMode: AuthMode = AuthMode.LOGIN,
    val usernameInput: String = "",
    val emailInput: String = "",
    val passwordInput: String = "",
    val wardInput: String = "Ward 12 — Bangalore Central",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    val authState: StateFlow<AuthState> = authRepository.authState

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun setAuthMode(mode: AuthMode) {
        _uiState.value = _uiState.value.copy(authMode = mode, errorMessage = null, successMessage = null)
    }

    fun onUsernameChange(value: String) {
        _uiState.value = _uiState.value.copy(usernameInput = value)
    }

    fun onEmailChange(value: String) {
        _uiState.value = _uiState.value.copy(emailInput = value)
    }

    fun onPasswordChange(value: String) {
        _uiState.value = _uiState.value.copy(passwordInput = value)
    }

    fun onWardChange(value: String) {
        _uiState.value = _uiState.value.copy(wardInput = value)
    }

    fun submit() {
        val state = _uiState.value
        _uiState.value = state.copy(isLoading = true, errorMessage = null)

        viewModelScope.launch {
            when (state.authMode) {
                AuthMode.LOGIN -> {
                    val res = authRepository.login(state.usernameInput.ifBlank { "auditor_central" }, state.passwordInput.ifBlank { "password" })
                    res.onFailure { _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = it.message) }
                       .onSuccess { _uiState.value = _uiState.value.copy(isLoading = false, successMessage = "Welcome back, ${it.fullName}!") }
                }
                AuthMode.REGISTER -> {
                    val res = authRepository.register(
                        state.usernameInput.ifBlank { "new_auditor" },
                        state.emailInput.ifBlank { "auditor@bbmp.gov.in" },
                        state.passwordInput.ifBlank { "password" },
                        state.wardInput
                    )
                    res.onFailure { _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = it.message) }
                       .onSuccess { _uiState.value = _uiState.value.copy(isLoading = false, successMessage = "Registration successful!") }
                }
                AuthMode.FORGOT_PASSWORD -> {
                    val res = authRepository.resetPassword(state.emailInput.ifBlank { "auditor@bbmp.gov.in" })
                    res.onFailure { _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = it.message) }
                       .onSuccess { _uiState.value = _uiState.value.copy(isLoading = false, successMessage = "Password reset instructions sent to email.") }
                }
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _uiState.value = AuthUiState()
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(errorMessage = null, successMessage = null)
    }
}
