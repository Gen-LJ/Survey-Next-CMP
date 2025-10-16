package com.lucilab.surveynext.presentation.screens.auth.login

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lucilab.surveynext.data.model.UserModel
import com.lucilab.surveynext.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LoginFormState(
    val email: String = "",
    val password: String = "",
    val emailError: String? = null,
    val passwordError: String? = null
)

sealed class LoginUIState {
    data class Idle(
        val form: LoginFormState = LoginFormState(),
        val isLoading: Boolean = false,
        val errorMessage: String? = null // backend error (wrong password, etc.)
    ) : LoginUIState()

    data class Success(
        val data: UserModel
    ) : LoginUIState()
}


@HiltViewModel
class LoginViewModel @Inject constructor(
    private val repository: AuthRepository
) : ViewModel() {

    var uiState by mutableStateOf<LoginUIState>(LoginUIState.Idle())
        private set

    fun onEmailChange(newEmail: String) {
        updateForm { it.copy(email = newEmail, emailError = null) }
    }

    fun onPasswordChange(newPassword: String) {
        updateForm { it.copy(password = newPassword, passwordError = null) }
    }

    fun onLoginClick() {
        val idleState = uiState as? LoginUIState.Idle ?: return

        if (!validateForm(idleState.form)) return

        updateIdleState { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            try {
                val userData = repository.login(
                    idleState.form.email,
                    idleState.form.password
                )
                uiState = LoginUIState.Success(userData.user)
            } catch (e: Exception) {
                updateIdleState {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message
                    )
                }

            }
        }
    }

    private fun validateForm(form: LoginFormState): Boolean {
        var emailError: String? = null
        var passwordError: String? = null

        if (form.email.isEmpty()) emailError = "Email cannot be empty"
        else if (!form.email.contains("@")) emailError = "Invalid email format"

        if (form.password.isEmpty()) passwordError = "Password cannot be empty"

        if (emailError != null || passwordError != null) {
            updateForm { it.copy(emailError = emailError, passwordError = passwordError) }
            return false
        }

        return true
    }

    fun clearError() {
        uiState = (uiState as? LoginUIState.Idle)?.copy(errorMessage = null) ?: uiState
    }

    fun resetToIdle() {
        uiState = LoginUIState.Idle()
    }

    private inline fun updateForm(update: (LoginFormState) -> LoginFormState) {
        uiState = (uiState as? LoginUIState.Idle)?.let { idle ->
            idle.copy(form = update(idle.form))
        } ?: uiState
    }

    private inline fun updateIdleState(update: (LoginUIState.Idle) -> LoginUIState.Idle) {
        uiState = (uiState as? LoginUIState.Idle)?.let(update) ?: uiState
    }

}


