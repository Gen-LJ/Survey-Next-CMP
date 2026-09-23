package com.lucilab.surveynext.presentation.components

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter

/** Calls [onLoadMore] when the list scrolls within [threshold] items of its end. */
@Composable
fun LoadMoreEffect(listState: LazyListState, threshold: Int = 3, onLoadMore: () -> Unit) {
    val shouldLoad by remember {
        derivedStateOf {
            val info = listState.layoutInfo
            val lastVisible = info.visibleItemsInfo.lastOrNull()?.index ?: return@derivedStateOf false
            info.totalItemsCount > 0 && lastVisible >= info.totalItemsCount - 1 - threshold
        }
    }
    LaunchedEffect(listState) {
        snapshotFlow { shouldLoad }
            .distinctUntilChanged()
            .filter { it }
            .collect { onLoadMore() }
    }
}
