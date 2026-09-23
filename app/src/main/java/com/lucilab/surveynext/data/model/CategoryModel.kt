package com.lucilab.surveynext.data.model

import com.google.gson.annotations.SerializedName

data class CategoryModel(
    val id: Int,
    val name: String,
)

/**
 * Categories are seeded in a fixed order on the backend, so their ids are stable.
 * Respondents have no endpoint that lists them, so names are resolved from here.
 */
object Categories {
    private val seeded = listOf(
        "Energy & Environment",
        "Gender & Inclusion",
        "Climate Change & Sustainability",
        "Health & Well-being",
        "Education & Literacy",
        "Employment & Labor Market",
        "Income & Poverty",
        "Food Security & Agriculture",
        "Technology & Digital Access",
        "Housing & Infrastructure",
        "Transport & Mobility",
        "Governance & Political Participation",
        "Disaster Preparedness & Response",
        "Social Cohesion & Community Engagement",
        "Consumer Behavior & Market Trends",
    )

    fun nameOf(id: Int): String = seeded.getOrNull(id - 1) ?: "General"
}

data class CreateSurveyFormModel(
    @SerializedName("category_list")
    val categoryList: List<CategoryModel>,
    @SerializedName("country_list")
    val countryList: List<CountryModel>,
)
