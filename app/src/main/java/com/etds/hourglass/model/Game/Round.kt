package com.etds.hourglass.model.Game

import android.util.Log
import com.etds.hourglass.model.Player.Player
import com.etds.hourglass.util.Timer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.time.Duration
import java.time.Instant

class Round(
    val scope: CoroutineScope
) {
    companion object {
        const val TAG = "Round"
    }

    private val _totalTurns: MutableStateFlow<Int> = MutableStateFlow(0)
    val totalTurns: StateFlow<Int> = _totalTurns

    private var _playerOrder: List<Player> = listOf()
    private var _playerTurnCount = MutableStateFlow<MutableMap<Player, Int>>(mutableMapOf())
    val playerTurnCount: StateFlow<Map<Player, Int>> = _playerTurnCount

    val roundTimer: Timer = Timer(scope)
    val roundDuration: StateFlow<Long> = roundTimer.timeFlow

    var activeRoundTimer: Timer = Timer(scope)
    val activeRoundDuration: StateFlow<Long> = roundTimer.timeFlow

    val mutableIsActive: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val isActive: StateFlow<Boolean> = mutableIsActive

    val mutableHasStarted: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val hasStarted: StateFlow<Boolean> = mutableHasStarted

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

    fun endRound() {
        roundTimer.pause()
        activeRoundTimer.pause()
        mutableIsActive.value = false
    }

    fun startRound() {
        roundTimer.start()
        activeRoundTimer.start()
        mutableHasStarted.value = true
        mutableIsActive.value = true
    }
}