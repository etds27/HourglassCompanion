package com.etds.hourglass.model.Player

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.ColorUtils
import com.etds.hourglass.model.Device.GameDevice
import com.etds.hourglass.ui.theme.PlayerColor1
import com.etds.hourglass.ui.theme.PlayerColor10
import com.etds.hourglass.ui.theme.PlayerColor11
import com.etds.hourglass.ui.theme.PlayerColor12
import com.etds.hourglass.ui.theme.PlayerColor13
import com.etds.hourglass.ui.theme.PlayerColor14
import com.etds.hourglass.ui.theme.PlayerColor15
import com.etds.hourglass.ui.theme.PlayerColor16
import com.etds.hourglass.ui.theme.PlayerColor2
import com.etds.hourglass.ui.theme.PlayerColor3
import com.etds.hourglass.ui.theme.PlayerColor4
import com.etds.hourglass.ui.theme.PlayerColor5
import com.etds.hourglass.ui.theme.PlayerColor6
import com.etds.hourglass.ui.theme.PlayerColor7
import com.etds.hourglass.ui.theme.PlayerColor8
import com.etds.hourglass.ui.theme.PlayerColor9
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.time.Instant
import kotlin.random.Random

class Player(
    var name: String = "",
    var device: GameDevice
) {

    companion object {
        var availableColors: MutableList<Color> = mutableListOf(
            PlayerColor1,
            PlayerColor2,
            PlayerColor3,
            PlayerColor4,
            PlayerColor5,
            PlayerColor6,
            PlayerColor7,
            PlayerColor8,
            PlayerColor9,
            PlayerColor10,
            PlayerColor11,
            PlayerColor12,
            PlayerColor13,
            PlayerColor14,
            PlayerColor15,
            PlayerColor16,
        )
    }

    var lastTurnStart: Instant = Instant.now()

    var totalTurnTime: Long = 0
    var color: Color = availableColors.removeAt(Random.nextInt(availableColors.size))
    var accentColor: Color =
        Color(ColorUtils.blendARGB(color.toArgb(), Color.Black.toArgb(), 0.25F))

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