package com.etds.hourglass.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.etds.hourglass.data.BLEData.BLERepository
import com.etds.hourglass.data.BLEData.remote.BLERemoteDatasource
import com.etds.hourglass.data.game.GameRepository
import com.etds.hourglass.data.game.local.LocalGameDatasource
import com.etds.hourglass.model.Device.GameDevice
import com.etds.hourglass.model.Player.Player
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class GameViewModel(
): ViewModel() {
    private val gameRepository: GameRepository = GameRepository(
        localGameDatasource = LocalGameDatasource(),
        bluetoothDatasource = BLERemoteDatasource()
    )

    val timerDuration: StateFlow<Int> = gameRepository.timerDuration
    val enforceTimer: StateFlow<Boolean> = gameRepository.enforceTimer
    val activePlayer: StateFlow<Player?> = gameRepository.activePlayer
    val skippedPlayers: StateFlow<Set<Player>> = gameRepository.skippedPlayers
    val players: StateFlow<List<Player>> = gameRepository.players
    val isGamePaused: StateFlow<Boolean> = gameRepository.isPaused

    fun toggleGamePause() {
        if (gameRepository.isPaused.value) {
            gameRepository.resumeGame()
        } else {
            gameRepository.pauseGame()
        }
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

    fun nextPlayer() {
        gameRepository.nextPlayer()
    }

    fun previousPlayer() {
        gameRepository.previousPlayer()
    }
}