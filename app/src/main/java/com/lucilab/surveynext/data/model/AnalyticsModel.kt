package com.lucilab.surveynext.data.model

import com.google.gson.annotations.SerializedName

data class AnalyticsModel(
    val survey: SurveyModel,
    val answers: List<AnswerModel>,
    val summary: List<QuestionSummaryModel>,
    @SerializedName("answer_total")
    val answerTotal: Int,
)

/** Only the fields that apply to the question's type are populated. */
data class QuestionSummaryModel(
    @SerializedName("question_id")
    val questionId: Int,
    val text: String,
    @SerializedName("question_type")
    val questionType: String,
    @SerializedName("response_count")
    val responseCount: Int,
    @SerializedName("option_counts")
    val optionCounts: List<OptionCountModel>? = null,
    // Five buckets, ratings 1..5
    @SerializedName("rating_counts")
    val ratingCounts: List<Int>? = null,
    @SerializedName("average_rating")
    val averageRating: Double? = null,
    @SerializedName("text_responses")
    val textResponses: List<String>? = null,
) {
    val type: QuestionType get() = QuestionType.from(questionType)
}

data class OptionCountModel(
    val option: String,
    val count: Int,
)
