package com.lucilab.surveynext.data.model

import com.google.gson.annotations.SerializedName

data class PageModel<T>(
    val items: List<T>,
    val meta: PageMetaModel,
)

data class PageMetaModel(
    val page: Int,
    val limit: Int,
    val total: Int,
    @SerializedName("total_pages")
    val totalPages: Int,
    @SerializedName("has_next")
    val hasNext: Boolean,
)
