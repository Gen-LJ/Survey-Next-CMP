package com.lucilab.surveynext.data.model

data class CountryModel (
    val id: UInt,
    val name: String,
    val code: String,
    // Only /auth/register-form nests regions; the create-survey form omits them.
    val regions: List<RegionModel>? = null
)
