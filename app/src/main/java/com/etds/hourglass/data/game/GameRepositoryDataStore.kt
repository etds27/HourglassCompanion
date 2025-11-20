package com.etds.hourglass.data.game

import android.util.Log
import androidx.compose.ui.graphics.Color
import com.etds.hourglass.data.game.local.LocalGameDatasource
import com.etds.hourglass.model.Player.Player
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

/// Singleton class to hold all of the information/data created from the DeviceViewModel
/// so that it can be accessed by every instance of the game repositories
class GameRepositoryDataStore @Inject constructor() {
    val mutableSkippedPlayers = MutableStateFlow<Set<Player>>(setOf())
    val mutablePlayers = MutableStateFlow(mutableListOf<Player>())
    val mutableNumberOfLocalDevices = MutableStateFlow(0)

    init {
        Log.d(TAG, "GameRepositoryDataStore initialized")
    }

    fun setSkippedPlayers(players: Set<Player>) {
        mutableSkippedPlayers.value = players.toMutableSet()
    }

    fun setPlayers(players: List<Player>) {
        mutablePlayers.value = players.toMutableList()
    }

    fun addPlayer(player: Player) {
        mutablePlayers.value = mutablePlayers.value.plus(player).toMutableList()
    }

    fun setNumberOfLocalDevices(numberOfLocalDevices: Int) {
        mutableNumberOfLocalDevices.value = numberOfLocalDevices
    }

    fun movePlayer(from: Int, to: Int) {
        Log.d(TAG, "From: $from, To: $to, Before reorder: ${mutablePlayers.value}")
        mutablePlayers.value = mutablePlayers.value.apply {
            add(to, removeAt(from))
        }
        Log.d(TAG, "From: $from, To: $to, After reorder: ${mutablePlayers.value}")
    }

    companion object {
        private const val TAG = "GameRepositoryDataStore"
    }
}
