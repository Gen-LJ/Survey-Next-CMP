package com.lucilab.surveynext.presentation.screens.respondent.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.History
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lucilab.surveynext.presentation.common.formatPoints
import com.lucilab.surveynext.presentation.common.formatRelative
import com.lucilab.surveynext.presentation.components.ListFooter
import com.lucilab.surveynext.presentation.components.LoadMoreEffect
import com.lucilab.surveynext.presentation.components.TabScaffold
import com.lucilab.surveynext.presentation.components.state.EmptyView
import com.lucilab.surveynext.presentation.components.survey.SurveyCard
import com.lucilab.surveynext.presentation.screens.error.ErrorView
import com.lucilab.surveynext.presentation.screens.loading.LoadingView
import com.lucilab.surveynext.presentation.screens.respondent.history.viewmodel.HistoryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    onOpenSurvey: (Int) -> Unit,
    viewModel: HistoryViewModel = hiltViewModel(),
) {
    val list = viewModel.list
    val listState = rememberLazyListState()
    LoadMoreEffect(listState, onLoadMore = viewModel::loadMore)

    TabScaffold(title = "History", subtitle = "Surveys you've answered") { padding ->
        when {
            list.items.isEmpty() && list.error != null -> ErrorView(list.error, onRetry = viewModel::retry)
            list.items.isEmpty() && list.isInitialLoad -> LoadingView()
            else -> PullToRefreshBox(
                isRefreshing = list.isRefreshing,
                onRefresh = viewModel::refresh,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                if (list.items.isEmpty()) {
                    EmptyView(
                        icon = Icons.Outlined.History,
                        title = "No answers yet",
                        message = "Surveys you complete, and the points they earned you, show up here."
                    )
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(list.items, key = { it.id }) { survey ->
                            SurveyCard(
                                survey = survey,
                                onClick = { onOpenSurvey(survey.id) },
                                footer = "Answered ${formatRelative(survey.answeredAt)} · " +
                                    "+${formatPoints(survey.pointsPerAnswer)} pts"
                            )
                        }
                        item { ListFooter(list.isLoadingMore, list.error, viewModel::retry) }
                    }
                }
            }
        }
    }
}
