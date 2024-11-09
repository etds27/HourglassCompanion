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

    override fun writeActiveTurn(active: Boolean) {
        Log.d(TAG, "writeActiveTurn: $name: $active")
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

    override fun writeSkipped(skipped: Boolean) {
        Log.d(TAG, "writeSkipped: $name: $skipped")
    }

    override fun writeGameActive(active: Boolean) {
        Log.d(TAG, "writeGameActive: $name: $active")
    }

    override fun writeGamePaused(paused: Boolean) {
        Log.d(TAG, "writeGamePaused: $name: $paused")
    }

    override fun writeTurnTimerEnforced(enforced: Boolean) {
        Log.d(TAG, "writeTurnTimerEnforced: $name: $enforced")
    }

    override fun readActiveTurn() {
        Log.d(TAG, "readActiveTurn: $name: ${activeTurn.value}")
    }

    override fun readSkipped() {
        Log.d(TAG, "readSkipped: $name: ${skipped.value}")
    }

    companion object {
        const val TAG = "LocalDevice"
    }

}