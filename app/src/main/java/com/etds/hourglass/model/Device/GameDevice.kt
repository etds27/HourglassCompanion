package com.etds.hourglass.model.Device

import com.etds.hourglass.model.DeviceState.DeviceState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

abstract class GameDevice(
    val name: String,
    val address: String
) {

    // Set to indicate that the device is in the process of pairing
    protected var _connecting: MutableStateFlow<Boolean> = MutableStateFlow(false)
    var connecting: StateFlow<Boolean> = _connecting
    protected var _connected: MutableStateFlow<Boolean> = MutableStateFlow(false)
    var connected: StateFlow<Boolean> = _connected

    var onSkipCallback: ((Boolean) -> Unit)? = null
    var onActiveTurnCallback: ((Boolean) -> Unit)? = null
    var onDisconnectCallback: (() -> Unit)? = null
    var onServicesDiscoveredCallback: (() -> Unit)? = null
    var onConnectionCallback: (() -> Unit)? = null
    var onServicesRediscoveredCallback: (() -> Unit)? = null


    abstract suspend fun connectToDevice(): Boolean
    abstract suspend fun disconnectFromDevice(): Boolean

    abstract fun writeNumberOfPlayers(number: Int)
    abstract fun writePlayerIndex(index: Int)
    abstract fun writeTimer(duration: Long)
    abstract fun writeElapsedTime(duration: Long)
    abstract fun writeCurrentPlayer(index: Int)
    abstract fun writeSkipped()
    abstract fun writeGamePaused(paused: Boolean)
    abstract fun writeTurnTimerEnforced(enforced: Boolean)

    /// Write the skipped players to the device
    /// The skipped players are encoded in an Int
    /// Each bit of the int represents each player in the turn sequence
    /// A 1 indicates that the player has been skipped, a 0 indicates that the player has not been skipped
    /// The least significant bit is the first player in the turn sequence
    abstract fun writeSkippedPlayers(skippedPlayers: Int)

    abstract fun writeUnskipped()
    abstract fun writeAwaitingGameStart()

    private var _deviceState: DeviceState = DeviceState.Off

    /// Synchronize the Device State known by the app with the current device
    fun syncDeviceState() {
        setDeviceState(_deviceState)
    }

    fun getDeviceState(): DeviceState {
        return _deviceState
    }

    open fun setDeviceState(deviceState: DeviceState) {
        this._deviceState = deviceState
    }
}