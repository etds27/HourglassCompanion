package com.etds.hourglass.ui.presentation.time

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.time.DurationUnit
import kotlin.time.toDuration

fun timeToString(
    time: Long, includeMillis: Boolean = true
): String {
    val duration = time.toDuration(DurationUnit.MILLISECONDS)
    val millis = duration.inWholeMilliseconds % 1000 / 100
    val seconds = duration.inWholeSeconds % 60
    val minutes = duration.inWholeMinutes % 60
    val hours = duration.inWholeHours % 24
    val days = duration.inWholeDays % 365
    val components: MutableList<String> = mutableListOf()
    if (days > 0) {
        components.add("%02d".format(days))
    }
    if (hours > 0 || components.isNotEmpty()) {
        components.add("%02d".format(hours))
    }
    if (minutes > 0 || components.isNotEmpty()) {
        components.add("%02d".format(minutes))
    }
    if (seconds > 0 || components.isNotEmpty()) {
        components.add("%02d".format(seconds))
    } else {
        components.add("00")
    }

    var ret: String = ""
    if (includeMillis) {
        ret = components.joinToString(":") + ".%01d".format(millis)
    } else {
        ret = components.joinToString(":")
    }
    return ret
}

@Composable
fun CountDownTimer(
    remainingTime: Long,
    includeMillis: Boolean = true,
    textSize: TextUnit = 20.sp,
    fontColor: Color = Color.Black,
    showArrow: Boolean = false,
    showProgressBar: Boolean = false,
    totalTime: Long = 60000L,
    arrowColor1: Color = Color.Green,
    arrowColor2: Color = Color.Red,
    modifier: Modifier = Modifier
) {

    val turnTimeString = timeToString(remainingTime, includeMillis)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.height(IntrinsicSize.Min)
    ) {
        Text(
            text = turnTimeString,
            color = fontColor,
            fontSize = textSize,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .then(modifier)
        )
        if (showArrow) {
            Icon(
                imageVector = Icons.Default.ArrowDownward,
                contentDescription = "Turn Timer Icon",
                tint = fontColor,
                modifier = Modifier.fillMaxHeight()
            )
        }

        if (showProgressBar) {
            val percent by remember { mutableFloatStateOf((remainingTime / totalTime).toFloat()) }
            // val percent = 0.5F
            val animatedWeight = animateFloatAsState(
                percent,
                animationSpec = tween(
                    10,
                    easing = LinearEasing
                ), label = "progress_bar"
            )
            Column(
                modifier = Modifier
                    .width(20.dp)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(20.dp))
            ) {

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Green)
                        .weight(1 - animatedWeight.value)
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Red)
                        .weight(animatedWeight.value)
                )
            }
        }
    }
}