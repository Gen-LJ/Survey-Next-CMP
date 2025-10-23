package com.lucilab.surveynext.presentation.screens.auth.register

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lucilab.surveynext.presentation.components.button.custombutton.CustomButton

@Composable
fun RegisterScreen(
    onRegister: () -> Unit,
    onLoginClick: () -> Unit,
    viewModel: RegisterViewModel = hiltViewModel()
) {
    val uiState = viewModel.uiState
    val idleState = uiState as? RegisterUIState.Idle
    val snackBarHostState = remember { SnackbarHostState() }

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackBarHostState)
        }) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
                .padding(WindowInsets.ime.asPaddingValues())
                .padding(24.dp)
        ) {
            Text(
                "Register",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.W500
                )
            )

            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = idleState?.form?.name.orEmpty(),
                modifier = Modifier.fillMaxWidth(),
                onValueChange = viewModel::onNameChanged,
                label = { Text("Name") },
                isError = idleState?.form?.nameError != null,
                supportingText = idleState?.form?.nameError?.let { { Text(it) } }
            )


            Spacer(Modifier.height(16.dp))
            CustomButton(
                text = "Register",
                onClick = {},
                modifier = Modifier.fillMaxWidth(),
                isLoading = false
            )


            Spacer(Modifier.height(8.dp))
            TextButton(onClick = onLoginClick) {
                Text("Already have an account? Login")
            }
        }
    }
}

