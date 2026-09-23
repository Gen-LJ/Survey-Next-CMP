package com.lucilab.surveynext.presentation.screens.respondent.info.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lucilab.surveynext.data.model.SurveyModel
import com.lucilab.surveynext.data.repository.RespondentRepository
import com.lucilab.surveynext.presentation.common.LoadState
import com.lucilab.surveynext.presentation.common.toLoadState
import com.lucilab.surveynext.presentation.navigation.ARG_SURVEY_ID
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SurveyInfoViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: RespondentRepository,
) : ViewModel() {

    private val surveyId: Int = checkNotNull(savedStateHandle[ARG_SURVEY_ID])

    var state by mutableStateOf<LoadState<SurveyModel>>(LoadState.Loading)
        private set

    var isSaved by mutableStateOf(false)
        private set

    var isSaving by mutableStateOf(false)
        private set

    var message by mutableStateOf<String?>(null)
        private set

    init {
        load()
    }

    fun load() {
        state = LoadState.Loading
        viewModelScope.launch {
            // The backend answers 409 with a readable reason when the survey is closed or already answered.
            state = runCatching { repository.survey(surveyId) }.toLoadState()
        }
        viewModelScope.launch {
            runCatching { repository.savedSurveyIds() }.onSuccess { isSaved = surveyId in it }
        }
    }

    fun toggleSaved() {
        if (isSaving) return
        viewModelScope.launch {
            isSaving = true
            val saving = !isSaved
            runCatching {
                if (saving) repository.saveSurvey(surveyId) else repository.removeSavedSurvey(surveyId)
            }.onSuccess {
                isSaved = saving
                message = if (saving) "Saved for later" else "Removed from saved"
            }.onFailure { message = it.message }
            isSaving = false
        }
    }

    fun clearMessage() {
        message = null
    }
}
