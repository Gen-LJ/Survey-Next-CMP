package com.lucilab.surveynext.presentation.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/** Last item of a paged list: a spinner while the next page loads, or a retry. */
@Composable
fun ListFooter(isLoadingMore: Boolean, error: String?, onRetry: () -> Unit) {
    Box(
        Modifier
            .fillMaxWidth()
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        when {
            isLoadingMore -> CircularProgressIndicator(Modifier.padding(8.dp))
            error != null -> TextButton(onClick = onRetry) { Text("Couldn't load more · Retry") }
        }
    }
}
