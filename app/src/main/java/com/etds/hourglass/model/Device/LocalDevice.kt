package com.etds.hourglass.model.Device

import com.etds.hourglass.model.config.ColorConfig
import android.util.Log
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlin.random.Random

class LocalDevice(
    name: String = "",
    address: String = "",
) : GameDevice(
    initialName = name,
    address = address
) {
    init {
        mutableConnectionState.value = DeviceConnectionState.Connected
    }

    override suspend fun connectToDevice(): Boolean {
        mutableConnectionState.value = DeviceConnectionState.Connected
        return true
    }

    override suspend fun disconnectFromDevice(): Boolean {
        mutableConnectionState.value = DeviceConnectionState.Disconnected
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

    override fun writeDeviceNameWrite(boolean: Boolean) {
        Log.d(TAG, "writeDeviceNameWrite: $name: $boolean")
    }

    override fun writeColorConfigWrite(boolean: Boolean) {
        Log.d(TAG, "writeColorConfigWrite: $name: $boolean")
    }

    override fun writeLEDOffset(offset: Int) {
        Log.d(TAG, "writeLEDOffset: $name: $offset")
    }
    override fun writeLEDOffsetWrite(boolean: Boolean) {
        Log.d(TAG, "writeLEDOffsetWrite: $name: $boolean")
    }

    override fun writeLEDCount(count: Int) {
        Log.d(TAG, "writeLEDCount: $name: $count")
    }
    override fun writeLEDCountWrite(boolean: Boolean) {
        Log.d(TAG, "writeLEDCountWrite: $name: $boolean")
    }

    override fun fetchDeviceName(): String {
        return name.value
    }

    override fun fetchDeviceColorConfig(): ColorConfig {
        return colorConfig.value
    }

    override fun readDeviceName() {}

    override suspend fun readDeviceColorConfig() {
        coroutineScope {
            delay(500)
            mutableColorConfig.value = ColorConfig(
                colors = MutableList(4) {
                    Color(
                        red = Random.nextFloat(),
                        green = Random.nextFloat(),
                        blue = Random.nextFloat(),
                        alpha = 1f
                    )
                }
            )

            Log.d(TAG, "readDeviceColorConfig: $name: ${mutableColorConfig.value}")
            mutableColorConfigChannel.send(mutableColorConfig.value)
        }
    }

    override fun writeSkippedPlayers(skippedPlayers: Int) {
        Log.d(TAG, "writeSkippedPlayers: $name: $skippedPlayers")
    }

    override fun writeUnskipped() {
        Log.d(TAG, "writeUnskipped: $name")
    }

    override fun writeAwaitingGameStart() {
        Log.d(TAG, "writeAwaitingGameStart: $name")
    }

    override fun readLEDCount() {

    }

    override fun readLEDOffset() {

    }

    companion object {
        const val TAG = "LocalDevice"
    }

}