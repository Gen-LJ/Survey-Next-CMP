package com.lucilab.surveynext.presentation.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lucilab.surveynext.data.model.UserModel
import com.lucilab.surveynext.data.repository.AuthRepository
import com.lucilab.surveynext.data.session.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SessionViewModel @Inject constructor(
    sessionManager: SessionManager,
    private val authRepository: AuthRepository,
) : ViewModel() {

    /** Null once signed out, including when the backend rejects the token. */
    val user: StateFlow<UserModel?> = sessionManager.user

    init {
        // Validate a restored session and pick up point changes made elsewhere.
        if (user.value != null) {
            viewModelScope.launch { runCatching { authRepository.refreshUser() } }
        }
    }

    fun logout() = authRepository.logout()
}
