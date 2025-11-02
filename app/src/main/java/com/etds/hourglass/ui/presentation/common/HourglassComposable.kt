package com.etds.hourglass.ui.presentation.common

import android.util.Log
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.room.Index
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
    circles: Int = 16,
    canvasSize: Dp? = null,
    degreesPerRotation: Int = 450,
    rotations: Int = 4,
    rotationDuration: Int = 3000,
    circleOutlineThickness: Int = 3,
    paused: Boolean = false,
    onCirclePressed: (Int) -> Unit = {}
) {

    require(colors.isNotEmpty()) { "colors must not be empty" }
    require(circles > 0) { "Circles must be a positive number"}
    // require(circles % colors.count() == 0) { "circles must be divisible by the number of colors" }

    var dynamicCanvasSize by remember { mutableStateOf(Size.Zero) }
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
            .pointerInput(Unit) {
                detectTapGestures { tapOffset ->
                    val circleSize = calculateCircleSize(dynamicCanvasSize)

                    (0..<circles).forEach { index ->
                        val circleCenter = calculateCirclePosition(
                            canvasSize = dynamicCanvasSize,
                            index = index,
                            circles = circles
                        )

                        val distance = (circleCenter - tapOffset).getDistance()
                        if (distance <= circleSize) { onCirclePressed(index) }
                    }

                }
            }
    ) {
        dynamicCanvasSize = size
        val circleSize = calculateCircleSize(size)
        val circlesPerColor: Int = circles / colors.count()

        // Determine how many segments will have one more circle than other segments
        val excessSegments: Int = circles % colors.count()
        val segmentLengths = List(colors.count()) { index ->
            if (index < excessSegments) circlesPerColor + 1 else circlesPerColor
        }

        rotate(position.value) {
            var currentIndex = 0
            segmentLengths.forEachIndexed { segmentIndex, length ->
                (0..<length).forEach { colorIndex ->
                    val circleCenter = calculateCirclePosition(
                        canvasSize = size,
                        index = currentIndex,
                        circles = circles
                    )

                    drawCircle(
                        color = Color.Black,
                        center = circleCenter,
                        radius = circleSize + circleOutlineThickness,
                    )

                    val color = colors[segmentIndex]

                    Log.d(
                        "HourGlass",
                        "Drawing circle $currentIndex, color: $color, center: $circleCenter"
                    )
                    drawCircle(
                        color = color,
                        center = circleCenter,
                        radius = circleSize,
                    )
                    currentIndex += 1
                }
            }
        }
    }
}

private fun calculateCircleSize(canvasSize: Size): Float {
    return canvasSize.width / 20
}

private fun calculateCirclePosition(canvasSize: Size, index: Int, circles: Int): Offset {
    val canvasWidth = canvasSize.width
    val circleSize = calculateCircleSize(canvasSize)
    val distance = canvasWidth / 2F - circleSize * 2

    val angle = index.toFloat() / circles * 2 * PI - PI / 2 + PI / circles
    val x = distance * cos(angle).toFloat()
    val y = distance * sin(angle).toFloat()

    return Offset(x = canvasWidth / 2 + x, y = canvasWidth / 2 + y)
}

@Preview
@Composable
private fun HourglassCanvasPreview() {
    HourglassComposable(
        modifier = Modifier
            .padding(32.dp),
        colors = listOf(
            Color.Red,
            Color.Blue,
        ),
        paused = true,
        onCirclePressed = { index ->
            Log.d("Hourglass", "TEST Circle $index pressed")
        }
    )
}