package com.lucilab.surveynext.data.model

import com.google.gson.annotations.SerializedName

/**
 * Covers the backend's summary, detail and answered-survey shapes: Go embeds the
 * summary struct in the others, so they all arrive flat.
 */
data class SurveyModel(
    val id: Int,
    val title: String,
    val description: String,
    @SerializedName("category_id")
    val categoryId: Int,
    @SerializedName("country_id")
    val countryId: Int,
    @SerializedName("region_id")
    val regionId: Int,
    @SerializedName("creator_id")
    val creatorId: Int,
    val minutes: Int,
    @SerializedName("expected_answer_counts")
    val expectedAnswerCounts: Int,
    @SerializedName("points_per_answer")
    val pointsPerAnswer: Int,
    @SerializedName("pending_points")
    val pendingPoints: Int,
    @SerializedName("total_points")
    val totalPoints: Int,
    @SerializedName("answer_count")
    val answerCount: Int,
    @SerializedName("question_count")
    val questionCount: Int,
    val state: String,
    @SerializedName("published_at")
    val publishedAt: String? = null,
    @SerializedName("completed_at")
    val completedAt: String? = null,
    @SerializedName("last_modified_at")
    val lastModifiedAt: String? = null,
    // Detail responses only
    val questions: List<QuestionModel>? = null,
    // Respondent's completed list only
    @SerializedName("answered_at")
    val answeredAt: String? = null,
) {
    val surveyState: SurveyState get() = SurveyState.from(state)

    val categoryName: String get() = Categories.nameOf(categoryId)

    val progress: Float
        get() = if (expectedAnswerCounts == 0) 0f
        else (answerCount.toFloat() / expectedAnswerCounts).coerceIn(0f, 1f)

    val spotsLeft: Int get() = (expectedAnswerCounts - answerCount).coerceAtLeast(0)
}

enum class SurveyState(val value: String, val label: String) {
    Draft("draft", "Draft"),
    Published("published", "Live"),
    Paused("paused", "Paused"),
    Completed("completed", "Completed");

    companion object {
        fun from(value: String): SurveyState = entries.firstOrNull { it.value == value } ?: Draft
    }
}

data class QuestionModel(
    val id: Int,
    @SerializedName("survey_id")
    val surveyId: Int,
    val text: String,
    @SerializedName("question_type")
    val questionType: String,
    @SerializedName("allow_multi_answer")
    val allowMultiAnswer: Boolean,
    val position: Int,
    val options: List<OptionModel>? = null,
) {
    val type: QuestionType get() = QuestionType.from(questionType)
}

data class OptionModel(
    val id: Int,
    val text: String,
    val position: Int,
)

enum class QuestionType(val value: String, val label: String) {
    MultipleChoice("multiple_choice", "Multiple choice"),
    Rating("rating", "Rating"),
    TextInput("text_input", "Text answer");

    companion object {
        fun from(value: String): QuestionType = entries.firstOrNull { it.value == value } ?: TextInput
    }
}
