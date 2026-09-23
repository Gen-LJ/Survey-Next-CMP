package com.lucilab.surveynext.presentation.screens.main

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.automirrored.rounded.Assignment
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.hilt.navigation.compose.hiltViewModel
import com.lucilab.surveynext.data.model.Role
import com.lucilab.surveynext.data.model.UserModel
import com.lucilab.surveynext.presentation.screens.interviewer.home.InterviewerHomeScreen
import com.lucilab.surveynext.presentation.screens.interviewer.surveys.InterviewerSurveysScreen
import com.lucilab.surveynext.presentation.screens.interviewer.surveys.viewmodel.InterviewerSurveysViewModel
import com.lucilab.surveynext.presentation.screens.profile.ProfileScreen
import com.lucilab.surveynext.presentation.screens.respondent.discover.DiscoverScreen
import com.lucilab.surveynext.presentation.screens.respondent.discover.viewmodel.DiscoverViewModel
import com.lucilab.surveynext.presentation.screens.respondent.history.HistoryScreen
import com.lucilab.surveynext.presentation.screens.respondent.home.RespondentHomeScreen

private enum class Tab(val label: String, val icon: ImageVector, val selectedIcon: ImageVector) {
    Home("Home", Icons.Outlined.Home, Icons.Rounded.Home),
    Surveys("Surveys", Icons.AutoMirrored.Outlined.Assignment, Icons.AutoMirrored.Rounded.Assignment),
    Discover("Discover", Icons.Outlined.Explore, Icons.Rounded.Explore),
    History("History", Icons.Outlined.History, Icons.Rounded.History),
    Account("Account", Icons.Outlined.AccountCircle, Icons.Rounded.AccountCircle),
}

private fun tabsFor(role: Role) = when (role) {
    Role.Interviewer -> listOf(Tab.Home, Tab.Surveys, Tab.Account)
    Role.Respondent -> listOf(Tab.Home, Tab.Discover, Tab.History, Tab.Account)
    Role.Admin -> listOf(Tab.Account)
}

@Composable
fun MainScreen(
    user: UserModel,
    onCreateSurvey: () -> Unit,
    onOpenManagedSurvey: (Int) -> Unit,
    onOpenAvailableSurvey: (Int) -> Unit,
    onOpenAnsweredSurvey: (Int) -> Unit,
    onLogout: () -> Unit,
) {
    val tabs = tabsFor(user.userRole)
    var selected by rememberSaveable { mutableStateOf(Tab.Home.name) }
    val current = tabs.firstOrNull { it.name == selected } ?: tabs.first()

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        bottomBar = {
            if (tabs.size > 1) {
                NavigationBar {
                    tabs.forEach { tab ->
                        NavigationBarItem(
                            selected = tab == current,
                            onClick = { selected = tab.name },
                            icon = { Icon(if (tab == current) tab.selectedIcon else tab.icon, contentDescription = null) },
                            label = { Text(tab.label) }
                        )
                    }
                }
            }
        }
    ) { padding ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Crossfade(targetState = current, label = "tab") { tab ->
                when (tab) {
                    Tab.Home -> if (user.userRole == Role.Interviewer) {
                        // Shares the Surveys tab's view model, so stat cards can preset its filter.
                        val surveysViewModel: InterviewerSurveysViewModel = hiltViewModel()
                        InterviewerHomeScreen(
                            userName = user.name,
                            onCreateSurvey = onCreateSurvey,
                            onOpenSurvey = onOpenManagedSurvey,
                            onOpenSurveys = { state ->
                                surveysViewModel.selectFilter(state)
                                selected = Tab.Surveys.name
                            }
                        )
                    } else {
                        val discoverViewModel: DiscoverViewModel = hiltViewModel()
                        RespondentHomeScreen(
                            userName = user.name,
                            onOpenSurvey = onOpenAvailableSurvey,
                            onOpenDiscover = { showSaved ->
                                discoverViewModel.showSaved(showSaved)
                                selected = Tab.Discover.name
                            },
                            onOpenHistory = { selected = Tab.History.name }
                        )
                    }

                    Tab.Surveys -> InterviewerSurveysScreen(
                        onCreateSurvey = onCreateSurvey,
                        onOpenSurvey = onOpenManagedSurvey
                    )

                    Tab.Discover -> DiscoverScreen(onOpenSurvey = onOpenAvailableSurvey)
                    Tab.History -> HistoryScreen(onOpenSurvey = onOpenAnsweredSurvey)
                    Tab.Account -> ProfileScreen(onLogout = onLogout)
                }
            }
        }
    }
}
