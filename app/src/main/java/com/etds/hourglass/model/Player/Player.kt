package com.etds.hourglass.model.Player

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.ColorUtils
import androidx.core.graphics.toColor
import com.etds.hourglass.model.Device.GameDevice
import kotlinx.coroutines.flow.StateFlow
import kotlin.random.Random

class Player(
    public var name: String = "",
    public var device: GameDevice
) {

    public var totalTurnTime: Long = 0
    public var color: Color = Color(
        red = Random.nextInt(128) + 127,
        blue = Random.nextInt(128) + 127,
        green = Random.nextInt(128) + 127
    )
    public var accentColor: Color = Color(ColorUtils.blendARGB(color.toArgb(), Color.Black.toArgb(), 0.25F))

    public var connected: StateFlow<Boolean> = device.connected
    public var skipped: StateFlow<Boolean> = device.skipped

    fun setDeviceOnSkipCallback(callback: (Player) -> Unit) {
        device.onSkipCallback = { callback(this) }
    }

    fun setDeviceOnActiveTurnCallback(callback: (Player) -> Unit) {
        device.onActiveTurnCallback = { callback(this) }
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
}