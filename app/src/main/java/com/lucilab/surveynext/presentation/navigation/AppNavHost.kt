package com.lucilab.surveynext.presentation.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.lucilab.surveynext.presentation.screens.auth.login.LoginScreen
import com.lucilab.surveynext.presentation.screens.auth.register.RegisterScreen
import com.lucilab.surveynext.presentation.screens.interviewer.analytics.AnalyticsScreen
import com.lucilab.surveynext.presentation.screens.interviewer.create.CreateSurveyScreen
import com.lucilab.surveynext.presentation.screens.interviewer.manage.ManageSurveyScreen
import com.lucilab.surveynext.presentation.screens.main.MainScreen
import com.lucilab.surveynext.presentation.screens.respondent.answer.AnswerSurveyScreen
import com.lucilab.surveynext.presentation.screens.respondent.complete.AnswerCompleteScreen
import com.lucilab.surveynext.presentation.screens.respondent.details.AnswerDetailsScreen
import com.lucilab.surveynext.presentation.screens.respondent.info.SurveyInfoScreen

private val surveyIdArgument = listOf(navArgument(ARG_SURVEY_ID) { type = NavType.IntType })

@Composable
fun AppNavHost(sessionViewModel: SessionViewModel = hiltViewModel()) {
    val navController = rememberNavController()
    val user by sessionViewModel.user.collectAsState()
    val startDestination = remember {
        if (sessionViewModel.user.value != null) Screen.Main.route else Screen.Login.route
    }

    fun NavOptionsBuilder.clearBackStack() {
        popUpTo(0) { inclusive = true }
    }

    // Signing out, or the backend rejecting the token, lands back on login.
    LaunchedEffect(user == null) {
        val route = navController.currentDestination?.route
        if (user == null && route != null && route != Screen.Login.route && route != Screen.Register.route) {
            navController.navigate(Screen.Login.route) { clearBackStack() }
        }
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        enterTransition = {
            slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(300)) + fadeIn(tween(300))
        },
        exitTransition = { fadeOut(tween(200)) },
        popEnterTransition = { fadeIn(tween(300)) },
        popExitTransition = {
            slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(300)) + fadeOut(tween(300))
        },
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.Main.route) {
                        clearBackStack()
                    }
                },
                onRegister = { navController.navigate(Screen.Register.route) }
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.navigate(Screen.Main.route) {
                        clearBackStack()
                    }
                },
                onLoginClick = {
                    navController.navigate(Screen.Login.route) {
                        clearBackStack()
                    }
                }
            )
        }

        composable(Screen.Main.route) {
            val current = user ?: return@composable
            MainScreen(
                user = current,
                onCreateSurvey = { navController.navigate(Screen.CreateSurvey.route) },
                onOpenManagedSurvey = { navController.navigate(Screen.ManageSurvey.create(it)) },
                onOpenAvailableSurvey = { navController.navigate(Screen.SurveyInfo.create(it)) },
                onOpenAnsweredSurvey = { navController.navigate(Screen.AnswerDetails.create(it)) },
                onLogout = sessionViewModel::logout,
            )
        }

        composable(Screen.CreateSurvey.route) {
            CreateSurveyScreen(
                onBack = { navController.popBackStack() },
                onCreated = { surveyId ->
                    navController.navigate(Screen.ManageSurvey.create(surveyId)) {
                        popUpTo(Screen.CreateSurvey.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.ManageSurvey.route, arguments = surveyIdArgument) {
            ManageSurveyScreen(
                onBack = { navController.popBackStack() },
                onOpenAnalytics = { navController.navigate(Screen.Analytics.create(it)) },
            )
        }

        composable(Screen.Analytics.route, arguments = surveyIdArgument) {
            AnalyticsScreen(onBack = { navController.popBackStack() })
        }

        composable(Screen.SurveyInfo.route, arguments = surveyIdArgument) {
            SurveyInfoScreen(
                onBack = { navController.popBackStack() },
                onStart = { navController.navigate(Screen.AnswerSurvey.create(it)) },
            )
        }

        composable(Screen.AnswerSurvey.route, arguments = surveyIdArgument) {
            AnswerSurveyScreen(
                onClose = { navController.popBackStack() },
                onSubmitted = { points ->
                    navController.navigate(Screen.AnswerComplete.create(points)) {
                        popUpTo(Screen.Main.route)
                    }
                }
            )
        }

        composable(
            Screen.AnswerComplete.route,
            arguments = listOf(navArgument(ARG_POINTS) { type = NavType.IntType })
        ) { entry ->
            AnswerCompleteScreen(
                points = entry.arguments?.getInt(ARG_POINTS) ?: 0,
                onDone = { navController.popBackStack(Screen.Main.route, inclusive = false) }
            )
        }

        composable(Screen.AnswerDetails.route, arguments = surveyIdArgument) {
            AnswerDetailsScreen(onBack = { navController.popBackStack() })
        }
    }
}
