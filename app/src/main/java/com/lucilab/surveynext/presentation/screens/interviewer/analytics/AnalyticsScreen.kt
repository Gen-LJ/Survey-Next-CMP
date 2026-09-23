package com.lucilab.surveynext.presentation.screens.interviewer.analytics

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.material.icons.outlined.Percent
import androidx.compose.material.icons.outlined.Stars
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lucilab.surveynext.data.model.AnalyticsModel
import com.lucilab.surveynext.data.model.QuestionSummaryModel
import com.lucilab.surveynext.data.model.QuestionType
import com.lucilab.surveynext.presentation.common.LoadState
import com.lucilab.surveynext.presentation.common.formatPoints
import com.lucilab.surveynext.presentation.components.BackTopBar
import com.lucilab.surveynext.presentation.components.card.StatCard
import com.lucilab.surveynext.presentation.components.rating.RatingSelector
import com.lucilab.surveynext.presentation.components.state.EmptyView
import com.lucilab.surveynext.presentation.components.survey.StateBadge
import com.lucilab.surveynext.presentation.screens.error.ErrorView
import com.lucilab.surveynext.presentation.screens.interviewer.analytics.viewmodel.AnalyticsViewModel
import com.lucilab.surveynext.presentation.screens.interviewer.manage.view.icon
import com.lucilab.surveynext.presentation.screens.loading.LoadingView
import kotlin.math.roundToInt

private const val TEXT_PREVIEW_COUNT = 3

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    onBack: () -> Unit,
    viewModel: AnalyticsViewModel = hiltViewModel(),
) {
    Scaffold(topBar = { BackTopBar("Responses", onBack = onBack) }) { padding ->
        when (val state = viewModel.state) {
            LoadState.Loading -> LoadingView("Crunching responses...")
            is LoadState.Error -> ErrorView(state.message, onRetry = viewModel::load)
            is LoadState.Success -> PullToRefreshBox(
                isRefreshing = viewModel.isRefreshing,
                onRefresh = viewModel::refresh,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                AnalyticsContent(state.data)
            }
        }
    }
}

@Composable
private fun AnalyticsContent(data: AnalyticsModel) {
    val survey = data.survey
    val completion = if (survey.expectedAnswerCounts == 0) 0
    else (data.answerTotal * 100f / survey.expectedAnswerCounts).roundToInt()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                StateBadge(survey.surveyState)
                Text(survey.title, style = MaterialTheme.typography.headlineSmall)
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard(Icons.Outlined.Groups, data.answerTotal.toString(), "Responses", Modifier.weight(1f))
                StatCard(
                    Icons.Outlined.Percent, "$completion%", "Of target", Modifier.weight(1f),
                    accent = MaterialTheme.colorScheme.secondary
                )
                StatCard(
                    Icons.Outlined.Stars,
                    formatPoints(data.answerTotal * survey.pointsPerAnswer),
                    "Points paid",
                    Modifier.weight(1f),
                    accent = MaterialTheme.colorScheme.tertiary
                )
            }
        }

        if (data.answerTotal == 0) {
            item {
                EmptyView(
                    icon = Icons.Outlined.Insights,
                    title = "No responses yet",
                    message = "Results appear here as respondents in your target region answer. Pull down to refresh."
                )
            }
        } else {
            itemsIndexed(data.summary, key = { _, s -> s.questionId }) { index, summary ->
                QuestionSummaryCard(index + 1, summary)
            }
        }
    }
}

@Composable
private fun QuestionSummaryCard(number: Int, summary: QuestionSummaryModel) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(
                    summary.type.icon(),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.height(16.dp)
                )
                Text(
                    "Q$number · ${summary.type.label} · ${summary.responseCount} answered",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(summary.text, style = MaterialTheme.typography.titleMedium)

            when (summary.type) {
                QuestionType.MultipleChoice -> ChoiceBreakdown(summary)
                QuestionType.Rating -> RatingBreakdown(summary)
                QuestionType.TextInput -> TextResponses(summary.textResponses.orEmpty())
            }
        }
    }
}

@Composable
private fun ChoiceBreakdown(summary: QuestionSummaryModel) {
    val counts = summary.optionCounts.orEmpty()
    // Multi-select questions can total more picks than respondents; percentages are per respondent.
    val base = summary.responseCount.coerceAtLeast(1)
    val top = counts.maxOfOrNull { it.count } ?: 0

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        counts.forEach { option ->
            val share = option.count.toFloat() / base
            Bar(
                label = option.option,
                valueLabel = "${(share * 100).roundToInt()}% · ${option.count}",
                fraction = share,
                highlight = option.count == top && top > 0
            )
        }
    }
}

@Composable
private fun RatingBreakdown(summary: QuestionSummaryModel) {
    val buckets = summary.ratingCounts.orEmpty()
    val total = buckets.sum().coerceAtLeast(1)
    val average = summary.averageRating ?: 0.0

    Row(horizontalArrangement = Arrangement.spacedBy(20.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("%.1f".format(average), style = MaterialTheme.typography.displaySmall)
            RatingSelector(rating = average.roundToInt(), starSize = 16.dp)
            Text(
                "average",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            for (stars in 5 downTo 1) {
                val count = buckets.getOrNull(stars - 1) ?: 0
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("$stars", style = MaterialTheme.typography.labelMedium, modifier = Modifier.width(10.dp))
                    BarTrack(count.toFloat() / total, MaterialTheme.colorScheme.tertiary, Modifier.weight(1f))
                    Text(
                        "$count",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.width(24.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun TextResponses(responses: List<String>) {
    var expanded by remember { mutableStateOf(false) }
    val shown = if (expanded) responses else responses.take(TEXT_PREVIEW_COUNT)

    Column(Modifier.animateContentSize(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        shown.forEach { text ->
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("“$text”", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(12.dp))
            }
        }
        if (responses.size > TEXT_PREVIEW_COUNT) {
            TextButton(onClick = { expanded = !expanded }) {
                Text(if (expanded) "Show less" else "Show all ${responses.size}")
            }
        }
    }
}

@Composable
private fun Bar(label: String, valueLabel: String, fraction: Float, highlight: Boolean) {
    val color = if (highlight) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(alpha = 0.45f)
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row {
            Text(label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
            Text(valueLabel, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        BarTrack(fraction, color, Modifier.fillMaxWidth())
    }
}

@Composable
private fun BarTrack(fraction: Float, color: Color, modifier: Modifier = Modifier) {
    val animated by animateFloatAsState(fraction.coerceIn(0f, 1f), tween(600), label = "bar")
    Box(
        modifier
            .height(10.dp)
            .clip(MaterialTheme.shapes.small)
            .background(MaterialTheme.colorScheme.surfaceContainerHighest)
    ) {
        Box(
            Modifier
                .fillMaxHeight()
                .fillMaxWidth(animated)
                .clip(MaterialTheme.shapes.small)
                .background(color)
        )
    }
}
