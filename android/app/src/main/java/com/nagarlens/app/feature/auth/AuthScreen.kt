package com.nagarlens.app.feature.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.navigation.compose.hiltViewModel
import com.nagarlens.app.core.designsystem.component.NagarlensCard
import com.nagarlens.app.core.designsystem.component.NagarlensPrimaryButton
import com.nagarlens.app.core.designsystem.component.NagarlensSectionHeader
import com.nagarlens.app.core.designsystem.theme.NagarlensSpacing
import com.nagarlens.app.core.model.AuthState

@Composable
fun AuthScreen(
    viewModel: AuthViewModel = hiltViewModel(),
    onAuthenticated: () -> Unit = {}
) {
    val authState by viewModel.authState.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(NagarlensSpacing.medium),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "NAGARLENS",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Infrastructure Intelligence Network",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.secondary,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = "\"Giving the physical world a living memory.\"",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(vertical = NagarlensSpacing.small)
        )

        Spacer(modifier = Modifier.height(NagarlensSpacing.medium))

        NagarlensCard {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(NagarlensSpacing.small)
            ) {
                when (authState) {
                    is AuthState.Authenticated -> {
                        val session = (authState as AuthState.Authenticated).session
                        Text(
                            text = "Active Session Identified",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Text(text = "User: ${session.fullName} (${session.username})")
                        Text(text = "Role: ${session.role.name}")
                        Text(text = "Ward: ${session.assignedWard}")

                        Spacer(modifier = Modifier.height(NagarlensSpacing.small))

                        NagarlensPrimaryButton(
                            onClick = onAuthenticated,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("ENTER SYSTEM SHELL")
                        }

                        TextButton(
                            onClick = { viewModel.logout() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("SIGN OUT / SWITCH USER")
                        }
                    }
                    else -> {
                        NagarlensSectionHeader(
                            title = when (uiState.authMode) {
                                AuthMode.LOGIN -> "Field Auditor Sign In"
                                AuthMode.REGISTER -> "Inspector Registration"
                                AuthMode.FORGOT_PASSWORD -> "Reset Password Credentials"
                            }
                        )

                        if (uiState.authMode != AuthMode.FORGOT_PASSWORD) {
                            OutlinedTextField(
                                value = uiState.usernameInput,
                                onValueChange = { viewModel.onUsernameChange(it) },
                                label = { Text("Username / Badge ID") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                        }

                        if (uiState.authMode == AuthMode.REGISTER || uiState.authMode == AuthMode.FORGOT_PASSWORD) {
                            OutlinedTextField(
                                value = uiState.emailInput,
                                onValueChange = { viewModel.onEmailChange(it) },
                                label = { Text("Municipal Email") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                        }

                        if (uiState.authMode != AuthMode.FORGOT_PASSWORD) {
                            OutlinedTextField(
                                value = uiState.passwordInput,
                                onValueChange = { viewModel.onPasswordChange(it) },
                                label = { Text("Password") },
                                visualTransformation = PasswordVisualTransformation(),
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                        }

                        if (uiState.authMode == AuthMode.REGISTER) {
                            OutlinedTextField(
                                value = uiState.wardInput,
                                onValueChange = { viewModel.onWardChange(it) },
                                label = { Text("Assigned Municipal Ward") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                        }

                        Spacer(modifier = Modifier.height(NagarlensSpacing.small))

                        if (uiState.isLoading) {
                            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                        } else {
                            NagarlensPrimaryButton(
                                onClick = { viewModel.submit() },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = when (uiState.authMode) {
                                        AuthMode.LOGIN -> "SIGN IN"
                                        AuthMode.REGISTER -> "REGISTER INSPECTOR"
                                        AuthMode.FORGOT_PASSWORD -> "SEND RESET LINK"
                                    }
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            if (uiState.authMode == AuthMode.LOGIN) {
                                TextButton(onClick = { viewModel.setAuthMode(AuthMode.REGISTER) }) {
                                    Text("Register")
                                }
                                TextButton(onClick = { viewModel.setAuthMode(AuthMode.FORGOT_PASSWORD) }) {
                                    Text("Forgot Password?")
                                }
                            } else {
                                TextButton(onClick = { viewModel.setAuthMode(AuthMode.LOGIN) }) {
                                    Text("Back to Sign In")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    uiState.errorMessage?.let { err ->
        AlertDialog(
            onDismissRequest = { viewModel.clearMessages() },
            title = { Text("Authentication Notice") },
            text = { Text(err) },
            confirmButton = {
                TextButton(onClick = { viewModel.clearMessages() }) {
                    Text("OK")
                }
            }
        )
    }

    uiState.successMessage?.let { msg ->
        AlertDialog(
            onDismissRequest = { viewModel.clearMessages() },
            title = { Text("Success") },
            text = { Text(msg) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearMessages()
                        onAuthenticated()
                    }
                ) {
                    Text("PROCEED")
                }
            }
        )
    }
}
