package com.lucilab.surveynext.presentation.navigation

const val ARG_SURVEY_ID = "surveyId"
const val ARG_POINTS = "points"

sealed class Screen(val route: String) {
    data object Login : Screen("login")
    data object Register : Screen("register")
    data object Main : Screen("main")

    // Interviewer
    data object CreateSurvey : Screen("interviewer/create")
    data object ManageSurvey : Screen("interviewer/survey/{$ARG_SURVEY_ID}") {
        fun create(surveyId: Int) = "interviewer/survey/$surveyId"
    }
    data object Analytics : Screen("interviewer/survey/{$ARG_SURVEY_ID}/analytics") {
        fun create(surveyId: Int) = "interviewer/survey/$surveyId/analytics"
    }

    // Respondent
    data object SurveyInfo : Screen("respondent/survey/{$ARG_SURVEY_ID}") {
        fun create(surveyId: Int) = "respondent/survey/$surveyId"
    }
    data object AnswerSurvey : Screen("respondent/survey/{$ARG_SURVEY_ID}/answer") {
        fun create(surveyId: Int) = "respondent/survey/$surveyId/answer"
    }
    data object AnswerComplete : Screen("respondent/answered/{$ARG_POINTS}") {
        fun create(points: Int) = "respondent/answered/$points"
    }
    data object AnswerDetails : Screen("respondent/completed/{$ARG_SURVEY_ID}") {
        fun create(surveyId: Int) = "respondent/completed/$surveyId"
    }
}
