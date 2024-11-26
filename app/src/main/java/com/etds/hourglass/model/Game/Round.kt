package com.etds.hourglass.model.Game

import com.etds.hourglass.model.Player.Player
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.time.Duration
import java.time.Instant

class Round {
    private val _totalTurns: MutableStateFlow<Int> = MutableStateFlow(0)
    val totalTurns: StateFlow<Int> = _totalTurns

    var playerOrder: List<Player> = listOf()

    var totalActiveTime: Long = 0L
    val totalRoundTime: Long
        get() {
            return if (roundEndTime != null) {
                Duration.between(roundStartTime, roundEndTime).toMillis()
            } else {
                Duration.between(roundStartTime, Instant.now()).toMillis()
            }
        }

    private var _roundStartTime: Instant = Instant.now()
    var roundStartTime: Instant
        get() = _roundStartTime
        set(value) { _roundStartTime = value }

    private var _roundEndTime: Instant? = null
    var roundEndTime: Instant?
        get() = _roundEndTime
        set(value) { _roundEndTime = value }

    fun incrementTotalTurns() {
        _totalTurns.value++
    }

    fun setPlayerOrder(order: List<Player>) {
        playerOrder = order.toList()
    }
}