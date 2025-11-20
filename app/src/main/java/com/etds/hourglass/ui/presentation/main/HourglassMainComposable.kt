package com.etds.hourglass.ui.presentation.main

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.etds.hourglass.model.Device.GameDevice
import com.etds.hourglass.ui.presentation.buzzer_mode.BuzzerModeGameView
import com.etds.hourglass.ui.presentation.buzzer_mode.BuzzerModeSettingsPage
import com.etds.hourglass.ui.presentation.device_personalization.DevicePersonalizationView
import com.etds.hourglass.ui.presentation.gameview.GameView
import com.etds.hourglass.ui.presentation.gameview.SequentialModeSettingsPage
import com.etds.hourglass.ui.presentation.game_mode_selection.GameSelectionView
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
    NavHost(
        navController = navController,
        startDestination = "launch",
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .windowInsetsPadding(WindowInsets.systemBars)
        ) {

        composable("launch") {
            LaunchPage(
                onNavigateToGameSelection = {
                    navController.navigate("game_selection")
                },
                onNavigateToEditDevice = { device: GameDevice ->
                    navController.navigate(route = "device_personalization/${device.address}")
                }
            )

        }

        composable("game_selection") {
            GameSelectionView(
                onGameSelection = { gameMode ->
                    navController.navigate(gameMode)
                }
            )
        }
        composable("game") {
            GameView(
                onSettingsNavigate = {
                    navController.navigate("sequential_settings")
                }
            )
        }

        composable("sequential_settings") {
            SequentialModeSettingsPage(
                onGameViewNavigate = {
                    navController.navigate("game")
                }
            )
        }

        composable("buzzer_game") {
            BuzzerModeGameView(
                onSettingsNavigate = {
                    navController.navigate("buzzer_settings")
                }
            )
        }

        composable("buzzer_settings") {
            BuzzerModeSettingsPage(
                onGameViewNavigate = {
                    navController.navigate("buzzer_game")
                }
            )
        }

        composable("pause") {
            // PauseView()
        }

        composable("settings") {
        }

        composable(
            route = "device_personalization/{deviceId}",
        ) {
                DevicePersonalizationView(
                    onNavigateToLaunchPage = {
                        navController.navigate("launch")
                    }
                )
            }
    }
}