package com.lucilab.surveynext.data.model

data class CountryModel (
    val id: UInt,
    val name: String,
    val code: String,
    val regions: List<RegionModel>
)

