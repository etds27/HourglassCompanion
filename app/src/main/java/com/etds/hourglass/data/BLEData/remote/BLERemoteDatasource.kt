package com.etds.hourglass.data.BLEData.remote

import android.Manifest
import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.pm.PackageManager
import androidx.compose.runtime.mutableStateListOf
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.etds.hourglass.model.Device.BLEDevice
import com.etds.hourglass.model.Device.GameDevice
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.UUID

class BLERemoteDatasource(
    private val context: Context
) {
    // private val bluetoothManager: BluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    // private val bluetoothAdapter: BluetoothAdapter = bluetoothManager.adapter

    private val _devices = MutableStateFlow<List<BluetoothDevice>>(mutableStateListOf())
    val devices: StateFlow<List<BluetoothDevice>> = _devices




    private val leScanCallback: ScanCallback = object: ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult?) {

            super.onScanResult(callbackType, result)
            //result?.device.uuids
        }
    }



    private val connectedDevices: MutableList<GameDevice> = mutableListOf()
    private val discoveredDevices: MutableList<BLEDevice> = mutableListOf(
        BLEDevice(address = "13:15:52:62", name = "FISCHER1"),
        BLEDevice(address = "14:15:52:62", name = "FISCHER2"),
        BLEDevice(address = "15:15:52:62", name = "FISCHER3"),
        BLEDevice(address = "16:15:52:62", name = "FISCHER4"),
        BLEDevice(address = "17:15:52:62", name = "FISCHER5")
    )
    suspend fun fetchGameDevices(): List<BLEDevice> {
        return discoveredDevices
    }

    suspend fun connectToDevice(gameDevice: GameDevice) {
        if (connectedDevices.contains(gameDevice)) { return }
        connectedDevices.add(gameDevice)
        discoveredDevices.remove(gameDevice)
    }

    suspend fun fetchConnectedDevices(): List<GameDevice> {
        return connectedDevices
    }

    companion object {
        val serviceUUID = UUID.fromString("d7560343-51d4-4c24-a0fe-118fd9078144")
    }
}