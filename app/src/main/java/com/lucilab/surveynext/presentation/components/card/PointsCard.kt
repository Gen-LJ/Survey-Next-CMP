package com.lucilab.surveynext.presentation.components.card

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Stars
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.lucilab.surveynext.presentation.common.formatPoints
import com.lucilab.surveynext.presentation.theme.HeroEnd
import com.lucilab.surveynext.presentation.theme.HeroStart

/**
 * Gradient hero showing a points balance.
 *
 * @param caption line under the balance, e.g. escrowed points
 */
@Composable
fun PointsCard(
    label: String,
    points: Int,
    modifier: Modifier = Modifier,
    caption: String? = null,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .background(Brush.linearGradient(listOf(HeroStart, HeroEnd)))
            .padding(20.dp)
    ) {
        // Decorative rings
        Box(
            Modifier
                .align(Alignment.TopEnd)
                .padding(end = 0.dp)
                .size(120.dp)
                .background(Color.White.copy(alpha = 0.08f), CircleShape)
        )
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(label, style = MaterialTheme.typography.labelLarge, color = Color.White.copy(alpha = 0.8f))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Rounded.Stars, contentDescription = null, tint = Color(0xFFFFD27A), modifier = Modifier.size(32.dp))
                Text(
                    formatPoints(points),
                    style = MaterialTheme.typography.displaySmall,
                    color = Color.White
                )
                Text("pts", style = MaterialTheme.typography.titleMedium, color = Color.White.copy(alpha = 0.8f))
            }
            if (caption != null) {
                Text(caption, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.8f))
            }
        }
    }
}
