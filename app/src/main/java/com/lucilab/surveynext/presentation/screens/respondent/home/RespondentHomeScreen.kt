package com.lucilab.surveynext.presentation.screens.respondent.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Bookmarks
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Inbox
import androidx.compose.material.icons.outlined.TaskAlt
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lucilab.surveynext.data.model.RespondentHomeModel
import com.lucilab.surveynext.presentation.common.LoadState
import com.lucilab.surveynext.presentation.common.pluralize
import com.lucilab.surveynext.presentation.components.SectionHeader
import com.lucilab.surveynext.presentation.components.TabScaffold
import com.lucilab.surveynext.presentation.components.card.PointsCard
import com.lucilab.surveynext.presentation.components.card.StatCard
import com.lucilab.surveynext.presentation.components.state.EmptyView
import com.lucilab.surveynext.presentation.components.survey.SurveyCard
import com.lucilab.surveynext.presentation.screens.error.ErrorView
import com.lucilab.surveynext.presentation.screens.loading.LoadingView
import com.lucilab.surveynext.presentation.screens.respondent.home.viewmodel.RespondentHomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RespondentHomeScreen(
    userName: String,
    onOpenSurvey: (Int) -> Unit,
    onOpenDiscover: (showSaved: Boolean) -> Unit,
    onOpenHistory: () -> Unit,
    viewModel: RespondentHomeViewModel = hiltViewModel(),
) {
    TabScaffold(
        title = "Hi, ${userName.substringBefore(' ')}",
        subtitle = "Share your opinion, earn points",
    ) { padding ->
        when (val state = viewModel.state) {
            LoadState.Loading -> LoadingView()
            is LoadState.Error -> ErrorView(state.message, onRetry = viewModel::retry)
            is LoadState.Success -> PullToRefreshBox(
                isRefreshing = viewModel.isRefreshing,
                onRefresh = viewModel::refresh,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                HomeContent(state.data, onOpenSurvey, onOpenDiscover, onOpenHistory)
            }
        }
    }
}

@Composable
private fun HomeContent(
    home: RespondentHomeModel,
    onOpenSurvey: (Int) -> Unit,
    onOpenDiscover: (Boolean) -> Unit,
    onOpenHistory: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            PointsCard(
                label = "Points earned",
                points = home.points,
                caption = "${pluralize(home.answeredCount, "survey")} completed so far"
            )
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard(
                    Icons.Outlined.Explore, home.availableCount.toString(), "Available",
                    Modifier.weight(1f), onClick = { onOpenDiscover(false) }
                )
                StatCard(
                    Icons.Outlined.TaskAlt, home.answeredCount.toString(), "Completed",
                    Modifier.weight(1f), accent = MaterialTheme.colorScheme.secondary, onClick = onOpenHistory
                )
                StatCard(
                    Icons.Outlined.Bookmarks, "${home.savedCount}/5", "Saved",
                    Modifier.weight(1f), accent = MaterialTheme.colorScheme.tertiary, onClick = { onOpenDiscover(true) }
                )
            }
        }

        if (home.recentSurveys.isEmpty()) {
            item {
                EmptyView(
                    icon = Icons.Outlined.Inbox,
                    title = "You're all caught up",
                    message = "New surveys for your region will appear here. Pull down to check again."
                )
            }
        } else {
            item {
                SectionHeader("New for you", actionLabel = "See all", onAction = { onOpenDiscover(false) })
            }
            items(home.recentSurveys, key = { it.id }) { survey ->
                SurveyCard(
                    survey = survey,
                    onClick = { onOpenSurvey(survey.id) },
                    footer = "${pluralize(survey.spotsLeft, "spot")} left"
                )
            }
        }
    }
}
