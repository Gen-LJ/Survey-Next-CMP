package com.lucilab.surveynext.presentation.screens.interviewer.manage.view

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.QuestionAnswer
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.RocketLaunch
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.lucilab.surveynext.data.model.QuestionModel
import com.lucilab.surveynext.data.model.SurveyModel
import com.lucilab.surveynext.presentation.common.formatPoints
import com.lucilab.surveynext.presentation.common.pluralize
import com.lucilab.surveynext.presentation.components.button.custombutton.CustomButton
import com.lucilab.surveynext.presentation.components.dialog.ConfirmDialog
import com.lucilab.surveynext.presentation.components.form.DropdownField
import com.lucilab.surveynext.presentation.components.form.Stepper
import com.lucilab.surveynext.presentation.components.state.EmptyView
import com.lucilab.surveynext.presentation.screens.interviewer.manage.viewmodel.ManageSurveyViewModel
import kotlinx.coroutines.launch

private val tabs = listOf("Questions", "Details", "Publish")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DraftEditor(
    survey: SurveyModel,
    viewModel: ManageSurveyViewModel,
    modifier: Modifier = Modifier,
) {
    val pagerState = rememberPagerState { tabs.size }
    val scope = rememberCoroutineScope()

    Column(modifier.fillMaxSize()) {
        PrimaryTabRow(selectedTabIndex = pagerState.currentPage, containerColor = MaterialTheme.colorScheme.background) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = pagerState.currentPage == index,
                    onClick = { scope.launch { pagerState.animateScrollToPage(index) } },
                    unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    text = { Text(if (index == 0) "$title (${survey.questionCount})" else title) }
                )
            }
        }
        HorizontalPager(state = pagerState, modifier = Modifier.weight(1f)) { page ->
            when (page) {
                0 -> QuestionsTab(survey, viewModel)
                1 -> DetailsTab(viewModel)
                else -> PublishTab(survey, viewModel)
            }
        }
    }
}

@Composable
private fun QuestionsTab(survey: SurveyModel, viewModel: ManageSurveyViewModel) {
    // Null while the sheet is closed; a target with no question means "new".
    var editor by remember { mutableStateOf<EditorTarget?>(null) }
    var pendingDelete by remember { mutableStateOf<QuestionModel?>(null) }
    val questions = survey.questions.orEmpty().sortedBy { it.position }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (questions.isEmpty()) {
            item {
                EmptyView(
                    icon = Icons.Outlined.QuestionAnswer,
                    title = "No questions yet",
                    message = "Mix multiple choice, star ratings and open text. Every question is required for respondents."
                )
            }
        }
        itemsIndexed(questions, key = { _, q -> q.id }) { index, question ->
            QuestionCard(
                number = index + 1,
                question = question,
                onEdit = { editor = EditorTarget(question) },
                onDelete = { pendingDelete = question },
                modifier = Modifier.animateItem()
            )
        }
        item {
            OutlinedButton(
                onClick = { editor = EditorTarget(null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 56.dp),
                shape = MaterialTheme.shapes.large,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Rounded.Add, contentDescription = null)
                Text("Add question", modifier = Modifier.padding(start = 8.dp))
            }
        }
    }

    editor?.let { target ->
        QuestionEditorSheet(
            existing = target.question,
            isSaving = viewModel.isBusy,
            onSave = { draft -> viewModel.saveQuestion(target.question, draft, onSaved = { editor = null }) },
            onDismiss = { editor = null }
        )
    }

    pendingDelete?.let { question ->
        ConfirmDialog(
            title = "Delete question?",
            message = "\"${question.text}\" will be removed from this survey.",
            confirmLabel = "Delete",
            destructive = true,
            onConfirm = {
                viewModel.deleteQuestion(question)
                pendingDelete = null
            },
            onDismiss = { pendingDelete = null }
        )
    }
}

private class EditorTarget(val question: QuestionModel?)

