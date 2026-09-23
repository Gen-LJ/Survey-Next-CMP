package com.lucilab.surveynext.data.model

import com.google.gson.annotations.SerializedName

data class InterviewerHomeModel(
    val points: Int,
    @SerializedName("draft_count")
    val draftCount: Int,
    @SerializedName("published_count")
    val publishedCount: Int,
    @SerializedName("paused_count")
    val pausedCount: Int,
    @SerializedName("completed_count")
    val completedCount: Int,
    @SerializedName("recent_published")
    val recentPublished: List<SurveyModel>,
    @SerializedName("recent_drafts")
    val recentDrafts: List<SurveyModel>,
)

data class RespondentHomeModel(
    val points: Int,
    @SerializedName("available_count")
    val availableCount: Int,
    @SerializedName("answered_count")
    val answeredCount: Int,
    @SerializedName("saved_count")
    val savedCount: Int,
    @SerializedName("recent_surveys")
    val recentSurveys: List<SurveyModel>,
)
