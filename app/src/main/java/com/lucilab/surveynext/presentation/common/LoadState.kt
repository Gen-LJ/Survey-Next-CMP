package com.lucilab.surveynext.presentation.common

/** State of a screen whose content comes from a single request. */
sealed interface LoadState<out T> {
    data object Loading : LoadState<Nothing>
    data class Success<T>(val data: T) : LoadState<T>
    data class Error(val message: String) : LoadState<Nothing>
}

fun <T> Result<T>.toLoadState(): LoadState<T> = fold(
    onSuccess = { LoadState.Success(it) },
    onFailure = { LoadState.Error(it.message ?: "Something went wrong") }
)

/** A list filled page by page. */
data class PagedList<T>(
    val items: List<T> = emptyList(),
    val page: Int = 0,
    val hasNext: Boolean = true,
    val isRefreshing: Boolean = false,
    val isLoadingMore: Boolean = false,
    val error: String? = null,
) {
    val isInitialLoad: Boolean get() = page == 0 && error == null
}

const val PAGE_SIZE = 10
