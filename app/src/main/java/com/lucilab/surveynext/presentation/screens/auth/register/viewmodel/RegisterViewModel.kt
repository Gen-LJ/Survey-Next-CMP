package com.lucilab.surveynext.presentation.screens.auth.register.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lucilab.surveynext.data.model.CountryModel
import com.lucilab.surveynext.data.model.RegionModel
import com.lucilab.surveynext.data.model.UserModel
import com.lucilab.surveynext.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RegisterFormState(
    val name: String? = null,
    val email: String? = null,
    val selectedCountry: CountryModel? = null,
    val availableRegions: List<RegionModel> = emptyList(),
    val selectedRegion: RegionModel? = null,
    val password: String? = null,
    val confirmPassword: String? = null,

    val nameError: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null,
    val countryError: String? = null,
    val regionError: String? = null,
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
            println("Countries: $it")
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

    fun onCountrySelected(country: CountryModel) {
        updateForm {
            it.copy(
                selectedCountry = country,
                availableRegions = country.regions,
                selectedRegion = null,
                countryError = null,
            )
        }
    }

    fun onRegionSelected(region: RegionModel) {
        updateForm {
            it.copy(
                selectedRegion = region,
                regionError = null
            )
        }
    }

    fun register() {
        val currentState = state as? RegisterState.Idle ?: return
        val form = currentState.form

        val validatedForm = validateForm(currentState.form)

        val hasErrors = listOf(
            validatedForm.nameError, validatedForm.emailError,
            validatedForm.passwordError, validatedForm.confirmPasswordError,
            validatedForm.countryError, validatedForm.regionError
        ).any { it != null }

        if (hasErrors) {
            updateIdleState { it.copy(form = validatedForm) }
            return
        }

        val name = form.name ?: return
        val email = form.email ?: return
        val password = form.password ?: return
        val countryId = form.selectedCountry?.id ?: return
        val regionId = form.selectedRegion?.id ?: return

        viewModelScope.launch {
            updateIdleState { it.copy(isLoading = true, errorMessage = null) }

            runCatching {
                repository.register(
                    name = name,
                    email = email,
                    password = password,
                    role = "interviewer",
                    regionId = regionId,
                    countryId = countryId,
                )
            }.onSuccess { user ->
                state = RegisterState.Success(data = user)
            }.onFailure { error ->
                updateIdleState {
                    it.copy(isLoading = false, errorMessage = error.message)
                }
            }
        }
    }

    fun clearError() {
        state = (state as? RegisterState.Idle)?.copy(errorMessage = null) ?: state
    }

    fun resetToInitial() {
        state = RegisterState.Initial()
    }

    private fun validateForm(form: RegisterFormState): RegisterFormState {
        return form.copy(
            nameError = if (form.name.isNullOrBlank()) "Name is required" else null,
            emailError = if (form.email.isNullOrBlank()) "Email is required" else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(
                    form.email
                ).matches()
            ) "Invalid email" else null,
            countryError = if (form.selectedCountry == null) "Please select a country" else null,
            regionError = if (form.selectedRegion == null) "Please select a region" else null,
            passwordError = if (form.password.isNullOrBlank()) "Password is required" else if (form.password.length < 6) "Password too short" else null,
            confirmPasswordError = if (form.confirmPassword != form.password) "Passwords do not match" else null
        )
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