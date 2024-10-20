package com.etds.hourglass.data.game.local

import android.util.Log
import com.etds.hourglass.model.Player.Player

class LocalGameDatasource {
    private val TAG: String = "LocalGameDatasource"
    private val players: List<Player> = listOf()
    private val skippedPlayers: MutableSet<Player> = mutableSetOf()
    private val currentPlayer: Player? = null
    private val timerDuration: Int = 60
    private val enforceTurnTimer: Boolean = false

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
        return listOf(
            Player(name = "Ethan"),
            Player(name = "Haley"),
            Player(name = "Max")
        )
    //return players
    }

    fun fetchCurrentPlayer(): Player? {
        return currentPlayer
    }

    fun fetchTimerDuration(): Int {
        return timerDuration
    }

    fun fetchEnforceTurnTimer(): Boolean {
        return enforceTurnTimer
    }
}