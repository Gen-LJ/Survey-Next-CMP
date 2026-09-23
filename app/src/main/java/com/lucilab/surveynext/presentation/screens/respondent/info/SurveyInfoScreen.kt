package com.lucilab.surveynext.presentation.screens.respondent.info

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Stars
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lucilab.surveynext.data.model.QuestionType
import com.lucilab.surveynext.data.model.SurveyModel
import com.lucilab.surveynext.presentation.common.LoadState
import com.lucilab.surveynext.presentation.common.formatPoints
import com.lucilab.surveynext.presentation.common.pluralize
import com.lucilab.surveynext.presentation.components.BackTopBar
import com.lucilab.surveynext.presentation.components.button.custombutton.CustomButton
import com.lucilab.surveynext.presentation.components.state.EmptyView
import com.lucilab.surveynext.presentation.screens.interviewer.manage.view.icon
import com.lucilab.surveynext.presentation.screens.loading.LoadingView
import com.lucilab.surveynext.presentation.screens.respondent.info.viewmodel.SurveyInfoViewModel

@Composable
fun SurveyInfoScreen(
    onBack: () -> Unit,
    onStart: (Int) -> Unit,
    viewModel: SurveyInfoViewModel = hiltViewModel(),
) {
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(viewModel.message) {
        viewModel.message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }
    val survey = (viewModel.state as? LoadState.Success)?.data

    Scaffold(
        topBar = {
            BackTopBar("", onBack = onBack, actions = {
                if (survey != null) {
                    IconButton(onClick = viewModel::toggleSaved, enabled = !viewModel.isSaving) {
                        Icon(
                            if (viewModel.isSaved) Icons.Rounded.Bookmark else Icons.Outlined.BookmarkBorder,
                            contentDescription = if (viewModel.isSaved) "Remove from saved" else "Save for later",
                            tint = if (viewModel.isSaved) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            })
        },
        bottomBar = {
            if (survey != null) {
                Surface(color = MaterialTheme.colorScheme.background) {
                    CustomButton(
                        text = "Start survey",
                        icon = Icons.AutoMirrored.Rounded.ArrowForward,
                        onClick = { onStart(survey.id) },
                        enabled = survey.questions.orEmpty().isNotEmpty(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(16.dp)
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
                    title = "Survey unavailable",
                    message = state.message,
                    action = { Button(onClick = onBack) { Text("Go back") } }
                )
            }

            is LoadState.Success -> SurveyInfoContent(state.data, Modifier.padding(padding))
        }
    }
}

@Composable
private fun SurveyInfoContent(survey: SurveyModel, modifier: Modifier = Modifier) {
    val questions = survey.questions.orEmpty()
    val typeCounts = questions.groupingBy { it.type }.eachCount()

    Column(
        modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                survey.categoryName.uppercase(),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Text(survey.title, style = MaterialTheme.typography.headlineMedium)
            Text(
                survey.description,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            InfoTile(Icons.Outlined.Schedule, "${survey.minutes} min", "to complete", Modifier.weight(1f))
            InfoTile(
                Icons.AutoMirrored.Outlined.HelpOutline, questions.size.toString(),
                if (questions.size == 1) "question" else "questions", Modifier.weight(1f)
            )
            InfoTile(
                Icons.Outlined.Stars, "+${formatPoints(survey.pointsPerAnswer)}", "points",
                Modifier.weight(1f), accent = MaterialTheme.colorScheme.tertiary
            )
        }

        Surface(shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.surfaceContainerLow) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row {
                    Text("Spots filling up", style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                    Text(
                        "${pluralize(survey.spotsLeft, "spot")} left",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                LinearProgressIndicator(
                    progress = { survey.progress },
                    modifier = Modifier.fillMaxWidth(),
                    strokeCap = StrokeCap.Round,
                    trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                    drawStopIndicator = {}
                )
                Text(
                    "Points are paid to the first ${survey.expectedAnswerCounts} people who finish.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (typeCounts.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("What to expect", style = MaterialTheme.typography.titleMedium)
                QuestionType.entries.forEach { type ->
                    val count = typeCounts[type] ?: return@forEach
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Icon(type.icon(), contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Text(pluralize(count, type.noun()), style = MaterialTheme.typography.bodyLarge)
                    }
                }
                Text(
                    "Every question needs an answer before you can submit.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun InfoTile(
    icon: ImageVector,
    value: String,
    label: String,
    modifier: Modifier = Modifier,
    accent: Color = MaterialTheme.colorScheme.primary,
) {
    Surface(modifier = modifier, shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.surfaceContainerLow) {
        Column(
            Modifier.padding(vertical = 14.dp, horizontal = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(22.dp))
            Text(value, style = MaterialTheme.typography.titleLarge)
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

private fun QuestionType.noun() = when (this) {
    QuestionType.MultipleChoice -> "multiple-choice question"
    QuestionType.Rating -> "star rating"
    QuestionType.TextInput -> "written answer"
}
