package com.lucilab.surveynext.presentation.components.survey

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Stars
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.lucilab.surveynext.data.model.SurveyModel
import com.lucilab.surveynext.data.model.SurveyState
import com.lucilab.surveynext.presentation.common.formatPoints
import com.lucilab.surveynext.presentation.common.formatRelative

/**
 * A survey in a list.
 *
 * @param showState show the lifecycle badge and answer progress (the author's view)
 * @param footer replaces the default "last updated" caption
 * @param trailing top-right slot, e.g. a bookmark toggle
 */
@Composable
fun SurveyCard(
    survey: SurveyModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    showState: Boolean = false,
    footer: String? = null,
    trailing: @Composable (() -> Unit)? = null,
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        shape = MaterialTheme.shapes.large
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        survey.categoryName.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        survey.title,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (showState) {
                    Spacer(Modifier.width(8.dp))
                    StateBadge(survey.surveyState)
                }
                trailing?.invoke()
            }

            if (survey.description.isNotBlank()) {
                Text(
                    survey.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                MetaItem(Icons.Outlined.Schedule, "${survey.minutes} min")
                MetaItem(Icons.AutoMirrored.Outlined.HelpOutline, "${survey.questionCount} Qs")
                if (survey.pointsPerAnswer > 0) {
                    MetaItem(
                        Icons.Outlined.Stars,
                        "${formatPoints(survey.pointsPerAnswer)} pts",
                        highlight = true
                    )
                }
            }

            if (showState && survey.surveyState != SurveyState.Draft) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    LinearProgressIndicator(
                        progress = { survey.progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp),
                        strokeCap = StrokeCap.Round,
                        trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                        drawStopIndicator = {}
                    )
                    Text(
                        "${survey.answerCount} of ${survey.expectedAnswerCounts} responses",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                Text(
                    footer ?: "Updated ${formatRelative(survey.lastModifiedAt)}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun MetaItem(icon: ImageVector, text: String, highlight: Boolean = false) {
    val color = if (highlight) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurfaceVariant
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
        Text(text, style = MaterialTheme.typography.labelLarge, color = color)
    }
}
