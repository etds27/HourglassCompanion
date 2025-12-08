package com.etds.hourglass.ui.presentation.common

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.etds.hourglass.ui.viewmodel.GameViewModelProtocol
import com.etds.hourglass.ui.viewmodel.MockSequentialModeViewModel

private val ButtonShapeRadius = 16.dp

@Composable
fun PauseView(
    viewModel: GameViewModelProtocol = hiltViewModel(),
    onSettingsNavigate: () -> Unit = {}
) {
    // val isPaused by viewModel.isGamePaused.collectAsState()
    val isPaused = true
    val pauseScreenAlpha by animateFloatAsState(
        targetValue = if (isPaused) 1.0F else 0.0F,
        animationSpec = tween(
            durationMillis = 500,
            easing = LinearEasing
        ), label = "Pause Alpha"
    )

    Box(
        modifier = Modifier.fillMaxSize()
    ) {

        Box(
            contentAlignment = Alignment.TopEnd,
            modifier = Modifier.fillMaxSize()
                .blockInteraction(enabled = true),
        ) {
            Button(
                modifier = Modifier
                    .padding(8.dp)
                    .zIndex(2.0F),
                onClick = { onSettingsNavigate() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    contentColor = MaterialTheme.colorScheme.onBackground
                ),
                contentPadding = PaddingValues(0.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings"
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .zIndex(1.0F)
                .alpha(pauseScreenAlpha),
            contentAlignment = Alignment.Center
        ) {

            Column {
                Spacer(
                    Modifier
                        .fillMaxSize()
                        .weight(0.4F)
                )
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Pause",
                    tint = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(0.2F)
                        .clip(RoundedCornerShape(ButtonShapeRadius))
                        .clickable {
                            viewModel.resumeGame()
                        },
                )
                Spacer(
                    Modifier
                        .fillMaxSize()
                        .weight(0.4F)
                )
            }
        }
    }
}

@Preview
@Composable
fun MockPauseView() {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        PauseView(
            viewModel = MockSequentialModeViewModel()
        )
    }
}
