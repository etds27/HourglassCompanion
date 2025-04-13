package com.etds.hourglass.data.game

import android.util.Log
import com.etds.hourglass.data.game.local.db.entity.SettingsEntity
import com.etds.hourglass.model.Game.Round
import com.etds.hourglass.model.Player.Player
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

/// Singleton class to hold all of the information/data created from the DeviceViewModel
/// so that it can be accessed by every instance of the game repositories
class GameRepositoryDataStore @Inject constructor() {
    val mutableSkippedPlayers = MutableStateFlow<Set<Player>>(setOf())
    val mutablePlayers = MutableStateFlow(mutableListOf<Player>())
    val mutableNumberOfLocalDevices =  MutableStateFlow(0)

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

    companion object {
        private const val TAG = "GameRepositoryDataStore"
    }
}
