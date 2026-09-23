package com.lucilab.surveynext.presentation.screens.profile.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lucilab.surveynext.data.model.UserModel
import com.lucilab.surveynext.data.repository.AuthRepository
import com.lucilab.surveynext.data.session.DataChangeNotifier
import com.lucilab.surveynext.data.session.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    sessionManager: SessionManager,
    private val authRepository: AuthRepository,
    notifier: DataChangeNotifier,
) : ViewModel() {

    val user: StateFlow<UserModel?> = sessionManager.user

    var location by mutableStateOf<String?>(null)
        private set

    var isRefreshing by mutableStateOf(false)
        private set

    init {
        viewModelScope.launch {
            // Points move whenever surveys are answered or published.
            notifier.version.collect { runCatching { authRepository.refreshUser() } }
        }
        viewModelScope.launch {
            user.value?.let { location = authRepository.locationName(it.countryId, it.regionId) }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            isRefreshing = true
            runCatching { authRepository.refreshUser() }
            isRefreshing = false
        }
    }
}
