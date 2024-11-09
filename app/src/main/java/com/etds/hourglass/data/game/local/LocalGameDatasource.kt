package com.etds.hourglass.data.game.local

import android.util.Log
import com.etds.hourglass.model.Device.GameDevice
import com.etds.hourglass.model.Device.LocalDevice
import com.etds.hourglass.model.Player.Player

class LocalGameDatasource {
    private val TAG: String = "LocalGameDatasource"
    private val players: MutableList<Player> = mutableListOf(
        Player(name = "Ethan", LocalDevice(name = "Ethan")),
        Player(name = "Haley", LocalDevice(name = "Haley")),
        Player(name = "Max", LocalDevice(name = "Max"))
    )
    private val skippedPlayers: MutableSet<Player> = mutableSetOf()
    private val currentPlayer: Player? = null
    private var turnTime: Long = 600000
    private var totalTurnTime: Long = 9000000
    private val enforceTurnTimer: Boolean = false
    private val connectedDevices: MutableList<GameDevice> = mutableListOf()
    private val localDevice: LocalDevice = LocalDevice(
        name = "My Device",
        address = ""
    )

    fun fetchLocalDevice(): GameDevice {
        return localDevice
    }

    fun fetchConnectedDevices(): List<GameDevice> {
        return connectedDevices
    }

    fun addConnectedDevice(gameDevice: GameDevice) {
        if (connectedDevices.contains(gameDevice)) { return }
        connectedDevices.add(gameDevice)
    }

    fun removeConnectedDevice(gameDevice: GameDevice) {
        if (!connectedDevices.contains(gameDevice)) { return }
        connectedDevices.remove(gameDevice)
    }

    fun setSkippedPlayer(player: Player) {
        if (skippedPlayers.contains(player)) {
            Log.d(TAG, "Player is already skipped")
        }
        skippedPlayers.add(player)
    }

    fun setUnskippedPlayer(player: Player) {
        if (!skippedPlayers.contains(player)) {
            Log.d(TAG, "Player is not skipped already")
        }
        skippedPlayers.remove(player)
    }

    fun fetchSkippedPlayers(): MutableSet<Player> {
        return skippedPlayers
    }

    fun fetchPlayers(): List<Player> {
        return players
    }

    fun fetchCurrentPlayer(): Player? {
        return currentPlayer
    }


    fun fetchEnforceTurnTimer(): Boolean {
        return enforceTurnTimer
    }

    fun fetchTurnTime(): Long {
        return turnTime
    }

    fun setTurnTime(duration: Long)  {
        turnTime = duration
    }

    fun fetchTotalTurnTime(): Long {
        return totalTurnTime
    }

    fun setTotalTurnTime(duration: Long) {
        totalTurnTime = duration
    }

    fun removePlayer(player: Player) {
        players.remove(player)
    }
}