@Composable
private fun DetailsTab(viewModel: ManageSurveyViewModel) {
    val form = viewModel.infoForm ?: return
    val category = viewModel.categories.firstOrNull { it.id == form.categoryId }

    Column(
        Modifier
            .fillMaxSize()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedTextField(
            value = form.title,
            onValueChange = { viewModel.onInfoChange(form.copy(title = it)) },
            label = { Text("Title") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = form.description,
            onValueChange = { viewModel.onInfoChange(form.copy(description = it)) },
            label = { Text("Description") },
            minLines = 3,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
            modifier = Modifier.fillMaxWidth()
        )
        DropdownField(
            label = "Category",
            options = viewModel.categories,
            selected = category,
            optionLabel = { it.name },
            onSelected = { viewModel.onInfoChange(form.copy(categoryId = it.id)) },
            placeholder = if (viewModel.categories.isEmpty()) "Loading..." else "Select"
        )
        Stepper(
            label = "Estimated time",
            value = form.minutes,
            onValueChange = { viewModel.onInfoChange(form.copy(minutes = it)) },
            suffix = " min",
            max = 60
        )
        Text(
            "The target country and region can't be changed after creation.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp)
        )
        CustomButton(
            text = "Save details",
            onClick = viewModel::saveInfo,
            isLoading = viewModel.isBusy,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp)
        )
    }
}

@Composable
private fun PublishTab(survey: SurveyModel, viewModel: ManageSurveyViewModel) {
    val form = viewModel.publishForm
    val balanceAfter = viewModel.balance - form.totalCost
    val hasQuestions = survey.questionCount > 0
    val canPublish = hasQuestions && form.answers > 0 && form.points > 0 && balanceAfter >= 0
    var confirm by remember { mutableStateOf(false) }

    Column(
        Modifier
            .fillMaxSize()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ChecklistRow(
            done = hasQuestions,
            text = if (hasQuestions) "${pluralize(survey.questionCount, "question")} ready"
            else "Add at least one question"
        )

        Text("Reward", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 4.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(
                value = form.expectedAnswers,
                onValueChange = { viewModel.onPublishChange(form.copy(expectedAnswers = it)) },
                label = { Text("Responses") },
                supportingText = { Text("How many you need") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = form.pointsPerAnswer,
                onValueChange = { viewModel.onPublishChange(form.copy(pointsPerAnswer = it)) },
                label = { Text("Points each") },
                supportingText = { Text("Paid per response") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f)
            )
        }

        Surface(shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.surfaceContainerLow) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                CostRow("Your balance", "${formatPoints(viewModel.balance)} pts")
                CostRow("Reward pool", "− ${formatPoints(form.totalCost)} pts")
                HorizontalDivider()
                CostRow(
                    "Balance after publishing",
                    "${formatPoints(balanceAfter)} pts",
                    emphasize = true,
                    valueColor = if (balanceAfter < 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                )
            }
        }

        Text(
            "The pool is reserved when you publish and paid out as responses arrive. " +
                "Deleting the survey refunds whatever hasn't been paid.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (balanceAfter < 0) {
            Text(
                "Not enough points. Lower the reward or the number of responses.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error
            )
        }

        CustomButton(
            text = "Publish survey",
            icon = Icons.Rounded.RocketLaunch,
            onClick = { confirm = true },
            enabled = canPublish,
            isLoading = viewModel.isBusy,
            modifier = Modifier.fillMaxWidth()
        )
    }

    if (confirm) {
        ConfirmDialog(
            title = "Publish survey?",
            message = "${formatPoints(form.totalCost)} points will be reserved from your balance. " +
                "Questions can't be edited once the survey is live.",
            confirmLabel = "Publish",
            icon = Icons.Rounded.RocketLaunch,
            onConfirm = {
                confirm = false
                viewModel.publish()
            },
            onDismiss = { confirm = false }
        )
    }
}

@Composable
private fun ChecklistRow(done: Boolean, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Icon(
            if (done) Icons.Outlined.CheckCircle else Icons.Outlined.ErrorOutline,
            contentDescription = null,
            tint = if (done) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
            modifier = Modifier.size(20.dp)
        )
        Text(text, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun CostRow(label: String, value: String, emphasize: Boolean = false, valueColor: Color = Color.Unspecified) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            label,
            style = if (emphasize) MaterialTheme.typography.titleSmall else MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
        Text(
            value,
            style = if (emphasize) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyLarge,
            color = valueColor
        )
    }
}
