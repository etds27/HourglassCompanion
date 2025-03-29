package com.etds.hourglass.ui.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.etds.hourglass.data.game.GameRepository
import com.etds.hourglass.model.Game.Round
import com.etds.hourglass.model.Player.Player
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

@HiltViewModel
class GameViewModel @Inject constructor(
    private val gameRepository: GameRepository
) : ViewModel() {

    val timerDuration: StateFlow<Long> = gameRepository.timerDuration
    val totalTimerDuration: StateFlow<Long> = gameRepository.totalTimerDuration
    val enforceTimer: StateFlow<Boolean> = gameRepository.enforceTimer
    val enforceTotalTimer: StateFlow<Boolean> = MutableStateFlow(false)  // gameRepository.enforceTotalTimer
    val activePlayer: StateFlow<Player?> = MutableStateFlow(null) // gameRepository.activePlayer
    val skippedPlayers: StateFlow<Set<Player>> = gameRepository.skippedPlayers
    val players: StateFlow<List<Player>> = gameRepository.players
    val isGamePaused: StateFlow<Boolean> = gameRepository.isPaused
    val turnTime: StateFlow<Long> = gameRepository.elapsedTurnTime
    val totalTurnTime: StateFlow<Long> = gameRepository.totalElapsedTurnTime

    val currentRoundNumber: StateFlow<Int> = gameRepository.currentRoundNumber
    val currentRound: StateFlow<Round> = gameRepository.currentRound

    val totalTurns: StateFlow<Int> = gameRepository.totalTurnCount
    val gameStartTime = gameRepository.startTime

    fun startGame() {
        gameRepository.updatePlayersList()
        gameRepository.startGame()
    }

    fun quitGame() {
        gameRepository.quitGame()
    }

    fun toggleGamePause() {
        if (gameRepository.isPaused.value) {
            resumeGame()
        } else {
            pauseGame()
        }
    }

    fun endRound() {
        gameRepository.endRound()
    }

    fun pauseGame() {
        gameRepository.pauseGame()
    }

    fun resumeGame() {
        gameRepository.resumeGame()
    }

    fun toggleSkipped(player: Player) {
        if (skippedPlayers.value.contains(player)) {
            gameRepository.setUnskippedPlayer(player)
        } else {
            gameRepository.setSkippedPlayer(player)
        }
    }

    fun toggleEnforcedTurnTimer() {
        if (enforceTimer.value) {
            gameRepository.setTurnTimerNotEnforced()
        } else {
            gameRepository.setTurnTimerEnforced()
        }
    }

    fun toggleEnforcedTotalTurnTimer() {
        if (enforceTotalTimer.value) {
            // gameRepository.setTotalTurnTimerNotEnforced()
        } else {
            // gameRepository.setTotalTurnTimerEnforced()
        }
    }

    fun updateTurnTimer(valueStr: String) {
        val value = valueStr.toLongOrNull()?.let { it * 1000 } ?: 0L
        gameRepository.setTurnTime(value)
    }

    fun updateTotalTurnTimer(valueStr: String) {
        val value = valueStr.toLongOrNull()?.let { it * 1000 * 60 } ?: 0L
        gameRepository.setTotalTurnTime(value)
    }

    fun nextPlayer() {
        // gameRepository.nextPlayer()
    }

    fun previousPlayer() {
        // gameRepository.previousPlayer()
    }

    fun updatePlayerName(player: Player, name: String) {
        player.name = name
    }

    fun removePlayer(player: Player) {
        gameRepository.removePlayer(player)
    }

    fun reorderPlayers(from: Int, to: Int) {
        // gameRepository.reorderPlayers(from, to)
    }

    fun shiftPlayerOrderForward() {
        // gameRepository.shiftPlayerOrderForward()
    }

    fun shiftPlayerOrderBackward() {
        // gameRepository.shiftPlayerOrderBackward()
    }

    companion object {
        const val TAG = "GameViewModel"
    }
}

/*
class GameViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(
        modelClass: Class<T>,
        extras: CreationExtras
    ): T {
        return GameViewModel(
            context = context
        ) as T
    }
}
*/