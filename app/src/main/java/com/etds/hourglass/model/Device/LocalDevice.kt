package com.etds.hourglass.model.Device

import android.util.Log

class LocalDevice(
    name: String = "",
    address: String = "",
) : GameDevice(
    name = name,
    address = address
) {
    init {
        _connected.value = true
    }

    override suspend fun connectToDevice(): Boolean {
        _connected.value = true
        return true
    }

    override suspend fun disconnectFromDevice(): Boolean {
        _connected.value = false
        return true
    }

    override fun writeNumberOfPlayers(number: Int) {
        Log.d(TAG, "writeNumberOfPlayers: $name: $number")
    }

    override fun writePlayerIndex(index: Int) {
        Log.d(TAG, "writePlayerIndex: $name: $index")
    }

    override fun writeTimer(duration: Long) {
        Log.d(TAG, "writeActiveTurn: $name: $duration")
    }

    override fun writeElapsedTime(duration: Long) {
        // Log.d(TAG, "writeElapsedTime: $name: $duration")
    }

    override fun writeCurrentPlayer(index: Int) {
        Log.d(TAG, "writeCurrentPlayer: $name: $index")
    }

    override fun writeSkipped() {
        Log.d(TAG, "writeSkipped: $name: skipped")
    }

    override fun writeGamePaused(paused: Boolean) {
        Log.d(TAG, "writeGamePaused: $name: $paused")
    }

    override fun writeTurnTimerEnforced(enforced: Boolean) {
        Log.d(TAG, "writeTurnTimerEnforced: $name: $enforced")
    }

    override fun writeUnskipped() {
        Log.d(TAG, "writeUnskipped: $name")
    }

    override fun writeAwaitingGameStart() {
        Log.d(TAG, "writeAwaitingGameStart: $name")
    }

    companion object {
        const val TAG = "LocalDevice"
    }

}