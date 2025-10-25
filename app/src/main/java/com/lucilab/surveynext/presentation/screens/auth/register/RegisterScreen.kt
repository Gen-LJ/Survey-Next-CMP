package com.lucilab.surveynext.presentation.screens.auth.register

import androidx.compose.runtime.*
import androidx.hilt.navigation.compose.hiltViewModel
import com.lucilab.surveynext.presentation.screens.auth.register.view.RegisterIdleView
import com.lucilab.surveynext.presentation.screens.error.ErrorView
import com.lucilab.surveynext.presentation.screens.loading.LoadingView

@Composable
fun RegisterScreen(
    onRegister: () -> Unit,
    onLoginClick: () -> Unit,
    viewModel: RegisterViewModel = hiltViewModel()
) {
    val state = viewModel.state

    when (state) {
        is RegisterState.Initial -> LoadingView(loadingInfo = "Retrieving Register Form...")
        is RegisterState.Idle -> RegisterIdleView(
            formState = state.form,
            countries = state.data,
            isLoading = state.isLoading,
            onNameChanged = viewModel::onNameChanged,
            onEmailChanged = viewModel::onEmailChanged,
            onPasswordChanged = viewModel::onPasswordChanged,
            onConfirmPasswordChanged = viewModel::onConfirmPasswordChanged,
            onLoginClick = onLoginClick
        )

        is RegisterState.Error -> ErrorView(
            message = state.errorMessage ?: "Something went wrong.",
            onRetry = viewModel::reload
        )

        is RegisterState.Success -> onRegister()
    }
}

