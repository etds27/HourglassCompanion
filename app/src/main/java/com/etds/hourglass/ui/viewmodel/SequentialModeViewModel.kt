package com.etds.hourglass.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.etds.hourglass.data.game.SequentialGameRepository
import com.etds.hourglass.model.Device.LocalDevice
import com.etds.hourglass.model.Game.Round
import com.etds.hourglass.model.Player.Player
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

interface SequentialModeViewModelProtocol {
    // MARK: Setting Flows
    val enforceTimer: StateFlow<Boolean>
    val enforceTotalTimer: StateFlow<Boolean>

    // MARK: Game Flows
    val timerDuration: StateFlow<Long>
    val totalTimerDuration: StateFlow<Long>
    val activePlayer: StateFlow<Player?>

    // Mark: Game Functions
    fun toggleEnforcedTurnTimer()
    fun toggleEnforcedTotalTurnTimer()
    fun updateTurnTimer(valueStr: String)
    fun updateTotalTurnTimer(valueStr: String)
    fun nextPlayer()
    fun previousPlayer()
    fun reorderPlayers(from: Int, to: Int)
    fun shiftPlayerOrderForward()
    fun shiftPlayerOrderBackward()
}

@Singleton
class SequentialModeViewModel @Inject constructor(
    private val gameRepository: SequentialGameRepository
): GameViewModel(
    gameRepository = gameRepository
), SequentialModeViewModelProtocol {

    // MARK: Setting Flows
    override val enforceTimer: StateFlow<Boolean> = gameRepository.enforceTimer
    override val enforceTotalTimer: StateFlow<Boolean> = MutableStateFlow(false)  // gameRepository.enforceTotalTimer

    // MARK: Game Flows
    override val timerDuration: StateFlow<Long> = gameRepository.timerDuration
    override val totalTimerDuration: StateFlow<Long> = gameRepository.totalTimerDuration
    override val activePlayer: StateFlow<Player?> = MutableStateFlow(null) // gameRepository.activePlayer


    override fun toggleEnforcedTurnTimer() {
        if (enforceTimer.value) {
            gameRepository.setTurnTimerNotEnforced()
        } else {
            gameRepository.setTurnTimerEnforced()
        }
    }

    override fun toggleEnforcedTotalTurnTimer() {
        if (enforceTotalTimer.value) {
            gameRepository.setTotalTurnTimerNotEnforced()
        } else {
            gameRepository.setTotalTurnTimerEnforced()
        }
    }

    override fun updateTurnTimer(valueStr: String) {
        val value = valueStr.toLongOrNull()?.let { it * 1000 } ?: 0L
        gameRepository.setTurnTime(value)
    }

    override fun updateTotalTurnTimer(valueStr: String) {
        val value = valueStr.toLongOrNull()?.let { it * 1000 * 60 } ?: 0L
        gameRepository.setTotalTurnTime(value)
    }

    override fun nextPlayer() {
        gameRepository.nextPlayer()
    }

    override fun previousPlayer() {
        gameRepository.previousPlayer()
    }

    override fun reorderPlayers(from: Int, to: Int) {
        gameRepository.reorderPlayers(from, to)
    }

    override fun shiftPlayerOrderForward() {
        gameRepository.shiftPlayerOrderForward()
    }

    override fun shiftPlayerOrderBackward() {
        gameRepository.shiftPlayerOrderBackward()
    }
}


class MockSequentialModeViewModel: MockGameViewModel(), SequentialModeViewModelProtocol {
    private val mutableEnforcedTimer = MutableStateFlow(false)
    override val enforceTimer: StateFlow<Boolean>
        get() = mutableEnforcedTimer

    private val mutableEnforcedTotalTimer = MutableStateFlow(false)
    override val enforceTotalTimer: StateFlow<Boolean>
        get() = mutableEnforcedTotalTimer

    private val mutableTimerDuration = MutableStateFlow(10000L)
    override val timerDuration: StateFlow<Long>
        get() = mutableTimerDuration

    private val mutableTotalTimerDuration = MutableStateFlow(100000L)
    override val totalTimerDuration: StateFlow<Long>
        get() = mutableTotalTimerDuration

    private val mutableActivePlayer = MutableStateFlow<Player?>(null)
    override val activePlayer: StateFlow<Player?>
        get() = mutableActivePlayer

    override fun toggleEnforcedTurnTimer() {
        mutableEnforcedTimer.value = !mutableEnforcedTimer.value
    }

    override fun toggleEnforcedTotalTurnTimer() {
        mutableEnforcedTotalTimer.value = !mutableEnforcedTotalTimer.value
    }

    override fun updateTurnTimer(valueStr: String) {
        val value = valueStr.toLongOrNull()?.let { it * 1000 } ?: 0L
        mutableTimerDuration.value = value
    }

    override fun updateTotalTurnTimer(valueStr: String) {
        val value = valueStr.toLongOrNull()?.let { it * 1000 } ?: 0L
        mutableTotalTimerDuration.value = value
    }

    override fun nextPlayer() {
        mutableActivePlayer.value = players.value[0]
    }

    override fun previousPlayer() {
        mutableActivePlayer.value = players.value.last()
    }

    override fun reorderPlayers(from: Int, to: Int) {

    }

    override fun shiftPlayerOrderForward() {

    }

    override fun shiftPlayerOrderBackward() {

    }

}