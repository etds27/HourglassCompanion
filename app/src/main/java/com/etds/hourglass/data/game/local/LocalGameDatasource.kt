package com.etds.hourglass.data.game.local

import android.util.Log
import com.etds.hourglass.model.Device.GameDevice
import com.etds.hourglass.model.Player.Player
import javax.inject.Inject

class LocalGameDatasource @Inject constructor() {
    companion object {
        private const val TAG: String = "LocalGameDatasource"
    }

    private var localDevicesCount = 0

    private var players: MutableList<Player> = mutableListOf()
    private val skippedPlayers: MutableSet<Player> = mutableSetOf()
    private var turnTime: Long = 600000
    private var totalTurnTime: Long = 9000000
    private val connectedDevices: MutableList<GameDevice> = mutableListOf()

    fun addLocalDevice() {
        localDevicesCount += 1
    }

    fun removeLocalDevice() {
        localDevicesCount -= 1
    }

    fun fetchNumberOfLocalDevices(): Int {
        return localDevicesCount
    }

    fun fetchConnectedDevices(): List<GameDevice> {
        return connectedDevices
    }

    fun addPlayer(player: Player) {
        players.add(player)
    }

    fun addConnectedDevice(gameDevice: GameDevice) {
        if (connectedDevices.contains(gameDevice)) {
            return
        }
        connectedDevices.add(gameDevice)
    }

    fun removeConnectedDevice(gameDevice: GameDevice) {
        if (!connectedDevices.contains(gameDevice)) {
            return
        }
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

    fun fetchTurnTime(): Long {
        return turnTime
    }

    fun setTurnTime(duration: Long) {
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

    fun movePlayer(from: Int, to: Int) {
        Log.d(TAG, "From: $from, To: $to, Before reorder: $players")
        players = players.toMutableList().apply {
            add(to, removeAt(from))
        }
        Log.d(TAG, "From: $from, To: $to, After reorder: $players")
    }

    fun shiftPlayerOrderForward() {
        Log.d(TAG, "Shift Forward. Before shift: $players")
        players = players.toMutableList().apply {
            add(0, removeAt(size - 1))
        }
        Log.d(TAG, "Shift Forward. After shift: $players")
    }

    fun shiftPlayerOrderBackward() {
        Log.d(TAG, "Shift Backward. Before shift: $players")
        players = players.toMutableList().apply {
            add(size - 1, removeAt(0))
        }
        Log.d(TAG, "Shift Backward. After shift: $players")
    }

    fun resetDatasource() {
        players.clear()
        skippedPlayers.clear()
        turnTime = 600000
        totalTurnTime = 9000000
        connectedDevices.clear()
    }
}