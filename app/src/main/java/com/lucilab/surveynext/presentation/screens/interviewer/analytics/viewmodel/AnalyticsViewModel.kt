package com.lucilab.surveynext.presentation.screens.interviewer.analytics.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lucilab.surveynext.data.model.AnalyticsModel
import com.lucilab.surveynext.data.repository.InterviewerRepository
import com.lucilab.surveynext.presentation.common.LoadState
import com.lucilab.surveynext.presentation.common.toLoadState
import com.lucilab.surveynext.presentation.navigation.ARG_SURVEY_ID
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: InterviewerRepository,
) : ViewModel() {

    private val surveyId: Int = checkNotNull(savedStateHandle[ARG_SURVEY_ID])

    var state by mutableStateOf<LoadState<AnalyticsModel>>(LoadState.Loading)
        private set

    var isRefreshing by mutableStateOf(false)
        private set

    init {
        load()
    }

    fun load() {
        state = LoadState.Loading
        viewModelScope.launch { state = runCatching { repository.analytics(surveyId) }.toLoadState() }
    }

    fun refresh() {
        viewModelScope.launch {
            isRefreshing = true
            runCatching { repository.analytics(surveyId) }.onSuccess { state = LoadState.Success(it) }
            isRefreshing = false
        }
    }
}
