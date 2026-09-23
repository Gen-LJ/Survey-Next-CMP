package com.lucilab.surveynext.data.model

import com.google.gson.annotations.SerializedName

data class AnswerModel(
    val id: Int,
    @SerializedName("survey_id")
    val surveyId: Int,
    @SerializedName("respondent_id")
    val respondentId: Int,
    @SerializedName("points_earned")
    val pointsEarned: Int,
    @SerializedName("answered_at")
    val answeredAt: String,
    @SerializedName("user_answers")
    val userAnswers: List<UserAnswerModel>,
)

data class UserAnswerModel(
    @SerializedName("question_id")
    val questionId: Int,
    val responses: List<String>,
)

data class AnswerDetailsModel(
    val survey: SurveyModel,
    val answer: AnswerModel,
)
