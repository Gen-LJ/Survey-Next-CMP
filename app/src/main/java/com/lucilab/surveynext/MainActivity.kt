package com.lucilab.surveynext

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.lucilab.surveynext.presentation.navigation.AppNavHost
import com.lucilab.surveynext.presentation.theme.SurveyNextTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SurveyNextTheme {
                AppNavHost()
            }
        }
    }
}
