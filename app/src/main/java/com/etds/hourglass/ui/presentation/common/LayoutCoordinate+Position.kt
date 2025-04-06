package com.etds.hourglass.ui.presentation.common

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.unit.IntOffset

fun LayoutCoordinates.windowPosition(): IntOffset {
    val windowOffset = this.localToWindow(Offset.Zero)
    return IntOffset(windowOffset.x.toInt(), windowOffset.y.toInt())
}