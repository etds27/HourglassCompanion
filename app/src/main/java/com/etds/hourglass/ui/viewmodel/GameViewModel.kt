package com.etds.hourglass.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.etds.hourglass.data.BLEData.BLERepository
import com.etds.hourglass.data.game.GameRepository
import com.etds.hourglass.model.Device.GameDevice
import com.etds.hourglass.model.Player.Player
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class GameViewModel(
    private val gameRepository: GameRepository,
    private val bleDeviceRepository: BLERepository
): ViewModel() {

    private val _timerDuration = MutableStateFlow<Int>(60)
    val timerDuration: StateFlow<Int> = _timerDuration

    private val _enforceTimer = MutableStateFlow<Boolean>(false)
    val enforceTimer: StateFlow<Boolean> = _enforceTimer

    private val _activePlayer = MutableStateFlow<Player?>(null)
    val activePlayer: StateFlow<Player?> = _activePlayer

    private val _skippedPlayers = MutableStateFlow<List<Player>>(listOf())
    val skippedPlayers: StateFlow<List<Player>> = _skippedPlayers

    private val _players = MutableStateFlow<List<Player>>(listOf())
    val players: StateFlow<List<Player>> = _players


}