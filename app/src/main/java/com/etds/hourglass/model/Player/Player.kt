package com.etds.hourglass.model.Player

import androidx.compose.ui.graphics.Color
import androidx.core.graphics.ColorUtils
import com.etds.hourglass.model.Device.GameDevice
import com.etds.hourglass.model.config.ColorConfig
import com.etds.hourglass.ui.theme.HourglassColors
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.time.Instant
import kotlin.random.Random

class Player(
    var name: String = "",
    var device: GameDevice
) {

    companion object {
        var availableColors: MutableList<Color> =
            HourglassColors.HourglassPlayerColors.toMutableList()
    }

    var lastTurnStart: Instant = Instant.now()

    var totalTurnTime: Long = 0L

    // Represents the total non timed time the user has spent on their turn
    var openTotalTurnTime: Long = 0L

    var colorConfig: ColorConfig = device.colorConfig.value

    private val devicePrimaryColor: StateFlow<Color> = device.primaryColor
    val primaryColor: Color
        get() = devicePrimaryColor.value

    private val deviceAccentColor: StateFlow<Color> = device.accentColor
    val accentColor: Color
        get() = deviceAccentColor.value

    var connected: StateFlow<Boolean> = device.connected

    private var _turnCounter: MutableStateFlow<Int> = MutableStateFlow(0)
    var turnCounter: StateFlow<Int> = _turnCounter

    fun setDeviceOnSkipCallback(callback: (Player, Boolean) -> Unit) {
        device.onSkipCallback = { newValue: Boolean ->
            callback(this, newValue)
        }
    }

    fun setDeviceOnActiveTurnCallback(callback: (Player, Boolean) -> Unit) {
        device.onActiveTurnCallback = { newValue: Boolean ->
            callback(this, newValue)
        }
    }

    fun setDeviceOnServicesDiscoveredCallback(callback: (Player) -> Unit) {
        device.onServicesDiscoveredCallback = { callback(this) }
    }

    fun setDeviceOnConnectCallback(callback: (Player) -> Unit) {
        connected = device.connected
        {}
    }

    fun setDeviceOnReconnectCallback(callback: (Player) -> Unit) {
        device.onServicesRediscoveredCallback = { callback(this) }
    }

    fun setDeviceOnDisconnectCallback(callback: (Player) -> Unit) {
        device.onDisconnectCallback = { callback(this) }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Player) return false
        return name == other.name  // Only compare based on ID
    }

    override fun hashCode(): Int {
        return name.hashCode()  // Hash code based on ID
    }

    override fun toString(): String {
        return name
    }

    fun incrementTurnCounter() {
        _turnCounter.value += 1
    }
}