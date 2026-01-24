package com.lucilab.surveynext.presentation.screens.auth.register
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import com.lucilab.surveynext.presentation.screens.auth.register.view.RegisterIdleView
import com.lucilab.surveynext.presentation.screens.auth.register.viewmodel.RegisterState
import com.lucilab.surveynext.presentation.screens.auth.register.viewmodel.RegisterViewModel
import com.lucilab.surveynext.presentation.screens.error.ErrorView
import com.lucilab.surveynext.presentation.screens.loading.LoadingView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onLoginClick: () -> Unit,
    onRegisterSuccess: ()->Unit,
    viewModel: RegisterViewModel = hiltViewModel()
) {
    val state = viewModel.state
    val snackBarHostState = remember { SnackbarHostState() }
    LaunchedEffect(state) {
        when (state) {
            is RegisterState.Success -> {
                onRegisterSuccess()
                viewModel.resetToInitial()
            }

            is RegisterState.Idle -> {
                state.errorMessage?.let { message ->
                    snackBarHostState.showSnackbar(message)
                    viewModel.clearError()
                }
            }

            is RegisterState.Error -> {}
            is RegisterState.Initial -> {}
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackBarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Register") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                )
            )
        }
    ) { padding ->
        when (val state = viewModel.state) {
            is RegisterState.Initial -> LoadingView("Retrieving Form...")
            is RegisterState.Idle -> RegisterIdleView(
                viewModel = viewModel,
                padding = padding,
                onLoginClick = onLoginClick
            )

            is RegisterState.Error -> ErrorView(
                message = state.errorMessage ?: "Error",
                onRetry = viewModel::reload
            )

            is RegisterState.Success -> LoadingView("Success")
        }
    }
}