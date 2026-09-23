package com.lucilab.surveynext.presentation.screens.respondent.details.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lucilab.surveynext.data.model.AnswerDetailsModel
import com.lucilab.surveynext.data.repository.RespondentRepository
import com.lucilab.surveynext.presentation.common.LoadState
import com.lucilab.surveynext.presentation.common.toLoadState
import com.lucilab.surveynext.presentation.navigation.ARG_SURVEY_ID
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AnswerDetailsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: RespondentRepository,
) : ViewModel() {

    private val surveyId: Int = checkNotNull(savedStateHandle[ARG_SURVEY_ID])

    var state by mutableStateOf<LoadState<AnswerDetailsModel>>(LoadState.Loading)
        private set

    init {
        load()
    }

    fun load() {
        state = LoadState.Loading
        viewModelScope.launch { state = runCatching { repository.answerDetails(surveyId) }.toLoadState() }
    }
}
