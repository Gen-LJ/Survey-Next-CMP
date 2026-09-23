package com.lucilab.surveynext.presentation.theme

import androidx.compose.ui.graphics.Color

// Brand: an indigo primary with a warm gold accent for points and rewards.
val Indigo10 = Color(0xFF000F5C)
val Indigo20 = Color(0xFF0A1F8F)
val Indigo30 = Color(0xFF2536B8)
val Indigo40 = Color(0xFF4052D6)
val Indigo80 = Color(0xFFBCC2FF)
val Indigo90 = Color(0xFFDFE0FF)

val Slate10 = Color(0xFF181A2C)
val Slate30 = Color(0xFF434659)
val Slate40 = Color(0xFF5B5D72)
val Slate80 = Color(0xFFC4C5DD)
val Slate90 = Color(0xFFE0E1F9)

val Gold10 = Color(0xFF271900)
val Gold20 = Color(0xFF412D00)
val Gold30 = Color(0xFF5E4200)
val Gold40 = Color(0xFF7C5800)
val Gold80 = Color(0xFFF8BD48)
val Gold90 = Color(0xFFFFDEA8)

val Red40 = Color(0xFFBA1A1A)
val Red80 = Color(0xFFFFB4AB)
val Red90 = Color(0xFFFFDAD6)
val Red10 = Color(0xFF410002)
val Red20 = Color(0xFF690005)
val Red30 = Color(0xFF93000A)

val NeutralLight = Color(0xFFFAF9FF)
val NeutralDark = Color(0xFF121318)

// Hero gradient used behind balances.
val HeroStart = Color(0xFF4052D6)
val HeroEnd = Color(0xFF7B4DDB)

/** Background and content pair for a status badge. */
data class StatusColor(val container: Color, val content: Color)

object StatusPalette {
    val draftLight = StatusColor(Color(0xFFE8E7EF), Color(0xFF45464F))
    val draftDark = StatusColor(Color(0xFF34343A), Color(0xFFC6C5D0))
    val liveLight = StatusColor(Color(0xFFD5F5DF), Color(0xFF0B6B35))
    val liveDark = StatusColor(Color(0xFF14432A), Color(0xFF8FDDAB))
    val pausedLight = StatusColor(Color(0xFFFFE9C7), Color(0xFF7A4B00))
    val pausedDark = StatusColor(Color(0xFF4A3100), Color(0xFFFFC56B))
    val completedLight = StatusColor(Color(0xFFDFE0FF), Color(0xFF2536B8))
    val completedDark = StatusColor(Color(0xFF26307A), Color(0xFFBCC2FF))
}
