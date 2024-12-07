package com.etds.hourglass.model.Game

import android.util.Log
import com.etds.hourglass.model.Player.Player
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.time.Duration
import java.time.Instant
import java.util.Dictionary

class Round {
    companion object {
        const val TAG = "Round"
    }

    private val _totalTurns: MutableStateFlow<Int> = MutableStateFlow(0)
    val totalTurns: StateFlow<Int> = _totalTurns

    private var _playerOrder: List<Player> = listOf()
    private var _playerTurnCount = MutableStateFlow<MutableMap<Player, Int>>(mutableMapOf())
    val playerTurnCount: StateFlow<Map<Player, Int>> = _playerTurnCount

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

    fun incrementPlayerTurnCounter(player: Player) {
        if (_playerTurnCount.value.containsKey(player)) {
            _playerTurnCount.value[player] = _playerTurnCount.value[player]!! + 1
        } else {
            Log.d(TAG, "Player not in player order")
        }
        _playerTurnCount.value = _playerTurnCount.value.toMutableMap()
    }

    fun getPlayerTurnCount(player: Player?): Int {
        if (player == null) {
            return -1
        }

        if (_playerTurnCount.value.containsKey(player)) {
            return _playerTurnCount.value[player]!!
        } else {
            Log.d(TAG, "Player not in player order")
        }

        return -1
    }

    fun setPlayerOrder(order: List<Player>) {
        _playerOrder = order.toList()
        _playerTurnCount.value.clear()
        _playerOrder.forEach {
            _playerTurnCount.value[it] = 0
        }
        _playerTurnCount.value = _playerTurnCount.value.toMutableMap()
    }
}