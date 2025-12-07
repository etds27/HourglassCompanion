package com.etds.hourglass.model.Device

import android.util.Log
import com.etds.hourglass.model.config.ColorConfig
import androidx.compose.ui.graphics.Color
import com.etds.hourglass.model.DeviceState.DeviceState
import com.etds.hourglass.ui.viewmodel.BaseDevicePersonalizationViewModel.Companion.TAG
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.compose
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.withTimeoutOrNull


enum class DeviceConnectionState {
    Connected,
    Connecting,
    Disconnected
}

data class DevicePersonalizationConfig (
    val name: String,
    val colorConfig: ColorConfig,
    val deviceState: DeviceState,
    val ledOffset: Int,
    val ledCount: Int
)

abstract class GameDevice(
    protected val initialName: String,
    val address: String
) {

    // Set to indicate that the device is in the process of pairing
    protected var mutableConnectionState: MutableStateFlow<DeviceConnectionState> =
        MutableStateFlow(DeviceConnectionState.Disconnected)
    var connectionState: StateFlow<DeviceConnectionState> = mutableConnectionState

    protected var mutableName: MutableStateFlow<String> = MutableStateFlow(initialName)
    var name: StateFlow<String> = mutableName


    protected var mutableColorConfig: MutableStateFlow<ColorConfig> = MutableStateFlow(ColorConfig())
    var colorConfig: StateFlow<ColorConfig> = mutableColorConfig

    protected val mutableColorConfigChannel = Channel<ColorConfig>(Channel.RENDEZVOUS)
    val colorConfigChannel = mutableColorConfigChannel.receiveAsFlow()

    protected var mutableColorConfigState: MutableStateFlow<DeviceState> = MutableStateFlow(DeviceState.Off)
    var colorConfigState: StateFlow<DeviceState> = mutableColorConfigState

    protected var mutablePrimaryColor: MutableStateFlow<Color> = MutableStateFlow(Color.Blue)
    var primaryColor: StateFlow<Color> = mutablePrimaryColor

    protected var mutableAccentColor: MutableStateFlow<Color> = MutableStateFlow(Color.Red)
    var accentColor: StateFlow<Color> = mutableAccentColor

    protected var mutableLEDOffset: MutableStateFlow<Int> = MutableStateFlow(0)
    var ledOffset: StateFlow<Int> = mutableLEDOffset

    protected var mutableLEDOffetChannel: Channel<Int> = Channel(Channel.RENDEZVOUS)
    var ledOffsetChannel = mutableLEDOffetChannel.receiveAsFlow()


    protected var mutableLEDCount: MutableStateFlow<Int> = MutableStateFlow(0)
    var ledCount: StateFlow<Int> = mutableLEDCount

    protected var mutableLEDCountChannel: Channel<Int> = Channel(Channel.RENDEZVOUS)
    var ledCountChannel = mutableLEDCountChannel.receiveAsFlow()


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

    open fun writeDeviceName(name: String) {
        mutableName.value = name
    }

    abstract fun writeDeviceNameWrite(boolean: Boolean)

    open fun writeDeviceColorConfig(color: ColorConfig) {
        mutableColorConfig.value = color
    }

    open fun writeColorConfigState(state: DeviceState) {
        mutableColorConfigState.value = state
    }

    abstract fun writeColorConfigWrite(boolean: Boolean)

    abstract fun writeLEDOffset(offset: Int)
    abstract fun writeLEDOffsetWrite(boolean: Boolean)

    abstract fun writeLEDCount(count: Int)
    abstract fun writeLEDCountWrite(boolean: Boolean)



    abstract fun readDeviceName()

    // Make this suspending so we can simulate a delayed update with the mock interface
    abstract suspend fun readDeviceColorConfig()

    abstract fun fetchDeviceName(): String
    abstract fun fetchDeviceColorConfig(): ColorConfig

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
        Log.d(TAG, "Setting device state to $deviceState")
        this._deviceState = deviceState
    }

    fun setPrimaryColor(color: Color) {
        this.mutablePrimaryColor.value = color
    }

    fun setAccentColor(color: Color) {
        this.mutableAccentColor.value = color
    }

    suspend fun performColorConfigRetrieval(deviceState: DeviceState): ColorConfig {
        writeColorConfigState(deviceState)
        readDeviceColorConfig()


        Log.d(TAG, "Waiting for peripheral to write new color config")

        val result = withTimeoutOrNull(5000) { colorConfigChannel.firstOrNull() }

        if (result == null) {
            Log.d(TAG, "Did not receive color config update from peripheral")
        } else {
            Log.d(TAG, "Received color config update from peripheral: $result")
        }

        if (result != null) {
            mutableColorConfig.value = result
        }

        // Update the devices primary and accent colors when we set the Device Color Mode
        if (deviceState == DeviceState.DeviceColorMode) {
            setPrimaryColor(colorConfig.value.colors[0])
            setAccentColor(colorConfig.value.colors[1])
        }

        val config = fetchDeviceColorConfig()
        Log.d(TAG, "Returning color config from peripheral: $config")
        return config
    }

    companion object {
        const val TAG = "GameDevice"
    }

    abstract fun readLEDCount()
    abstract fun readLEDOffset()
}