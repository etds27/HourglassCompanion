package com.etds.hourglass.ui.presentation.common

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.Dp
import com.etds.hourglass.R
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun HourglassComposable(
    modifier: Modifier = Modifier,
    colors: List<Color> = listOf(
        colorResource(R.color.hourglass_red),
        colorResource(R.color.hourglass_blue),
        colorResource(R.color.hourglass_green),
        colorResource(R.color.hourglass_yellow)
    ),
    canvasSize: Dp? = null,
    degreesPerRotation: Int = 450,
    rotations: Int = 4,
    rotationDuration: Int = 3000,
    circleOutlineThickness: Int = 3,
    paused: Boolean = false
) {

    val position = remember { Animatable(0f) }

    LaunchedEffect(position, paused) {
        launch {
            while (true) {
                if (paused) {
                    // When paused, simply delay the loop and skip animation
                    kotlinx.coroutines.delay(16) // Wait for 16ms before checking again
                    continue
                }
                (1..rotations).forEach { _ ->
                    position.animateTo(
                        targetValue = position.value + degreesPerRotation, animationSpec = tween(
                            durationMillis = rotationDuration
                        )
                    )
                }

                position.animateTo(
                    targetValue = 0F, animationSpec = tween(durationMillis = 0) // Instant reset
                )
            }
        }
    }

    Canvas(
        modifier = modifier
            .aspectRatio(1f)
            .then(
                if (canvasSize != null) Modifier.size(canvasSize)
                else Modifier.fillMaxSize()
            )
            .then(modifier)
    ) {
        val canvasWidth = size.width
        val circleSize = canvasWidth / 20
        val distance = canvasWidth / 2F - circleSize * 2
        val numCircles = 16
        rotate(position.value) {
            (1..numCircles).forEach { index ->
                val angle = index.toFloat() / numCircles * 2 * PI
                val x = distance * cos(angle).toFloat()
                val y = distance * sin(angle).toFloat()

                drawCircle(
                    color = Color.Black,
                    center = Offset(x = canvasWidth / 2 + x, y = canvasWidth / 2 + y),
                    radius = circleSize + circleOutlineThickness,
                )
                drawCircle(
                    color = colors[(index - 1) / (numCircles / 4)],
                    center = Offset(x = canvasWidth / 2 + x, y = canvasWidth / 2 + y),
                    radius = circleSize,
                )
            }
        }
    }
}