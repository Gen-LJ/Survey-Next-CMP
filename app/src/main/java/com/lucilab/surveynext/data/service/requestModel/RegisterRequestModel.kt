package com.lucilab.surveynext.data.service.requestModel
import com.google.gson.annotations.SerializedName

data class RegisterRequestModel(
    val name: String,
    val email: String,
    val password: String,
    val role: String,
    @SerializedName("country_id")
    val countryId: UInt,

    @SerializedName("region_id")
    val regionId: UInt
)