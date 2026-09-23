package com.lucilab.surveynext.presentation.screens.interviewer.manage.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lucilab.surveynext.data.model.CategoryModel
import com.lucilab.surveynext.data.model.QuestionModel
import com.lucilab.surveynext.data.model.QuestionType
import com.lucilab.surveynext.data.model.SurveyModel
import com.lucilab.surveynext.data.repository.AuthRepository
import com.lucilab.surveynext.data.repository.InterviewerRepository
import com.lucilab.surveynext.data.session.SessionManager
import com.lucilab.surveynext.presentation.common.LoadState
import com.lucilab.surveynext.presentation.navigation.ARG_SURVEY_ID
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

/** A question as edited in the sheet, before it is sent. */
data class QuestionDraft(
    val text: String = "",
    val type: QuestionType = QuestionType.MultipleChoice,
    val allowMultiAnswer: Boolean = false,
    val options: List<String> = listOf("", ""),
) {
    val cleanOptions: List<String> get() = options.map { it.trim() }.filter { it.isNotEmpty() }

    /** Null when the draft can be saved. */
    val validationError: String?
        get() = when {
            text.isBlank() -> "Write the question"
            type == QuestionType.MultipleChoice && cleanOptions.size < 2 -> "Add at least two options"
            type == QuestionType.MultipleChoice && cleanOptions.size != cleanOptions.distinct().size ->
                "Options must be different"
            else -> null
        }

    companion object {
        fun from(question: QuestionModel) = QuestionDraft(
            text = question.text,
            type = question.type,
            allowMultiAnswer = question.allowMultiAnswer,
            options = question.options.orEmpty().sortedBy { it.position }.map { it.text }
                .ifEmpty { listOf("", "") },
        )
    }
}

data class SurveyInfoForm(
    val title: String,
    val description: String,
    val minutes: Int,
    val categoryId: Int,
) {
    companion object {
        fun from(survey: SurveyModel) = SurveyInfoForm(survey.title, survey.description, survey.minutes, survey.categoryId)
    }
}

data class PublishForm(
    val expectedAnswers: String = "20",
    val pointsPerAnswer: String = "10",
) {
    val answers: Int get() = expectedAnswers.toIntOrNull() ?: 0
    val points: Int get() = pointsPerAnswer.toIntOrNull() ?: 0
    val totalCost: Long get() = answers.toLong() * points
}

@HiltViewModel
class ManageSurveyViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: InterviewerRepository,
    private val authRepository: AuthRepository,
    sessionManager: SessionManager,
) : ViewModel() {

    val surveyId: Int = checkNotNull(savedStateHandle[ARG_SURVEY_ID])

    var state by mutableStateOf<LoadState<SurveyModel>>(LoadState.Loading)
        private set

    var categories by mutableStateOf<List<CategoryModel>>(emptyList())
        private set

    var balance by mutableStateOf(sessionManager.user.value?.points ?: 0)
        private set

    var infoForm by mutableStateOf<SurveyInfoForm?>(null)
        private set

    var publishForm by mutableStateOf(PublishForm())
        private set

    /** An operation is in flight; action buttons show progress. */
    var isBusy by mutableStateOf(false)
        private set

    var message by mutableStateOf<String?>(null)
        private set

    var isDeleted by mutableStateOf(false)
        private set

    init {
        load()
        viewModelScope.launch {
            sessionManager.user.map { it?.points }.collect { points -> points?.let { balance = it } }
        }
        viewModelScope.launch { runCatching { authRepository.refreshUser() } }
        viewModelScope.launch {
            runCatching { repository.createSurveyForm() }.onSuccess { categories = it.categoryList }
        }
    }

    fun load() {
        state = LoadState.Loading
        viewModelScope.launch { fetch() }
    }

    private suspend fun fetch() {
        runCatching { repository.survey(surveyId) }
            .onSuccess { survey ->
                state = LoadState.Success(survey)
                infoForm = SurveyInfoForm.from(survey)
            }
            .onFailure {
                if (state !is LoadState.Success) state = LoadState.Error(it.message ?: "Couldn't load the survey")
                else message = it.message
            }
    }

    fun clearMessage() {
        message = null
    }

    fun onInfoChange(form: SurveyInfoForm) {
        infoForm = form
    }

    fun onPublishChange(form: PublishForm) {
        publishForm = form.copy(
            expectedAnswers = form.expectedAnswers.filter(Char::isDigit).take(6),
            pointsPerAnswer = form.pointsPerAnswer.filter(Char::isDigit).take(6),
        )
    }

    fun saveInfo() {
        val form = infoForm ?: return
        if (form.title.isBlank() || form.description.isBlank()) {
            message = "Title and description can't be empty"
            return
        }
        perform("Details saved") {
            repository.editSurveyInfo(surveyId, form.title.trim(), form.description.trim(), form.minutes, form.categoryId)
        }
    }

    /** @param onSaved called once the backend accepts the question, to close the editor */
    fun saveQuestion(existing: QuestionModel?, draft: QuestionDraft, onSaved: () -> Unit) {
        draft.validationError?.let {
            message = it
            return
        }
        perform(if (existing == null) "Question added" else "Question updated", onSuccess = onSaved) {
            if (existing == null) {
                repository.addQuestion(surveyId, draft.text.trim(), draft.type, draft.allowMultiAnswer, draft.cleanOptions)
            } else {
                val options = draft.cleanOptions.takeIf { existing.type == QuestionType.MultipleChoice }
                repository.editQuestion(surveyId, existing.id, draft.text.trim(), options)
            }
        }
    }

    fun deleteQuestion(question: QuestionModel) {
        perform("Question deleted") { repository.deleteQuestion(surveyId, question.id) }
    }

    fun publish() {
        val form = publishForm
        perform("Survey published") { repository.publishSurvey(surveyId, form.answers, form.points) }
    }

    fun setPaused(paused: Boolean) {
        perform(if (paused) "Survey paused" else "Survey resumed") { repository.setPaused(surveyId, paused) }
    }

    fun delete() {
        viewModelScope.launch {
            isBusy = true
            runCatching { repository.deleteSurvey(surveyId) }
                .onSuccess { isDeleted = true }
                .onFailure { message = it.message }
            isBusy = false
        }
    }

    private fun perform(successMessage: String, onSuccess: () -> Unit = {}, action: suspend () -> Unit) {
        if (isBusy) return
        viewModelScope.launch {
            isBusy = true
            runCatching { action() }
                .onSuccess {
                    fetch()
                    // Publishing and deleting move points in and out of escrow.
                    launch { runCatching { authRepository.refreshUser() } }
                    onSuccess()
                    message = successMessage
                }
                .onFailure { message = it.message }
            isBusy = false
        }
    }
}
