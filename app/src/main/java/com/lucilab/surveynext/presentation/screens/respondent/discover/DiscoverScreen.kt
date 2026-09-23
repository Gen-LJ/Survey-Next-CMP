package com.lucilab.surveynext.presentation.screens.respondent.discover

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Bookmarks
import androidx.compose.material.icons.outlined.TravelExplore
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lucilab.surveynext.data.model.SurveyModel
import com.lucilab.surveynext.presentation.common.LoadState
import com.lucilab.surveynext.presentation.common.pluralize
import com.lucilab.surveynext.presentation.components.ListFooter
import com.lucilab.surveynext.presentation.components.LoadMoreEffect
import com.lucilab.surveynext.presentation.components.TabScaffold
import com.lucilab.surveynext.presentation.components.state.EmptyView
import com.lucilab.surveynext.presentation.components.survey.SurveyCard
import com.lucilab.surveynext.presentation.screens.error.ErrorView
import com.lucilab.surveynext.presentation.screens.loading.LoadingView
import com.lucilab.surveynext.presentation.screens.respondent.discover.viewmodel.DiscoverViewModel
import com.lucilab.surveynext.presentation.screens.respondent.discover.viewmodel.MAX_SAVED

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiscoverScreen(
    onOpenSurvey: (Int) -> Unit,
    viewModel: DiscoverViewModel = hiltViewModel(),
) {
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(viewModel.message) {
        viewModel.message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    TabScaffold(title = "Discover", snackbarHostState = snackbarHostState) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            SingleChoiceSegmentedButtonRow(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                SegmentedButton(
                    selected = !viewModel.showingSaved,
                    onClick = { viewModel.showSaved(false) },
                    shape = SegmentedButtonDefaults.itemShape(0, 2),
                    label = { Text("Available") }
                )
                SegmentedButton(
                    selected = viewModel.showingSaved,
                    onClick = { viewModel.showSaved(true) },
                    shape = SegmentedButtonDefaults.itemShape(1, 2),
                    label = { Text("Saved ${viewModel.savedIds.size}/$MAX_SAVED") }
                )
            }

            PullToRefreshBox(
                isRefreshing = viewModel.available.isRefreshing,
                onRefresh = viewModel::refresh,
                modifier = Modifier.fillMaxSize()
            ) {
                if (viewModel.showingSaved) SavedList(viewModel, onOpenSurvey) else AvailableList(viewModel, onOpenSurvey)
            }
        }
    }
}

@Composable
private fun AvailableList(viewModel: DiscoverViewModel, onOpenSurvey: (Int) -> Unit) {
    val list = viewModel.available
    val listState = rememberLazyListState()
    LoadMoreEffect(listState, onLoadMore = viewModel::loadMore)

    when {
        list.items.isEmpty() && list.error != null -> ErrorView(list.error, onRetry = viewModel::retryAvailable)
        list.items.isEmpty() && list.isInitialLoad -> LoadingView()
        list.items.isEmpty() -> EmptyView(
            icon = Icons.Outlined.TravelExplore,
            title = "No surveys right now",
            message = "Surveys are matched to your country and region. Check back soon."
        )

        else -> SurveyList(
            surveys = list.items,
            savedIds = viewModel.savedIds,
            onOpenSurvey = onOpenSurvey,
            onToggleSaved = viewModel::toggleSaved,
            listState = listState,
            footer = { ListFooter(list.isLoadingMore, list.error, viewModel::retryAvailable) }
        )
    }
}

@Composable
private fun SavedList(viewModel: DiscoverViewModel, onOpenSurvey: (Int) -> Unit) {
    when (val saved = viewModel.saved) {
        LoadState.Loading -> LoadingView()
        is LoadState.Error -> ErrorView(saved.message, onRetry = viewModel::retrySaved)
        is LoadState.Success -> if (saved.data.isEmpty()) {
            EmptyView(
                icon = Icons.Outlined.Bookmarks,
                title = "Nothing saved",
                message = "Bookmark up to $MAX_SAVED surveys to answer later."
            )
        } else {
            SurveyList(
                surveys = saved.data,
                savedIds = viewModel.savedIds,
                onOpenSurvey = onOpenSurvey,
                onToggleSaved = viewModel::toggleSaved,
            )
        }
    }
}

@Composable
private fun SurveyList(
    surveys: List<SurveyModel>,
    savedIds: Set<Int>,
    onOpenSurvey: (Int) -> Unit,
    onToggleSaved: (SurveyModel) -> Unit,
    listState: LazyListState = rememberLazyListState(),
    footer: @Composable () -> Unit = {},
) {
    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(surveys, key = { it.id }) { survey ->
            val isSaved = survey.id in savedIds
            SurveyCard(
                survey = survey,
                onClick = { onOpenSurvey(survey.id) },
                footer = "${pluralize(survey.spotsLeft, "spot")} left",
                modifier = Modifier.animateItem(),
                trailing = {
                    IconToggleButton(checked = isSaved, onCheckedChange = { onToggleSaved(survey) }) {
                        Icon(
                            if (isSaved) Icons.Rounded.Bookmark else Icons.Outlined.BookmarkBorder,
                            contentDescription = if (isSaved) "Remove from saved" else "Save for later",
                            tint = if (isSaved) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            )
        }
        item { footer() }
    }
}
