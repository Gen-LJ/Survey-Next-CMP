package com.lucilab.surveynext.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.*
import com.lucilab.surveynext.presentation.screens.auth.register.RegisterScreen
import com.lucilab.surveynext.presentation.screens.auth.login.LoginScreen
import com.lucilab.surveynext.presentation.screens.home.HomeScreen

@Composable
fun AppNavHost() {
    val navController = rememberNavController()

    fun NavOptionsBuilder.clearBackStack() {
        popUpTo(0) { inclusive = true }
    }

    NavHost(navController = navController, startDestination = Screen.Login.route) {
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.Home.route) {
                        clearBackStack()
                    }
                },
                onRegister = { navController.navigate(Screen.Register.route) }
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                onRegister = { navController.navigate(Screen.Home.route) },
                onLoginClick = {
                    navController.navigate(Screen.Login.route) {
                        clearBackStack()
                    }
                }
            )
        }

        composable(Screen.Home.route) {
            HomeScreen()
        }
    }
}
