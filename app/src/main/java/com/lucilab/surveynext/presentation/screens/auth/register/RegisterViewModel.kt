package com.lucilab.surveynext.presentation.screens.auth.register

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lucilab.surveynext.data.model.CountryModel
import com.lucilab.surveynext.data.model.UserModel
import com.lucilab.surveynext.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RegisterFormState(
    val name: String? = null,
    val email: String? = null,
    val password: String? = null,
    val confirmPassword: String? = null,
    val countyId: Int? = null,
    val regionId: Int? = null,
    val nameError: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null,
)

sealed class RegisterState {
    class Initial : RegisterState()

    data class Idle(
        val data: List<CountryModel>,
        val form: RegisterFormState = RegisterFormState(),
        val isLoading: Boolean = false,
        val errorMessage: String? = null,
    ) : RegisterState()

    data class Error(
        val errorMessage: String? = null
    ) : RegisterState()

    data class Success(
        val data: UserModel
    ) : RegisterState()
}

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val repository: AuthRepository
) : ViewModel() {
    var state by mutableStateOf<RegisterState>(RegisterState.Initial())
        private set

    init {
        viewModelScope.launch {
            loadData()
        }
    }

    fun reload() {
        resetToInitial()
        viewModelScope.launch {
            loadData()
        }
    }

    private suspend fun loadData() {
        runCatching {
            repository.getRegisterForm()
        }.onSuccess {
            state = RegisterState.Idle(data = it)
        }.onFailure {
            state = RegisterState.Error(errorMessage = it.message)
        }
    }

    fun onNameChanged(name: String) {
        updateForm { it.copy(name = name, nameError = null) }
    }

    fun onEmailChanged(email: String) {
        updateForm { it.copy(email = email, emailError = null) }
    }

    fun onPasswordChanged(password: String) {
        updateForm { it.copy(password = password, passwordError = null) }
    }

    fun onConfirmPasswordChanged(confirmPassword: String) {
        updateForm { it.copy(confirmPassword = confirmPassword, confirmPasswordError = null) }
    }

    fun clearError() {
        state = (state as? RegisterState.Idle)?.copy(errorMessage = null) ?: state
    }

    fun resetToInitial() {
        state = RegisterState.Initial()

    }

    private inline fun updateForm(update: (RegisterFormState) -> RegisterFormState) {
        state = (state as? RegisterState.Idle)?.let { idle ->
            idle.copy(form = update(idle.form))
        } ?: state
    }

    private inline fun updateIdleState(update: (RegisterState.Idle) -> RegisterState.Idle) {
        state = (state as? RegisterState.Idle)?.let(update) ?: state
    }
}