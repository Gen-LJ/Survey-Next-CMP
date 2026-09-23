package com.lucilab.surveynext.presentation.screens.interviewer.manage

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.lucilab.surveynext.data.model.SurveyState
import com.lucilab.surveynext.presentation.common.LoadState
import com.lucilab.surveynext.presentation.common.formatPoints
import com.lucilab.surveynext.presentation.components.BackTopBar
import com.lucilab.surveynext.presentation.components.dialog.ConfirmDialog
import com.lucilab.surveynext.presentation.screens.error.ErrorView
import com.lucilab.surveynext.presentation.screens.interviewer.manage.view.DraftEditor
import com.lucilab.surveynext.presentation.screens.interviewer.manage.view.LiveOverview
import com.lucilab.surveynext.presentation.screens.interviewer.manage.viewmodel.ManageSurveyViewModel
import com.lucilab.surveynext.presentation.screens.loading.LoadingView

@Composable
fun ManageSurveyScreen(
    onBack: () -> Unit,
    onOpenAnalytics: (Int) -> Unit,
    viewModel: ManageSurveyViewModel = hiltViewModel(),
) {
    val snackbarHostState = remember { SnackbarHostState() }
    var menuOpen by remember { mutableStateOf(false) }
    var confirmDelete by remember { mutableStateOf(false) }
    val survey = (viewModel.state as? LoadState.Success)?.data

    LaunchedEffect(viewModel.isDeleted) {
        if (viewModel.isDeleted) onBack()
    }
    LaunchedEffect(viewModel.message) {
        viewModel.message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    Scaffold(
        topBar = {
            Column {
                BackTopBar(
                    title = when (survey?.surveyState) {
                        SurveyState.Draft -> "Edit draft"
                        null -> "Survey"
                        else -> "Survey overview"
                    },
                    onBack = onBack,
                    actions = {
                        if (survey != null) {
                            IconButton(onClick = { menuOpen = true }) {
                                Icon(Icons.Rounded.MoreVert, contentDescription = "More")
                            }
                            DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                                DropdownMenuItem(
                                    text = { Text("Delete survey", color = MaterialTheme.colorScheme.error) },
                                    leadingIcon = {
                                        Icon(Icons.Outlined.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                                    },
                                    onClick = {
                                        menuOpen = false
                                        confirmDelete = true
                                    }
                                )
                            }
                        }
                    }
                )
                if (viewModel.isBusy) LinearProgressIndicator(Modifier.fillMaxWidth())
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        when (val state = viewModel.state) {
            LoadState.Loading -> LoadingView()
            is LoadState.Error -> ErrorView(state.message, onRetry = viewModel::load)
            is LoadState.Success -> if (state.data.surveyState == SurveyState.Draft) {
                DraftEditor(state.data, viewModel, Modifier.padding(padding))
            } else {
                LiveOverview(
                    survey = state.data,
                    isBusy = viewModel.isBusy,
                    onOpenAnalytics = { onOpenAnalytics(state.data.id) },
                    onSetPaused = viewModel::setPaused,
                    modifier = Modifier.padding(padding)
                )
            }
        }
    }

    if (confirmDelete && survey != null) {
        ConfirmDialog(
            title = "Delete survey?",
            message = buildString {
                append("\"${survey.title}\" and all of its responses will be permanently deleted.")
                if (survey.pendingPoints > 0) {
                    append(" The ${formatPoints(survey.pendingPoints)} unpaid points go back to your balance.")
                }
            },
            confirmLabel = "Delete",
            icon = Icons.Outlined.Delete,
            destructive = true,
            onConfirm = {
                confirmDelete = false
                viewModel.delete()
            },
            onDismiss = { confirmDelete = false }
        )
    }
}
