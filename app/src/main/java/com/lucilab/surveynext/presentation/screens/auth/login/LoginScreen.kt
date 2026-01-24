package com.lucilab.surveynext.presentation.screens.auth.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lucilab.surveynext.R
import com.lucilab.surveynext.presentation.components.button.custombutton.CustomButton
import com.lucilab.surveynext.presentation.components.textfield.PasswordTextField
import com.lucilab.surveynext.presentation.screens.auth.login.viewmodel.LoginUIState
import com.lucilab.surveynext.presentation.screens.auth.login.viewmodel.LoginViewModel


@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onRegister: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val uiState = viewModel.uiState
    val snackBarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState) {
        when (uiState) {
            is LoginUIState.Success -> {
                onLoginSuccess()
                viewModel.resetToIdle()
            }

            is LoginUIState.Idle -> {
                uiState.errorMessage?.let { message ->
                    snackBarHostState.showSnackbar(message)
                    viewModel.clearError()
                }
            }
        }
    }

    val idleState = uiState as? LoginUIState.Idle

    Scaffold(snackbarHost = { SnackbarHost(hostState = snackBarHostState) }) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
                .padding(WindowInsets.statusBars.asPaddingValues())
                .padding(WindowInsets.ime.asPaddingValues())
                .padding(24.dp)
        ) {

            Spacer(Modifier.height(16.dp))
            Image(
                painter = painterResource(id = R.drawable.app_logo_image),
                contentDescription = null,
                modifier = Modifier.height(90.dp)
            )

            Spacer(Modifier.height(20.dp))

            Text(
                "Welcome",
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(Modifier.height(4.dp))

            Text(
                "Join our Survey Next community and start earning today.",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(Modifier.height(20.dp))

            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = idleState?.form?.email.orEmpty(),
                onValueChange = viewModel::onEmailChange,
                label = { Text("Email") },
                isError = idleState?.form?.emailError != null,
                supportingText = idleState?.form?.emailError?.let { { Text(it) } }
            )

            Spacer(Modifier.height(8.dp))

            PasswordTextField(
                password = idleState?.form?.password.orEmpty(),
                onPasswordChange = viewModel::onPasswordChange,
                isError = idleState?.form?.passwordError != null,
                supportingText = idleState?.form?.passwordError?.let { { Text(it) } }
            )

            Spacer(Modifier.height(24.dp))

            CustomButton(
                text = "Login",
                onClick = viewModel::onLoginClick,
                modifier = Modifier.fillMaxWidth(),
                isLoading = idleState?.isLoading == true
            )

            Spacer(Modifier.height(8.dp))

            TextButton(
                onClick = onRegister,
                modifier = Modifier.fillMaxWidth(),
            ) {
                val annotatedText = buildAnnotatedString {
                    withStyle(
                        style = SpanStyle(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    ) {
                        append("Don't have an account? ")
                    }

                    withStyle(
                        style = SpanStyle(
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    ) {
                        append("Register")
                    }
                }

                Text(
                    text = annotatedText,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

