package com.lucilab.surveynext.presentation.screens.respondent.answer.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lucilab.surveynext.data.model.QuestionModel
import com.lucilab.surveynext.data.model.QuestionType
import com.lucilab.surveynext.data.model.SurveyModel
import com.lucilab.surveynext.data.repository.RespondentRepository
import com.lucilab.surveynext.presentation.common.LoadState
import com.lucilab.surveynext.presentation.common.toLoadState
import com.lucilab.surveynext.presentation.navigation.ARG_SURVEY_ID
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AnswerSurveyViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: RespondentRepository,
) : ViewModel() {

    private val surveyId: Int = checkNotNull(savedStateHandle[ARG_SURVEY_ID])

    var state by mutableStateOf<LoadState<SurveyModel>>(LoadState.Loading)
        private set

    var questions by mutableStateOf<List<QuestionModel>>(emptyList())
        private set

    var currentIndex by mutableIntStateOf(0)
        private set

    /** Responses by question id, in the backend's shape: always a list of strings. */
    var answers by mutableStateOf<Map<Int, List<String>>>(emptyMap())
        private set

    var isSubmitting by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    /** Points earned, once the submission is accepted. */
    var earnedPoints by mutableStateOf<Int?>(null)
        private set

    val current: QuestionModel? get() = questions.getOrNull(currentIndex)
    val isLast: Boolean get() = currentIndex == questions.lastIndex
    val hasAnswers: Boolean get() = answers.values.any { it.isNotEmpty() }

    init {
        load()
    }

    fun load() {
        state = LoadState.Loading
        viewModelScope.launch {
            val result = runCatching { repository.survey(surveyId) }
            result.onSuccess { survey -> questions = survey.questions.orEmpty().sortedBy { it.position } }
            state = result.toLoadState()
        }
    }

    fun isAnswered(question: QuestionModel): Boolean {
        val responses = answers[question.id].orEmpty()
        return when (question.type) {
            QuestionType.TextInput -> responses.firstOrNull()?.isNotBlank() == true
            else -> responses.isNotEmpty()
        }
    }

    fun selectOption(question: QuestionModel, option: String) {
        val currentResponses = answers[question.id].orEmpty()
        val updated = if (question.allowMultiAnswer) {
            if (option in currentResponses) currentResponses - option else currentResponses + option
        } else {
            listOf(option)
        }
        answers = answers + (question.id to updated)
    }

    fun setRating(question: QuestionModel, rating: Int) {
        answers = answers + (question.id to listOf(rating.toString()))
    }

    fun setText(question: QuestionModel, text: String) {
        answers = answers + (question.id to listOf(text))
    }

    fun next() {
        if (currentIndex < questions.lastIndex) currentIndex++
    }

    /** @return false when already on the first question */
    fun previous(): Boolean {
        if (currentIndex == 0) return false
        currentIndex--
        return true
    }

    fun clearError() {
        errorMessage = null
    }

    fun submit() {
        if (isSubmitting) return
        val unanswered = questions.indexOfFirst { !isAnswered(it) }
        if (unanswered >= 0) {
            currentIndex = unanswered
            errorMessage = "Please answer every question"
            return
        }
        val payload = questions.associate { question ->
            val responses = answers[question.id].orEmpty()
            question.id to if (question.type == QuestionType.TextInput) responses.map { it.trim() } else responses
        }
        viewModelScope.launch {
            isSubmitting = true
            runCatching { repository.submitAnswers(surveyId, payload) }
                .onSuccess { earnedPoints = it.pointsEarned }
                .onFailure { errorMessage = it.message }
            isSubmitting = false
        }
    }
}
