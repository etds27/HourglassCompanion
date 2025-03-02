package com.etds.hourglass.ui.presentation.main

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.etds.hourglass.ui.presentation.gameview.GameView
import com.etds.hourglass.ui.presentation.pause.PauseView
import com.etds.hourglass.ui.presentation.launchpage.LaunchPage

@Composable
fun HourglassMainComposable(
    context: Context,
) {
    val navController = rememberNavController()
    AppNavHost(navController, context)
}


@Composable
fun AppNavHost(navController: NavHostController, context: Context) {
    // val gameDeviceViewModel: GameDeviceViewModel =
    NavHost(navController = navController, startDestination = "launch") {

        composable("launch") {
            LaunchPage(
                context,
                onNavigateToGame = {
                    navController.navigate("game")
                }
            )
        }
        composable("game") {
            GameView(context)
        }

        composable("pause") {
            PauseView()
        }

        composable("settings") {
        }
    }
}