package com.lucilab.surveynext.presentation.components.button.custombutton

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp

@Composable
fun CustomButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false
) {
    val focusManager = LocalFocusManager.current
    val isDark = isSystemInDarkTheme()
    val backgroundColor = if (isDark) Color.Gray else Color.Black
    val contentColor = Color.White

    Button(
        onClick = {
            focusManager.clearFocus()
            onClick()
        },
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor,
            contentColor = contentColor,
            disabledContainerColor = backgroundColor.copy(alpha = 0.8f)
        ),
        enabled = enabled && !isLoading
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                color = contentColor,
                modifier = Modifier
                    .size(24.dp)
                    .padding(2.dp),
                strokeWidth = 2.dp
            )
        } else {
            Text(text)
        }
    }
}
