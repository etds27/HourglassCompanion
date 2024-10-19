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

    suspend fun setSkippedPlayer(player: Player) {
        if (skippedPlayers.contains(player)) {
            Log.d(TAG, "Player is already skipped")
        }
        skippedPlayers.add(player)
    }

    suspend fun fetchSkippedPlayer(): MutableSet<Player> {
        return skippedPlayers
    }

    suspend fun fetchPlayers(): List<Player> {
        return players
    }

    suspend fun fetchCurrentPlayer(): Player? {
        return currentPlayer
    }

    suspend fun fetchTimerDuration(): Int {
        return timerDuration
    }

    suspend fun fetchEnforceTurnTimer(): Boolean {
        return enforceTurnTimer
    }
}