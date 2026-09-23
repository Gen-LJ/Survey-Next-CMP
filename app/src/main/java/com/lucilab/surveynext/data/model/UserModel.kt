package com.lucilab.surveynext.data.model

import com.google.gson.annotations.SerializedName

data class UserModel(
    val id: Int,
    val name: String,
    val email: String,
    val role: String,
    val points: Int,
    @SerializedName("pending_points")
    val pendingPoints: Int,
    @SerializedName("country_id")
    val countryId: Int,
    @SerializedName("region_id")
    val regionId: Int,
) {
    val userRole: Role get() = Role.from(role)
}

enum class Role(val value: String, val label: String) {
    Interviewer("interviewer", "Interviewer"),
    Respondent("respondent", "Respondent"),
    Admin("admin", "Admin");

    companion object {
        fun from(value: String): Role = entries.firstOrNull { it.value == value } ?: Respondent
    }
}
