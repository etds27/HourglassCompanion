package com.etds.hourglass.ui.presentation.time

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