package com.etds.hourglass.ui.presentation.common

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import com.etds.hourglass.ui.viewmodel.GameViewModelProtocol

private val ButtonShapeRadius = 16.dp

@Composable
fun PauseView(
    viewModel: GameViewModelProtocol = hiltViewModel()
) {
    val isPaused by viewModel.isGamePaused.collectAsState()
    val pauseScreenAlpha by animateFloatAsState(
        targetValue = if (isPaused) 1.0F else 0.0F,
        animationSpec = tween(
            durationMillis = 500,
            easing = LinearEasing
        ), label = "Pause Alpha"
    )


    Box(
        modifier = Modifier
            .fillMaxSize()
            .zIndex(1.0F)
            .alpha(pauseScreenAlpha)
            .blockInteraction(enabled = true),
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
