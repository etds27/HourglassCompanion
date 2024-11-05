package com.etds.hourglass.model.Device

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.util.Log
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class BLEDevice(
    override val name: String = "",
    override val address: String = "",
    val bluetoothDevice: BluetoothDevice? = null,
    val context: Context
) : GameDevice {

    private var _connecting = MutableStateFlow(false)
    private var _connected = MutableStateFlow(false)
    override var connecting: StateFlow<Boolean> = _connecting
    override var connected: StateFlow<Boolean> = _connected

    private var _connection: BluetoothGatt? = null

    fun setConnection(connection: BluetoothGatt) {

    }

    private val gattCallback = object : BluetoothGattCallback() {
        @SuppressLint("MissingPermission")
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                // Connected to GATT server, now you can discover services
                _connection?.discoverServices()
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                // Disconnected from GATT server
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d(TAG, "Device Discovered")
                // Services discovered, you can now read/write characteristics
            } else {
                Log.d(TAG, "Failed to discover servicesd")
            }
        }

        override fun onCharacteristicRead(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d(TAG, "Read Characteristic")
            }
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic
        ) {
            Log.d(TAG, "Characteristic changed")

        }

        override fun onCharacteristicWrite(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {
            Log.d(TAG, "Characteristic written")
        }
    }

    @SuppressLint("MissingPermission")
    override suspend fun connectToDevice(): Boolean {
        _connecting.value = true
        _connection = bluetoothDevice?.connectGatt(context, true, gattCallback)
        _connecting.value = false
        _connected.value = true
        return true
    }

    override suspend fun disconnectFromDevice(): Boolean {
        _connected.value = false
        return true
    }

    companion object {
        const val TAG = "BLEDevice"
    }
}
