package com.lucilab.surveynext.presentation.screens.respondent.details

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Stars
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lucilab.surveynext.data.model.AnswerDetailsModel
import com.lucilab.surveynext.data.model.QuestionModel
import com.lucilab.surveynext.data.model.QuestionType
import com.lucilab.surveynext.presentation.common.LoadState
import com.lucilab.surveynext.presentation.common.formatDate
import com.lucilab.surveynext.presentation.common.formatPoints
import com.lucilab.surveynext.presentation.components.BackTopBar
import com.lucilab.surveynext.presentation.components.rating.RatingSelector
import com.lucilab.surveynext.presentation.components.rating.ratingLabel
import com.lucilab.surveynext.presentation.screens.error.ErrorView
import com.lucilab.surveynext.presentation.screens.loading.LoadingView
import com.lucilab.surveynext.presentation.screens.respondent.details.viewmodel.AnswerDetailsViewModel

@Composable
fun AnswerDetailsScreen(
    onBack: () -> Unit,
    viewModel: AnswerDetailsViewModel = hiltViewModel(),
) {
    Scaffold(topBar = { BackTopBar("Your answers", onBack = onBack) }) { padding ->
        when (val state = viewModel.state) {
            LoadState.Loading -> LoadingView()
            is LoadState.Error -> ErrorView(state.message, onRetry = viewModel::load)
            is LoadState.Success -> AnswerDetailsContent(state.data, Modifier.padding(padding))
        }
    }
}

@Composable
private fun AnswerDetailsContent(details: AnswerDetailsModel, modifier: Modifier = Modifier) {
    val survey = details.survey
    val responses = details.answer.userAnswers.associate { it.questionId to it.responses }
    val questions = survey.questions.orEmpty().sortedBy { it.position }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    survey.categoryName.uppercase(),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(survey.title, style = MaterialTheme.typography.headlineSmall)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Answered ${formatDate(details.answer.answeredAt)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Surface(shape = CircleShape, color = MaterialTheme.colorScheme.tertiaryContainer) {
                        Row(
                            Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                Icons.Rounded.Stars,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                            Text(
                                "+${formatPoints(details.answer.pointsEarned)} pts",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }
                    }
                }
            }
        }

        itemsIndexed(questions, key = { _, q -> q.id }) { index, question ->
            AnsweredQuestionCard(index + 1, question, responses[question.id].orEmpty())
        }
    }
}

@Composable
private fun AnsweredQuestionCard(number: Int, question: QuestionModel, responses: List<String>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                "Q$number",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(question.text, style = MaterialTheme.typography.titleMedium)

            when (question.type) {
                QuestionType.MultipleChoice -> Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    question.options.orEmpty().sortedBy { it.position }.forEach { option ->
                        val picked = option.text in responses
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(
                                if (picked) Icons.Rounded.CheckCircle else Icons.Outlined.Circle,
                                contentDescription = if (picked) "Your answer" else null,
                                tint = if (picked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                option.text,
                                style = MaterialTheme.typography.bodyLarge,
                                color = if (picked) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                QuestionType.Rating -> {
                    val rating = responses.firstOrNull()?.toIntOrNull() ?: 0
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        RatingSelector(rating = rating, starSize = 28.dp)
                        Text(ratingLabel(rating), style = MaterialTheme.typography.labelLarge)
                    }
                }

                QuestionType.TextInput -> Surface(
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        responses.firstOrNull().orEmpty(),
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        }
    }
}
