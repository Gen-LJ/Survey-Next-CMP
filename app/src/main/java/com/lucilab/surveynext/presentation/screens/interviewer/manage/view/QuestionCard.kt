package com.lucilab.surveynext.presentation.screens.interviewer.manage.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ShortText
import androidx.compose.material.icons.outlined.CheckBoxOutlineBlank
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material.icons.outlined.Checklist
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.lucilab.surveynext.data.model.QuestionModel
import com.lucilab.surveynext.data.model.QuestionType
import com.lucilab.surveynext.presentation.components.rating.RatingSelector

fun QuestionType.icon(): ImageVector = when (this) {
    QuestionType.MultipleChoice -> Icons.Outlined.Checklist
    QuestionType.Rating -> Icons.Outlined.StarOutline
    QuestionType.TextInput -> Icons.AutoMirrored.Outlined.ShortText
}

/**
 * A question in the author's list. Edit and delete appear only when their
 * callbacks are given, i.e. while the survey is still a draft.
 */
@Composable
fun QuestionCard(
    number: Int,
    question: QuestionModel,
    modifier: Modifier = Modifier,
    onEdit: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer, modifier = Modifier.size(28.dp)) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            number.toString(),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
                Row(
                    Modifier
                        .weight(1f)
                        .padding(start = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        question.type.icon(),
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        question.type.label + if (question.allowMultiAnswer) " · multi-select" else "",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (onEdit != null) {
                    IconButton(onClick = onEdit) { Icon(Icons.Outlined.Edit, contentDescription = "Edit question") }
                }
                if (onDelete != null) {
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Outlined.Delete, contentDescription = "Delete question", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }

            Text(question.text, style = MaterialTheme.typography.titleMedium)

            when (question.type) {
                QuestionType.MultipleChoice -> Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    question.options.orEmpty().sortedBy { it.position }.forEach { option ->
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(
                                if (question.allowMultiAnswer) Icons.Outlined.CheckBoxOutlineBlank else Icons.Outlined.RadioButtonUnchecked,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.outline
                            )
                            Text(option.text, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }

                QuestionType.Rating -> RatingSelector(rating = 0, starSize = 24.dp)

                QuestionType.TextInput -> Text(
                    "Respondents type a free-text answer",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
