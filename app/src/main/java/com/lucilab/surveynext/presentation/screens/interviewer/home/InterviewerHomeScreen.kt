package com.lucilab.surveynext.presentation.screens.interviewer.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.PauseCircle
import androidx.compose.material.icons.outlined.PlayCircle
import androidx.compose.material.icons.outlined.PostAdd
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lucilab.surveynext.data.model.InterviewerHomeModel
import com.lucilab.surveynext.data.model.SurveyState
import com.lucilab.surveynext.presentation.common.LoadState
import com.lucilab.surveynext.presentation.components.SectionHeader
import com.lucilab.surveynext.presentation.components.TabScaffold
import com.lucilab.surveynext.presentation.components.card.PointsCard
import com.lucilab.surveynext.presentation.components.card.StatCard
import com.lucilab.surveynext.presentation.components.state.EmptyView
import com.lucilab.surveynext.presentation.components.survey.SurveyCard
import com.lucilab.surveynext.presentation.screens.error.ErrorView
import com.lucilab.surveynext.presentation.screens.interviewer.home.viewmodel.InterviewerHomeViewModel
import com.lucilab.surveynext.presentation.screens.loading.LoadingView
import com.lucilab.surveynext.presentation.theme.statusColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InterviewerHomeScreen(
    userName: String,
    onCreateSurvey: () -> Unit,
    onOpenSurvey: (Int) -> Unit,
    onOpenSurveys: (SurveyState?) -> Unit,
    viewModel: InterviewerHomeViewModel = hiltViewModel(),
) {
    TabScaffold(
        title = "Hi, ${userName.substringBefore(' ')}",
        subtitle = "Here's how your surveys are doing",
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onCreateSurvey,
                icon = { Icon(Icons.Rounded.Add, contentDescription = null) },
                text = { Text("New survey") }
            )
        }
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
                HomeContent(state.data, onCreateSurvey, onOpenSurvey, onOpenSurveys)
            }
        }
    }
}

@Composable
private fun HomeContent(
    home: InterviewerHomeModel,
    onCreateSurvey: () -> Unit,
    onOpenSurvey: (Int) -> Unit,
    onOpenSurveys: (SurveyState?) -> Unit,
) {
    val hasSurveys = home.draftCount + home.publishedCount + home.pausedCount + home.completedCount > 0

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            PointsCard(
                label = "Available balance",
                points = home.points,
                caption = "Publishing a survey reserves its reward pool from this balance"
            )
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard(
                    icon = Icons.Outlined.EditNote,
                    value = home.draftCount.toString(),
                    label = "Drafts",
                    accent = statusColor(SurveyState.Draft).content,
                    onClick = { onOpenSurveys(SurveyState.Draft) },
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    icon = Icons.Outlined.PlayCircle,
                    value = home.publishedCount.toString(),
                    label = "Live",
                    accent = statusColor(SurveyState.Published).content,
                    onClick = { onOpenSurveys(SurveyState.Published) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard(
                    icon = Icons.Outlined.PauseCircle,
                    value = home.pausedCount.toString(),
                    label = "Paused",
                    accent = statusColor(SurveyState.Paused).content,
                    onClick = { onOpenSurveys(SurveyState.Paused) },
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    icon = Icons.Outlined.CheckCircle,
                    value = home.completedCount.toString(),
                    label = "Completed",
                    accent = statusColor(SurveyState.Completed).content,
                    onClick = { onOpenSurveys(SurveyState.Completed) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        if (!hasSurveys) {
            item {
                EmptyView(
                    icon = Icons.Outlined.PostAdd,
                    title = "Create your first survey",
                    message = "Draft your questions, set a reward, and publish to respondents in your target region.",
                    action = { Button(onClick = onCreateSurvey) { Text("Start a survey") } }
                )
            }
        }

        if (home.recentPublished.isNotEmpty()) {
            item {
                SectionHeader("Live now", actionLabel = "See all", onAction = { onOpenSurveys(SurveyState.Published) })
            }
            items(home.recentPublished, key = { "live-${it.id}" }) { survey ->
                SurveyCard(survey = survey, onClick = { onOpenSurvey(survey.id) }, showState = true)
            }
        }

        if (home.recentDrafts.isNotEmpty()) {
            item {
                SectionHeader("Recent drafts", actionLabel = "See all", onAction = { onOpenSurveys(SurveyState.Draft) })
            }
            items(home.recentDrafts, key = { "draft-${it.id}" }) { survey ->
                SurveyCard(survey = survey, onClick = { onOpenSurvey(survey.id) }, showState = true)
            }
        }
    }
}
