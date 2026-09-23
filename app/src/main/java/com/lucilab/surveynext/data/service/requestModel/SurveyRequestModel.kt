package com.lucilab.surveynext.data.service.requestModel

import com.google.gson.annotations.SerializedName

data class CreateSurveyRequestModel(
    val title: String,
    val description: String,
    @SerializedName("category_id")
    val categoryId: Int,
    @SerializedName("country_id")
    val countryId: Int,
    @SerializedName("region_id")
    val regionId: Int,
    val minutes: Int,
)

data class EditSurveyInfoRequestModel(
    val title: String,
    val description: String,
    val minutes: Int,
    @SerializedName("category_id")
    val categoryId: Int,
)

data class PublishSurveyRequestModel(
    @SerializedName("expected_answer_counts")
    val expectedAnswerCounts: Int,
    @SerializedName("points_per_answer")
    val pointsPerAnswer: Int,
)

data class PauseSurveyRequestModel(
    val paused: Boolean,
)

data class AddQuestionRequestModel(
    val text: String,
    @SerializedName("question_type")
    val questionType: String,
    @SerializedName("allow_multi_answer")
    val allowMultiAnswer: Boolean,
    val options: List<String>? = null,
)

data class EditQuestionRequestModel(
    val text: String,
    // Null leaves the existing choices untouched; Gson omits it.
    val options: List<String>? = null,
)

data class SubmitAnswerRequestModel(
    val answers: List<UserAnswerRequestModel>,
)

data class UserAnswerRequestModel(
    @SerializedName("question_id")
    val questionId: Int,
    val responses: List<String>,
)
