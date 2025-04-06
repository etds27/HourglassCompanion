package com.etds.hourglass.ui.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.etds.hourglass.data.game.BuzzerGameRepository
import com.etds.hourglass.data.game.GameRepository
import com.etds.hourglass.model.Device.LocalDevice
import com.etds.hourglass.model.Game.Round
import com.etds.hourglass.model.Player.Player
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOf
import java.time.Instant
import javax.inject.Inject


interface GameViewModelProtocol {
    val skippedPlayers: StateFlow<Set<Player>>
    val players: StateFlow<List<Player>>
    val isGamePaused: StateFlow<Boolean>
    val turnTime: StateFlow<Long>
    val totalTurnTime: StateFlow<Long>

    val currentRoundNumber: StateFlow<Int>
    val currentRound: StateFlow<Round>

    val totalTurns: StateFlow<Int>
    val gameStartTime: Instant

    fun startGame()
    fun quitGame()
    fun toggleGamePause()
    fun endRound()
    fun pauseGame()
    fun resumeGame()
    fun toggleSkipped(player: Player)
    fun updatePlayerName(player: Player, name: String)
    fun removePlayer(player: Player)
}

// @HiltViewModel
abstract class GameViewModel(
    private val gameRepository: GameRepository
) : ViewModel(), GameViewModelProtocol {

    override val skippedPlayers: StateFlow<Set<Player>> = gameRepository.skippedPlayers
    override val players: StateFlow<List<Player>> = gameRepository.players
    override val isGamePaused: StateFlow<Boolean> = gameRepository.isPaused
    override val turnTime: StateFlow<Long> = gameRepository.elapsedTurnTime
    override val totalTurnTime: StateFlow<Long> = gameRepository.totalElapsedTurnTime

    override  val currentRoundNumber: StateFlow<Int> = gameRepository.currentRoundNumber
    override val currentRound: StateFlow<Round> = gameRepository.currentRound

    override val totalTurns: StateFlow<Int> = gameRepository.totalTurnCount
    override val gameStartTime = gameRepository.startTime

    override fun startGame() {
        gameRepository.updatePlayersList()
        gameRepository.startGame()
    }

    override fun quitGame() {
        gameRepository.quitGame()
    }

    override fun toggleGamePause() {
        if (gameRepository.isPaused.value) {
            resumeGame()
        } else {
            pauseGame()
        }
    }

    override fun endRound() {
        gameRepository.endRound()
    }

    override fun pauseGame() {
        gameRepository.pauseGame()
    }

    override fun resumeGame() {
        gameRepository.resumeGame()
    }

    override fun toggleSkipped(player: Player) {
        if (skippedPlayers.value.contains(player)) {
            gameRepository.setUnskippedPlayer(player)
        } else {
            gameRepository.setSkippedPlayer(player)
        }
    }

    override fun updatePlayerName(player: Player, name: String) {
        player.name = name
    }

    override fun removePlayer(player: Player) {
        gameRepository.removePlayer(player)
    }

    companion object {
        const val TAG = "GameViewModel"
    }
}

abstract class MockGameViewModel: ViewModel(), GameViewModelProtocol {
    private val mutableSkippedPlayers = MutableStateFlow(setOf<Player>())
    override val skippedPlayers: StateFlow<Set<Player>> = mutableSkippedPlayers

    private val mutablePlayers = MutableStateFlow(listOf(
        Player(name = "Ethan", device = LocalDevice()),
        Player(name = "Haley", device = LocalDevice()),
        Player(name = "Ethan2", device = LocalDevice()),
        Player(name = "Haley2", device = LocalDevice()),
        Player(name = "Ethan3", device = LocalDevice()),
        Player(name = "Haley3", device = LocalDevice()),
    ))
    override val players: StateFlow<List<Player>> = mutablePlayers

    private val mutableGamePaused = MutableStateFlow(false)
    override val isGamePaused: StateFlow<Boolean> = mutableGamePaused

    private val mutableTurnTime = MutableStateFlow(0L)
    override val turnTime: StateFlow<Long> = mutableTurnTime

    private val mutableTotalTurnTime = MutableStateFlow(0L)
    override val totalTurnTime: StateFlow<Long> = mutableTotalTurnTime

    private val mutableCurrentRoundNumber = MutableStateFlow(0)
    override val currentRoundNumber: StateFlow<Int> = mutableCurrentRoundNumber

    private val mutableCurrentRound = MutableStateFlow(Round())
    override val currentRound: StateFlow<Round> = mutableCurrentRound

    private val mutableTotalTurns = MutableStateFlow(0)
    override val totalTurns: StateFlow<Int> = mutableTotalTurns

    override val gameStartTime: Instant = Instant.now()

    override fun startGame() {
    }

    override fun quitGame() {
    }

    override fun toggleGamePause() {
        mutableGamePaused.value = !mutableGamePaused.value
    }

    override fun endRound() {
        mutableCurrentRoundNumber.value++
    }

    override fun pauseGame() {
        mutableGamePaused.value = true
    }

    override fun resumeGame() {
        mutableGamePaused.value = false
    }

    override fun toggleSkipped(player: Player) {
        if (skippedPlayers.value.contains(player)) {
            mutableSkippedPlayers.value = mutableSkippedPlayers.value.minus(player)
        } else {
            mutableSkippedPlayers.value = mutableSkippedPlayers.value.plus(player)
        }
    }

    override fun updatePlayerName(player: Player, name: String) {
        player.name = name
    }

    override fun removePlayer(player: Player) {
        mutablePlayers.value = mutablePlayers.value.minus(player)
    }
}