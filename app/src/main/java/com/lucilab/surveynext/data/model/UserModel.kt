package com.lucilab.surveynext.data.model

import com.google.gson.annotations.SerializedName

data class UserModel(
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
)