package com.etds.hourglass.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.etds.hourglass.data.game.SequentialGameRepository
import com.etds.hourglass.data.game.local.db.entity.SequentialSettingsEntity
import com.etds.hourglass.model.Device.LocalDevice
import com.etds.hourglass.model.Game.Round
import com.etds.hourglass.model.Player.Player
import com.etds.hourglass.util.CountDownTimer
import com.etds.hourglass.util.Timer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

interface SequentialModeViewModelProtocol: GameViewModelProtocol {
    // MARK: Setting Flows
    val autoStartTurnTimer: StateFlow<Boolean>
    val turnTimerDuration: StateFlow<Long>
    val autoStartTotalTurnTimer: StateFlow<Boolean>
    val totalTurnTimerDuration: StateFlow<Long>


    val enforceTimer: StateFlow<Boolean>
    val enforceTotalTimer: StateFlow<Boolean>

    // MARK: Game Flows
    val turnTimer: StateFlow<CountDownTimer?>
    val openTurnTimer: StateFlow<Timer?>
    val totalTurnTimer: StateFlow<CountDownTimer?>
    val openTotalTurnTimer: StateFlow<Timer?>

    val activePlayer: StateFlow<Player?>

    // Mark: Game Functions
    fun setAutoEnforceTurnTimer(value: Boolean)
    fun setAutoEnforceTotalTurnTimer(value: Boolean)
    fun setTurnTimerDuration(value: Number?)
    fun setTotalTurnTimerDuration(value: Number?)
    fun setTurnTimerEnforced(value: Boolean)
    fun setTotalTurnTimerEnforced(value: Boolean)
    fun nextPlayer()
    fun previousPlayer()
    fun reorderPlayers(from: Int, to: Int)
    fun shiftPlayerOrderForward()
    fun shiftPlayerOrderBackward()
}

@HiltViewModel
class SequentialModeViewModel @Inject constructor(
    private val gameRepository: SequentialGameRepository
): GameViewModel(
    gameRepository = gameRepository
), SequentialModeViewModelProtocol {

    // MARK: Setting Flows
    override val enforceTimer: StateFlow<Boolean> = gameRepository.enforceTimer
    override val enforceTotalTimer: StateFlow<Boolean> = gameRepository.enforceTotalTimer

    override val autoStartTurnTimer: StateFlow<Boolean> = gameRepository.autoStartTurnTimer
    override val turnTimerDuration: StateFlow<Long> = gameRepository.turnTimerDuration
    override val autoStartTotalTurnTimer: StateFlow<Boolean> = gameRepository.autoStartTotalTimer
    override val totalTurnTimerDuration: StateFlow<Long> = gameRepository.totalTurnTimerDuration


    // MARK: Game Flows

    override val turnTimer: StateFlow<CountDownTimer?> = gameRepository.turnTimer
    override val openTurnTimer: StateFlow<Timer?> = gameRepository.openTurnTimer
    override val totalTurnTimer: StateFlow<CountDownTimer?> = gameRepository.totalTurnTimer
    override val openTotalTurnTimer: StateFlow<Timer?> = gameRepository.openTotalTurnTimer

    override val activePlayer: StateFlow<Player?> = gameRepository.activePlayer


    override fun setAutoEnforceTurnTimer(value: Boolean) {
        gameRepository.setAutoStartTurnTime(value)
    }

    override fun setAutoEnforceTotalTurnTimer(value: Boolean) {
        gameRepository.setAutoStartTotalTurnTimer(value)
    }

    override fun setTurnTimerDuration(value: Number?) {
        if (value == null) return
        gameRepository.setTurnTimerDuration(value.toLong() * 1000L)
    }

    override fun setTotalTurnTimerDuration(value: Number?) {
        if (value == null) return
        gameRepository.setTotalTurnTimerDuration(value.toLong() * 1000L)
    }

    override fun setTurnTimerEnforced(value: Boolean) {
        gameRepository.setTurnTimerEnforced(value)
    }

    override fun setTotalTurnTimerEnforced(value: Boolean) {
        gameRepository.setTotalTurnTimerEnforced(value)
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

    override fun onInitialize() {}
}


class MockSequentialModeViewModel: MockGameViewModel(), SequentialModeViewModelProtocol {
    private val mutableEnforcedTimer = MutableStateFlow(false)
    override val autoStartTurnTimer: MutableStateFlow<Boolean> = MutableStateFlow(false)
    override val turnTimerDuration: MutableStateFlow<Long> = MutableStateFlow(10000L)
    override val autoStartTotalTurnTimer: MutableStateFlow<Boolean> = MutableStateFlow(false)
    override val totalTurnTimerDuration: MutableStateFlow<Long> = MutableStateFlow(100000L)
    override val enforceTimer: StateFlow<Boolean>
        get() = mutableEnforcedTimer

    private val mutableEnforcedTotalTimer = MutableStateFlow(false)
    override val enforceTotalTimer: StateFlow<Boolean>
        get() = mutableEnforcedTotalTimer
    override val turnTimer: MutableStateFlow<CountDownTimer?> = MutableStateFlow(null)
    override val openTurnTimer: MutableStateFlow<Timer?> = MutableStateFlow(null)
    override val totalTurnTimer: MutableStateFlow<CountDownTimer?> = MutableStateFlow(null)
    override val openTotalTurnTimer: MutableStateFlow<Timer?> = MutableStateFlow(null)

    private val mutableActivePlayer = MutableStateFlow<Player?>(players.value[0])
    override val activePlayer: StateFlow<Player?>
        get() = mutableActivePlayer

    override fun setAutoEnforceTurnTimer(value: Boolean) {
        autoStartTurnTimer.value = value
    }

    override fun setAutoEnforceTotalTurnTimer(value: Boolean) {
        autoStartTotalTurnTimer.value = value
    }

    override fun setTurnTimerDuration(value: Number?) {
        if (value == null) return
        turnTimerDuration.value = value.toLong() * 1000L
    }

    override fun setTotalTurnTimerDuration(value: Number?) {
        if (value == null) return
        totalTurnTimerDuration.value = value.toLong() * 1000L
    }

    override fun setTurnTimerEnforced(value: Boolean) {
        mutableEnforcedTimer.value = value
    }

    override fun setTotalTurnTimerEnforced(value: Boolean) {
        mutableEnforcedTotalTimer.value = value
    }

    override fun nextPlayer() {
        mutableActivePlayer.value = players.value[0]
    }

    override fun previousPlayer() {
        mutableActivePlayer.value = players.value.last()
    }

    override fun reorderPlayers(from: Int, to: Int) {
        mutablePlayers.value = players.value.toMutableList().apply {
            add(to, removeAt(from))
        }
    }

    override fun shiftPlayerOrderForward() {
        mutablePlayers.value = players.value.toMutableList().apply {
            add(0, removeAt(lastIndex))
        }
    }

    override fun shiftPlayerOrderBackward() {
        mutablePlayers.value = players.value.toMutableList().apply {
            add(lastIndex, removeAt(0))
        }
    }

    override fun onInitialize() {}
}