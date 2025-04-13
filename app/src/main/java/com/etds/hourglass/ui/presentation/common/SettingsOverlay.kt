package com.etds.hourglass.ui.presentation.common

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp

@Composable
fun TopBarOverlay(
    showSettings: Boolean = true,
    onSettingsNavigate: () -> Unit = {},
    targetColor: Color = MaterialTheme.colorScheme.onBackground,
    showRoundNumber: Boolean = false,
    roundNumber: Int = 0,
) {

    val overlayColor by animateColorAsState(
        targetValue = targetColor,
        animationSpec = tween(
            durationMillis = 500,
            easing = LinearEasing
        ), label = "Settings Color"
    )

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (showRoundNumber) {
                Text(
                    buildAnnotatedString {
                        append("Round: ")
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                            append(roundNumber.toString())
                        }
                    },
                    modifier = Modifier
                        .padding(16.dp),
                    color = overlayColor
                )
            }

            Spacer(modifier = Modifier.fillMaxWidth().weight(1F))

            if (showSettings) {
                Button(
                    modifier = Modifier
                        .padding(8.dp),
                    onClick = { onSettingsNavigate() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        contentColor = overlayColor
                    ),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings"
                    )
                }
            }
        }
    }
}