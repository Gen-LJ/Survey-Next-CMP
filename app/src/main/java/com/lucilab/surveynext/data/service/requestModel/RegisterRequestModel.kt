package com.lucilab.surveynext.data.service.requestModel

data class RegisterRequestModel(
    val name: String,
    val email: String,
    val password: String,
    val role: String,
    val countryId: UInt,
    val regionId: UInt
)