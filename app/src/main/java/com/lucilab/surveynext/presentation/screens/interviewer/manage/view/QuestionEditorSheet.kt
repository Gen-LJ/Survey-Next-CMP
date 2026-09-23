package com.lucilab.surveynext.presentation.screens.interviewer.manage.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.lucilab.surveynext.data.model.QuestionModel
import com.lucilab.surveynext.data.model.QuestionType
import com.lucilab.surveynext.presentation.components.button.custombutton.CustomButton
import com.lucilab.surveynext.presentation.screens.interviewer.manage.viewmodel.QuestionDraft

private const val MAX_OPTIONS = 10

/**
 * Adds a question, or edits [existing]. The backend only lets an existing
 * question change its text and options, so type and multi-select lock then.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuestionEditorSheet(
    existing: QuestionModel?,
    isSaving: Boolean,
    onSave: (QuestionDraft) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var draft by remember(existing) { mutableStateOf(existing?.let(QuestionDraft::from) ?: QuestionDraft()) }
    var showErrors by remember { mutableStateOf(false) }
    val isEditing = existing != null

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            Modifier
                .fillMaxWidth()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(if (isEditing) "Edit question" else "New question", style = MaterialTheme.typography.titleLarge)

            SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                QuestionType.entries.forEachIndexed { index, type ->
                    SegmentedButton(
                        selected = draft.type == type,
                        onClick = { draft = draft.copy(type = type) },
                        enabled = !isEditing,
                        shape = SegmentedButtonDefaults.itemShape(index, QuestionType.entries.size),
                        icon = { SegmentedButtonDefaults.Icon(draft.type == type) { Icon(type.icon(), null) } },
                        label = { Text(type.shortLabel()) }
                    )
                }
            }

            OutlinedTextField(
                value = draft.text,
                onValueChange = { draft = draft.copy(text = it) },
                label = { Text("Question") },
                minLines = 2,
                isError = showErrors && draft.text.isBlank(),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                modifier = Modifier.fillMaxWidth()
            )

            when (draft.type) {
                QuestionType.MultipleChoice -> {
                    Text("Options", style = MaterialTheme.typography.titleSmall)
                    draft.options.forEachIndexed { index, option ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = option,
                                onValueChange = { value ->
                                    draft = draft.copy(options = draft.options.toMutableList().also { it[index] = value })
                                },
                                placeholder = { Text("Option ${index + 1}") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = {
                                    draft = draft.copy(options = draft.options.toMutableList().also { it.removeAt(index) })
                                },
                                enabled = draft.options.size > 2
                            ) { Icon(Icons.Rounded.Close, contentDescription = "Remove option") }
                        }
                    }
                    if (draft.options.size < MAX_OPTIONS) {
                        TextButton(onClick = { draft = draft.copy(options = draft.options + "") }) {
                            Icon(Icons.Rounded.Add, contentDescription = null)
                            Text("Add option", modifier = Modifier.padding(start = 6.dp))
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("Allow multiple answers", style = MaterialTheme.typography.bodyLarge)
                            Text(
                                if (isEditing) "Can't be changed after the question is created"
                                else "Respondents can pick more than one option",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = draft.allowMultiAnswer,
                            onCheckedChange = { draft = draft.copy(allowMultiAnswer = it) },
                            enabled = !isEditing
                        )
                    }
                }

                QuestionType.Rating -> HintText("Respondents rate from 1 to 5 stars.")
                QuestionType.TextInput -> HintText("Respondents answer in their own words.")
            }

            if (showErrors) {
                draft.validationError?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
                }
            }

            CustomButton(
                text = if (isEditing) "Save changes" else "Add question",
                onClick = {
                    showErrors = true
                    if (draft.validationError == null) onSave(draft)
                },
                isLoading = isSaving,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
            )
        }
    }
}

@Composable
private fun HintText(text: String) {
    Text(text, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
}

private fun QuestionType.shortLabel() = when (this) {
    QuestionType.MultipleChoice -> "Choice"
    QuestionType.Rating -> "Rating"
    QuestionType.TextInput -> "Text"
}
