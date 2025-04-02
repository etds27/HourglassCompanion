package com.etds.hourglass.ui.presentation.time

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
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
    showArrow: Boolean = false,
    modifier: Modifier = Modifier
) {

    val turnTimeString = timeToString(remainingTime, includeMillis)

    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = turnTimeString,
            fontSize = textSize,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .background(Color.Blue)
                .then(modifier)
        )
        if (showArrow) {
            Icon(imageVector = Icons.Default.ArrowDownward, contentDescription = "Turn Timer Icon")
        }
    }
}