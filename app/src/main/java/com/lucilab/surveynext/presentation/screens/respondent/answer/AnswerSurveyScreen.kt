package com.lucilab.surveynext.presentation.screens.respondent.answer

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lucilab.surveynext.data.model.QuestionModel
import com.lucilab.surveynext.data.model.QuestionType
import com.lucilab.surveynext.presentation.common.LoadState
import com.lucilab.surveynext.presentation.components.BackTopBar
import com.lucilab.surveynext.presentation.components.button.custombutton.CustomButton
import com.lucilab.surveynext.presentation.components.dialog.ConfirmDialog
import com.lucilab.surveynext.presentation.components.rating.RatingSelector
import com.lucilab.surveynext.presentation.components.rating.ratingLabel
import com.lucilab.surveynext.presentation.components.state.EmptyView
import com.lucilab.surveynext.presentation.screens.loading.LoadingView
import com.lucilab.surveynext.presentation.screens.respondent.answer.viewmodel.AnswerSurveyViewModel

@Composable
fun AnswerSurveyScreen(
    onClose: () -> Unit,
    onSubmitted: (points: Int) -> Unit,
    viewModel: AnswerSurveyViewModel = hiltViewModel(),
) {
    val snackbarHostState = remember { SnackbarHostState() }
    var confirmExit by remember { mutableStateOf(false) }
    val questions = viewModel.questions
    val loaded = viewModel.state is LoadState.Success && questions.isNotEmpty()

    LaunchedEffect(viewModel.earnedPoints) {
        viewModel.earnedPoints?.let(onSubmitted)
    }
    LaunchedEffect(viewModel.errorMessage) {
        viewModel.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    fun requestExit() {
        if (viewModel.hasAnswers) confirmExit = true else onClose()
    }

    BackHandler(enabled = loaded) {
        if (!viewModel.previous()) requestExit()
    }

    Scaffold(
        topBar = {
            Column {
                BackTopBar(
                    title = if (loaded) "Question ${viewModel.currentIndex + 1} of ${questions.size}" else "",
                    onBack = ::requestExit,
                    navigationIcon = Icons.Rounded.Close
                )
                if (loaded) {
                    val progress by animateFloatAsState(
                        (viewModel.currentIndex + 1f) / questions.size,
                        label = "answer-progress"
                    )
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        strokeCap = StrokeCap.Round,
                        trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                        drawStopIndicator = {}
                    )
                }
            }
        },
        bottomBar = {
            val current = viewModel.current
            if (loaded && current != null) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .imePadding()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (viewModel.currentIndex > 0) {
                        OutlinedButton(
                            onClick = { viewModel.previous() },
                            modifier = Modifier.heightIn(min = 54.dp),
                            shape = MaterialTheme.shapes.medium
                        ) { Text("Back") }
                    }
                    CustomButton(
                        text = if (viewModel.isLast) "Submit" else "Next",
                        onClick = { if (viewModel.isLast) viewModel.submit() else viewModel.next() },
                        enabled = viewModel.isAnswered(current),
                        isLoading = viewModel.isSubmitting,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        when (val state = viewModel.state) {
            LoadState.Loading -> LoadingView()
            is LoadState.Error -> Box(
                Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                EmptyView(
                    icon = Icons.Outlined.Block,
                    title = "Can't open this survey",
                    message = state.message,
                    action = { Button(onClick = onClose) { Text("Go back") } }
                )
            }

            is LoadState.Success -> AnimatedContent(
                targetState = viewModel.currentIndex,
                transitionSpec = {
                    val forward = targetState > initialState
                    (slideInHorizontally { if (forward) it / 3 else -it / 3 } + fadeIn()) togetherWith
                        (slideOutHorizontally { if (forward) -it / 3 else it / 3 } + fadeOut())
                },
                modifier = Modifier.padding(padding),
                label = "question"
            ) { index ->
                questions.getOrNull(index)?.let { QuestionPage(it, viewModel) }
            }
        }
    }

    if (confirmExit) {
        ConfirmDialog(
            title = "Leave survey?",
            message = "Your answers so far won't be saved.",
            confirmLabel = "Leave",
            destructive = true,
            onConfirm = {
                confirmExit = false
                onClose()
            },
            onDismiss = { confirmExit = false }
        )
    }
}

@Composable
private fun QuestionPage(question: QuestionModel, viewModel: AnswerSurveyViewModel) {
    val responses = viewModel.answers[question.id].orEmpty()

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(question.text, style = MaterialTheme.typography.headlineSmall)
            Text(
                when (question.type) {
                    QuestionType.MultipleChoice ->
                        if (question.allowMultiAnswer) "Select all that apply" else "Choose one"
                    QuestionType.Rating -> "Rate from 1 to 5 stars"
                    QuestionType.TextInput -> "Answer in your own words"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        when (question.type) {
            QuestionType.MultipleChoice -> Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                question.options.orEmpty().sortedBy { it.position }.forEach { option ->
                    OptionRow(
                        text = option.text,
                        selected = option.text in responses,
                        multi = question.allowMultiAnswer,
                        onClick = { viewModel.selectOption(question, option.text) }
                    )
                }
            }

            QuestionType.Rating -> {
                val rating = responses.firstOrNull()?.toIntOrNull() ?: 0
                Column(
                    Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    RatingSelector(rating = rating, onRatingChange = { viewModel.setRating(question, it) })
                    Text(
                        ratingLabel(rating),
                        style = MaterialTheme.typography.titleMedium,
                        color = if (rating > 0) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            QuestionType.TextInput -> OutlinedTextField(
                value = responses.firstOrNull().orEmpty(),
                onValueChange = { viewModel.setText(question, it) },
                placeholder = { Text("Type your answer") },
                minLines = 5,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun OptionRow(text: String, selected: Boolean, multi: Boolean, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Surface(
        selected = selected,
        onClick = onClick,
        shape = MaterialTheme.shapes.medium,
        color = if (selected) colors.primaryContainer else colors.surfaceContainerLow,
        border = BorderStroke(if (selected) 2.dp else 1.dp, if (selected) colors.primary else colors.outlineVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // The whole row handles the click; the control is decoration.
            if (multi) Checkbox(checked = selected, onCheckedChange = null, modifier = Modifier.padding(12.dp))
            else RadioButton(selected = selected, onClick = null, modifier = Modifier.padding(12.dp))
            Text(
                text,
                style = MaterialTheme.typography.bodyLarge,
                color = if (selected) colors.onPrimaryContainer else colors.onSurface
            )
        }
    }
}
