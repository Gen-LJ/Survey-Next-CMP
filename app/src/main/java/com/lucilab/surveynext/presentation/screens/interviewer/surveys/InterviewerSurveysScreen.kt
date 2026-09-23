package com.lucilab.surveynext.presentation.screens.interviewer.surveys

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lucilab.surveynext.data.model.SurveyState
import com.lucilab.surveynext.presentation.components.ListFooter
import com.lucilab.surveynext.presentation.components.LoadMoreEffect
import com.lucilab.surveynext.presentation.components.TabScaffold
import com.lucilab.surveynext.presentation.components.state.EmptyView
import com.lucilab.surveynext.presentation.components.survey.SurveyCard
import com.lucilab.surveynext.presentation.screens.error.ErrorView
import com.lucilab.surveynext.presentation.screens.interviewer.surveys.viewmodel.InterviewerSurveysViewModel
import com.lucilab.surveynext.presentation.screens.loading.LoadingView

private val filters: List<SurveyState?> = listOf(null) + SurveyState.entries

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InterviewerSurveysScreen(
    onCreateSurvey: () -> Unit,
    onOpenSurvey: (Int) -> Unit,
    viewModel: InterviewerSurveysViewModel = hiltViewModel(),
) {
    val list = viewModel.list
    val listState = rememberLazyListState()
    LoadMoreEffect(listState, onLoadMore = viewModel::loadMore)

    TabScaffold(
        title = "My surveys",
        floatingActionButton = {
            FloatingActionButton(onClick = onCreateSurvey) {
                Icon(Icons.Rounded.Add, contentDescription = "New survey")
            }
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filters) { state ->
                    FilterChip(
                        selected = viewModel.filter == state,
                        onClick = { viewModel.selectFilter(state) },
                        label = { Text(state?.label ?: "All") }
                    )
                }
            }

            when {
                list.items.isEmpty() && list.error != null ->
                    ErrorView(list.error, onRetry = viewModel::retry)

                list.items.isEmpty() && (list.isInitialLoad || list.isRefreshing) -> LoadingView()

                else -> PullToRefreshBox(
                    isRefreshing = list.isRefreshing,
                    onRefresh = viewModel::refresh,
                    modifier = Modifier.fillMaxSize()
                ) {
                    if (list.items.isEmpty()) {
                        EmptyView(
                            icon = Icons.AutoMirrored.Outlined.Assignment,
                            title = if (viewModel.filter == null) "No surveys yet" else "Nothing ${viewModel.filter?.label?.lowercase()}",
                            message = if (viewModel.filter == null) "Tap + to draft your first survey."
                            else "Surveys will show up here as they move through their lifecycle.",
                        )
                    } else {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 96.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(list.items, key = { it.id }) { survey ->
                                SurveyCard(
                                    survey = survey,
                                    onClick = { onOpenSurvey(survey.id) },
                                    showState = true,
                                    modifier = Modifier.animateItem()
                                )
                            }
                            item { ListFooter(list.isLoadingMore, list.error, viewModel::retry) }
                        }
                    }
                }
            }
        }
    }
}
