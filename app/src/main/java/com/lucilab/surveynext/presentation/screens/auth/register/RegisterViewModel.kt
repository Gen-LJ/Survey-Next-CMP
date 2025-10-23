package com.lucilab.surveynext.presentation.screens.auth.register

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lucilab.surveynext.data.model.CountryModel
import com.lucilab.surveynext.data.model.UserModel
import com.lucilab.surveynext.data.repository.AuthRepository
import com.lucilab.surveynext.data.repository.AuthRepositoryImpl
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
    val snackErrors: String? = null,
)

sealed class RegisterUIState {
    class Initial : RegisterUIState()

    data class Idle(
        val data: List<CountryModel>,
        val form: RegisterFormState = RegisterFormState(),
        val isLoading: Boolean = false,
        val errorMessage: String? = null,
    ) : RegisterUIState()

    data class Error(
        val errorMessage: String? = null
    ) : RegisterUIState()

    data class Success(
        val data: UserModel
    ) : RegisterUIState()
}

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val repository: AuthRepository
) : ViewModel() {
    var uiState by mutableStateOf<RegisterUIState>(RegisterUIState.Initial())
        private set

    init {
        Log.d("DependencyInjection", "ViewModel created with repository hash: ${repository.hashCode()}")
        Log.d("DependencyInjection", "Repository type: ${repository::class.java.simpleName}")

        // If you can access the RestClient from repository, log it too
        if (repository is AuthRepositoryImpl) {
            // You might need to make service public or add a debug method
            Log.d("DependencyInjection", "RestClient hash in repo: ${repository.getServiceHash()}")
        }

        viewModelScope.launch {
            loadData()
        }
    }

    private suspend fun loadData() {
        runCatching {
            repository.getRegisterForm()
        }.onSuccess {
            uiState = RegisterUIState.Idle(data = it)
        }.onFailure {
            uiState = RegisterUIState.Error(errorMessage = it.message)
        }
    }

    fun onNameChanged(name: String) {
        updateForm { it.copy(name = name, nameError = null) }
    }

    fun clearError() {
        uiState = (uiState as? RegisterUIState.Idle)?.copy(errorMessage = null) ?: uiState
    }

    fun resetToInitial() {
        uiState = RegisterUIState.Initial()
    }

    private inline fun updateForm(update: (RegisterFormState) -> RegisterFormState) {
        uiState = (uiState as? RegisterUIState.Idle)?.let { idle ->
            idle.copy(form = update(idle.form))
        } ?: uiState
    }

    private inline fun updateIdleState(update: (RegisterUIState.Idle) -> RegisterUIState.Idle) {
        uiState = (uiState as? RegisterUIState.Idle)?.let(update) ?: uiState
    }
}