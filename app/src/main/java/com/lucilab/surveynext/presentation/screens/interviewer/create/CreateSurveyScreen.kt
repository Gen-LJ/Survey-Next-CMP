package com.lucilab.surveynext.presentation.screens.interviewer.create

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lucilab.surveynext.presentation.common.LoadState
import com.lucilab.surveynext.presentation.components.BackTopBar
import com.lucilab.surveynext.presentation.components.button.custombutton.CustomButton
import com.lucilab.surveynext.presentation.components.form.DropdownField
import com.lucilab.surveynext.presentation.components.form.Stepper
import com.lucilab.surveynext.presentation.screens.error.ErrorView
import com.lucilab.surveynext.presentation.screens.interviewer.create.viewmodel.CreateSurveyOptions
import com.lucilab.surveynext.presentation.screens.interviewer.create.viewmodel.CreateSurveyViewModel
import com.lucilab.surveynext.presentation.screens.loading.LoadingView

@Composable
fun CreateSurveyScreen(
    onBack: () -> Unit,
    onCreated: (Int) -> Unit,
    viewModel: CreateSurveyViewModel = hiltViewModel(),
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel.createdSurveyId) {
        viewModel.createdSurveyId?.let(onCreated)
    }
    LaunchedEffect(viewModel.errorMessage) {
        viewModel.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = { BackTopBar("New survey", onBack = onBack, navigationIcon = Icons.Rounded.Close) },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        when (val options = viewModel.options) {
            LoadState.Loading -> LoadingView("Preparing form...")
            is LoadState.Error -> ErrorView(options.message, onRetry = viewModel::load)
            is LoadState.Success -> CreateSurveyForm(
                viewModel = viewModel,
                options = options.data,
                modifier = Modifier.padding(padding)
            )
        }
    }
}

@Composable
private fun CreateSurveyForm(
    viewModel: CreateSurveyViewModel,
    options: CreateSurveyOptions,
    modifier: Modifier = Modifier,
) {
    val form = viewModel.form

    Column(
        modifier = modifier
            .fillMaxSize()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .navigationBarsPadding(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            "Start with the basics. You'll add questions and set rewards next.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        OutlinedTextField(
            value = form.title,
            onValueChange = viewModel::onTitleChange,
            label = { Text("Title") },
            placeholder = { Text("e.g. Commute habits") },
            singleLine = true,
            isError = form.titleError != null,
            supportingText = form.titleError?.let { { Text(it) } },
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = form.description,
            onValueChange = viewModel::onDescriptionChange,
            label = { Text("Description") },
            placeholder = { Text("What is this survey about?") },
            minLines = 3,
            isError = form.descriptionError != null,
            supportingText = form.descriptionError?.let { { Text(it) } },
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
            modifier = Modifier.fillMaxWidth()
        )

        DropdownField(
            label = "Category",
            options = options.categories,
            selected = form.category,
            optionLabel = { it.name },
            onSelected = viewModel::onCategorySelected,
            error = form.categoryError
        )

        Text("Audience", style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(top = 8.dp))

        DropdownField(
            label = "Country",
            options = options.countries,
            selected = form.country,
            optionLabel = { it.name },
            onSelected = { viewModel.onCountrySelected(it) },
            error = form.countryError
        )

        DropdownField(
            label = "Region",
            options = form.regions,
            selected = form.region,
            optionLabel = { it.name },
            onSelected = viewModel::onRegionSelected,
            enabled = form.country != null && !form.regionsLoading,
            placeholder = if (form.regionsLoading) "Loading regions..." else "Select",
            error = form.regionError,
            supportingText = "Only respondents living here will see this survey"
        )

        Stepper(
            label = "Estimated time",
            supportingText = "How long it takes to answer",
            value = form.minutes,
            onValueChange = viewModel::onMinutesChange,
            suffix = " min",
            max = 60,
            modifier = Modifier.padding(top = 4.dp)
        )

        CustomButton(
            text = "Create & add questions",
            icon = Icons.AutoMirrored.Rounded.ArrowForward,
            onClick = viewModel::submit,
            isLoading = viewModel.isSubmitting,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        )
    }
}
