package com.etds.hourglass.model.Player

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import com.etds.hourglass.model.Device.GameDevice

data class Player(
    public var name: String = "",
) {
    public var connection: BluetoothGatt? = null
    public var device: BluetoothDevice? = null
    public var totalTurnTime: Long = 0

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