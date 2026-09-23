package com.lucilab.surveynext.presentation.components.rating

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

const val MAX_RATING = 5

/**
 * Five stars. Read-only when [onRatingChange] is null.
 *
 * @param rating 0 for none, otherwise 1..5
 */
@Composable
fun RatingSelector(
    rating: Int,
    modifier: Modifier = Modifier,
    starSize: Dp = 44.dp,
    onRatingChange: ((Int) -> Unit)? = null,
) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(if (onRatingChange != null) 4.dp else 0.dp)) {
        for (value in 1..MAX_RATING) {
            val filled = value <= rating
            val tint by animateColorAsState(
                if (filled) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.outlineVariant,
                label = "star-tint"
            )
            val scale by animateFloatAsState(if (filled) 1f else 0.9f, label = "star-scale")
            val star = @Composable {
                Icon(
                    if (filled) Icons.Rounded.Star else Icons.Rounded.StarOutline,
                    contentDescription = "$value star${if (value > 1) "s" else ""}",
                    tint = tint,
                    modifier = Modifier
                        .size(starSize)
                        .scale(scale)
                )
            }
            if (onRatingChange != null) {
                IconButton(onClick = { onRatingChange(value) }, modifier = Modifier.size(starSize + 8.dp)) { star() }
            } else {
                star()
            }
        }
    }
}

fun ratingLabel(rating: Int): String = when (rating) {
    1 -> "Poor"
    2 -> "Fair"
    3 -> "Good"
    4 -> "Very good"
    5 -> "Excellent"
    else -> "Tap a star to rate"
}
