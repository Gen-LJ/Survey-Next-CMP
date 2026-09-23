package com.lucilab.surveynext.presentation.screens.respondent.complete

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Stars
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.lucilab.surveynext.presentation.common.formatPoints
import com.lucilab.surveynext.presentation.theme.HeroEnd
import com.lucilab.surveynext.presentation.theme.HeroStart
import kotlinx.coroutines.launch

@Composable
fun AnswerCompleteScreen(points: Int, onDone: () -> Unit) {
    val badgeScale = remember { Animatable(0f) }
    var shownPoints by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        launch { badgeScale.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy)) }
        // Count the reward up.
        val counter = Animatable(0f)
        counter.animateTo(points.toFloat(), tween(900)) { shownPoints = value.toInt() }
        shownPoints = points
    }
    BackHandler(onBack = onDone)

    Box(
        Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(HeroStart, HeroEnd)))
            .safeDrawingPadding()
            .padding(24.dp)
    ) {
        Column(
            Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = Color.White,
                modifier = Modifier
                    .size(112.dp)
                    .scale(badgeScale.value)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Rounded.Check, contentDescription = null, tint = HeroStart, modifier = Modifier.size(64.dp))
                }
            }
            Text(
                "Thanks for sharing!",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
                textAlign = TextAlign.Center
            )
            Text(
                "Your answers were submitted.",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.85f),
                textAlign = TextAlign.Center
            )
            if (points > 0) {
                Surface(shape = CircleShape, color = Color.White.copy(alpha = 0.16f)) {
                    Row(
                        Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Rounded.Stars, contentDescription = null, tint = Color(0xFFFFD27A))
                        Text(
                            "+${formatPoints(shownPoints)} points",
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White
                        )
                    }
                }
            }
        }

        Button(
            onClick = onDone,
            colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = HeroStart),
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 54.dp)
                .align(Alignment.BottomCenter)
        ) {
            Text("Back to home", style = MaterialTheme.typography.titleSmall)
        }
    }
}
