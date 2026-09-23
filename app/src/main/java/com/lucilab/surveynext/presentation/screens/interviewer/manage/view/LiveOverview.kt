package com.lucilab.surveynext.presentation.screens.interviewer.manage.view

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import com.lucilab.surveynext.data.model.SurveyModel
import com.lucilab.surveynext.data.model.SurveyState
import com.lucilab.surveynext.presentation.common.formatDate
import com.lucilab.surveynext.presentation.common.formatPoints
import com.lucilab.surveynext.presentation.components.SectionHeader
import com.lucilab.surveynext.presentation.components.survey.StateBadge

/** A survey that is live, paused or completed: read-only, with progress and controls. */
@Composable
fun LiveOverview(
    survey: SurveyModel,
    isBusy: Boolean,
    onOpenAnalytics: () -> Unit,
    onSetPaused: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val questions = survey.questions.orEmpty().sortedBy { it.position }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                StateBadge(survey.surveyState)
                Text(survey.title, style = MaterialTheme.typography.headlineSmall)
                Text(
                    survey.description,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "${survey.categoryName} · ${survey.minutes} min · published ${formatDate(survey.publishedAt)}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item { ProgressCard(survey) }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onOpenAnalytics,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 52.dp),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Icon(Icons.Rounded.BarChart, contentDescription = null)
                    Text("View responses", modifier = Modifier.padding(start = 8.dp))
                }
                when (survey.surveyState) {
                    SurveyState.Published -> FilledTonalButton(
                        onClick = { onSetPaused(true) },
                        enabled = !isBusy,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 52.dp),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Icon(Icons.Rounded.Pause, contentDescription = null)
                        Text("Pause survey", modifier = Modifier.padding(start = 8.dp))
                    }

                    SurveyState.Paused -> FilledTonalButton(
                        onClick = { onSetPaused(false) },
                        enabled = !isBusy,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 52.dp),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Icon(Icons.Rounded.PlayArrow, contentDescription = null)
                        Text("Resume survey", modifier = Modifier.padding(start = 8.dp))
                    }

                    else -> Unit
                }
            }
        }

        item { SectionHeader("Questions") }
        itemsIndexed(questions, key = { _, q -> q.id }) { index, question ->
            QuestionCard(number = index + 1, question = question)
        }
    }
}

@Composable
private fun ProgressCard(survey: SurveyModel) {
    val progress by animateFloatAsState(survey.progress, label = "progress")

    Surface(
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.size(112.dp),
                    strokeWidth = 10.dp,
                    strokeCap = StrokeCap.Round,
                    trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("${survey.answerCount}", style = MaterialTheme.typography.headlineMedium)
                    Text(
                        "of ${survey.expectedAnswerCounts}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Metric("Reward per response", "${formatPoints(survey.pointsPerAnswer)} pts")
                Metric("Paid out", "${formatPoints(survey.totalPoints - survey.pendingPoints)} pts")
                Metric("Left in pool", "${formatPoints(survey.pendingPoints)} pts")
            }
        }
    }
}

@Composable
private fun Metric(label: String, value: String) {
    Column {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.titleMedium)
    }
}
