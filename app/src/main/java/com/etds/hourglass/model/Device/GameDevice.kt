package com.etds.hourglass.model.Device

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

    var onSkipCallback: (() -> Unit)? = null
    var onActiveTurnCallback: (() -> Unit)? = null
    var onDisconnectCallback: (() -> Unit)? = null
    var onServicesDiscoveredCallback: (() -> Unit)? = null
    var onConnectionCallback: (() -> Unit)? = null
    var onServicesRediscoveredCallback: (() -> Unit)? = null


    abstract suspend fun connectToDevice(): Boolean
    abstract suspend fun disconnectFromDevice(): Boolean

    protected var _activeTurn: MutableStateFlow<Boolean> = MutableStateFlow(false)
    var activeTurn: StateFlow<Boolean> = _activeTurn

    protected var _skipped: MutableStateFlow<Boolean> = MutableStateFlow(false)
    var skipped: StateFlow<Boolean> = _skipped

    abstract fun writeNumberOfPlayers(number: Int)
    abstract fun writePlayerIndex(index: Int)
    abstract fun writeActiveTurn(active: Boolean)
    abstract fun writeTimer(duration: Long)
    abstract fun writeElapsedTime(duration: Long)
    abstract fun writeCurrentPlayer(index: Int)
    abstract fun writeSkipped(skipped: Boolean)
    abstract fun writeGameActive(active: Boolean)
    abstract fun writeGamePaused(paused: Boolean)
    abstract fun writeTurnTimerEnforced(enforced: Boolean)

    abstract fun readActiveTurn()
    abstract fun readSkipped()

}