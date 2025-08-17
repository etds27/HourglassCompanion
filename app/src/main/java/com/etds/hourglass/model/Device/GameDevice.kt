package com.etds.hourglass.model.Device

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.ColorUtils
import com.etds.hourglass.model.DeviceState.DeviceState
import com.etds.hourglass.model.Player.Player.Companion.availableColors
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.random.Random


abstract class GameDevice(
    protected val initialName: String,
    val address: String
) {

    // Set to indicate that the device is in the process of pairing
    protected var _connecting: MutableStateFlow<Boolean> = MutableStateFlow(false)
    var connecting: StateFlow<Boolean> = _connecting
    protected var _connected: MutableStateFlow<Boolean> = MutableStateFlow(false)
    var connected: StateFlow<Boolean> = _connected

    protected var mutableName: MutableStateFlow<String> = MutableStateFlow(initialName)
    var name: StateFlow<String> = mutableName

    protected var mutableColor: MutableStateFlow<Color> = MutableStateFlow(availableColors.removeAt(Random.nextInt(availableColors.size)))
    var color: StateFlow<Color> = mutableColor

    protected var mutableAccentColor: MutableStateFlow<Color> = MutableStateFlow(
        Color(ColorUtils.blendARGB(color.value.toArgb(), Color.Black.toArgb(), 0.25F))
    )
    var accentColor: StateFlow<Color> = mutableAccentColor

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

    open fun writeDeviceName(name: String) {
        mutableName.value = name
    }
    open fun writeDeviceColor(color: Color) {
        mutableColor.value = color
    }
    open fun writeDeviceAccentColor(color: Color) {
        mutableAccentColor.value = color
    }

    abstract fun readDeviceName(): String
    abstract fun readDeviceColor(): Color
    abstract fun readDeviceAccentColor(): Color

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