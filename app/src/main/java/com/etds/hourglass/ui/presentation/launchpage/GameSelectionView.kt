package com.etds.hourglass.ui.presentation.launchpage

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.etds.hourglass.R
import com.etds.hourglass.ui.viewmodel.GameDeviceViewModel
import com.etds.hourglass.ui.viewmodel.GameDeviceViewModelProtocol
import com.etds.hourglass.ui.viewmodel.GameViewModelProtocol
import com.etds.hourglass.ui.viewmodel.MockGameDeviceViewModel

@Composable
fun GameSelectionView(
    viewModel: GameDeviceViewModelProtocol = hiltViewModel<GameDeviceViewModel>(),
    onGameSelection: (String) -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        val gameTypes by viewModel.gameTypes.collectAsState()

        Column(
            modifier = Modifier
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            gameTypes.forEach { gameType ->
                val primaryColor =
                    if (isSystemInDarkTheme()) colorResource(gameType.colorValue) else colorResource(
                        gameType.colorValue
                    )
                val secondaryColor =
                    if (isSystemInDarkTheme()) colorResource(gameType.accentColorValue) else colorResource(
                        gameType.accentColorValue
                    )
                Button(
                    onClick = { onGameSelection(gameType.navigationName) },
                    modifier = Modifier
                        .width(240.dp)
                        .height(96.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors().copy(
                        containerColor = primaryColor,
                        contentColor = secondaryColor
                    ),
                    border = BorderStroke(2.dp, secondaryColor),
                    enabled = gameType.enabled,
                ) {
                    Text(
                        gameType.displayName,
                        fontSize = 30.sp
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun GameSelectionViewPreview() {
    GameSelectionView(
        viewModel = MockGameDeviceViewModel()
    )
}