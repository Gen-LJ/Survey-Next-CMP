package com.lucilab.surveynext.presentation.screens.interviewer.home.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lucilab.surveynext.data.model.InterviewerHomeModel
import com.lucilab.surveynext.data.repository.InterviewerRepository
import com.lucilab.surveynext.data.session.DataChangeNotifier
import com.lucilab.surveynext.presentation.common.LoadState
import com.lucilab.surveynext.presentation.common.toLoadState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InterviewerHomeViewModel @Inject constructor(
    private val repository: InterviewerRepository,
    notifier: DataChangeNotifier,
) : ViewModel() {

    var state by mutableStateOf<LoadState<InterviewerHomeModel>>(LoadState.Loading)
        private set

    var isRefreshing by mutableStateOf(false)
        private set

    init {
        viewModelScope.launch {
            notifier.version.collect { load() }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            isRefreshing = true
            load()
            isRefreshing = false
        }
    }

    fun retry() {
        state = LoadState.Loading
        viewModelScope.launch { load() }
    }

    private suspend fun load() {
        val result = runCatching { repository.home() }
        // Keep showing stale data if a background reload fails.
        if (result.isSuccess || state !is LoadState.Success) {
            state = result.toLoadState()
        }
    }
}
