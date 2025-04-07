package com.etds.hourglass.ui.presentation.common

import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.input.pointer.pointerInput

fun Modifier.blockInteraction(enabled: Boolean): Modifier = composed {
    if (!enabled) return@composed this
    this.pointerInput(Unit) {
        while (true) {
            awaitPointerEventScope {
                val event = awaitPointerEvent()
                event.changes.forEach { it.consume() }
            }
        }
    }
}