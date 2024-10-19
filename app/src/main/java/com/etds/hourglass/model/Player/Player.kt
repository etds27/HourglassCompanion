package com.etds.hourglass.model.Player

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import com.etds.hourglass.model.Device.GameDevice

data class Player(
    public var name: String = "",
) {
    public var connection: BluetoothGatt? = null
    public var device: BluetoothDevice? = null
    public var totalTurnTime: Double = 0.0
}