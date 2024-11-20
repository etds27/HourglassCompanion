package com.etds.hourglass.data.BLEData.remote

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.os.ParcelUuid
import androidx.compose.runtime.mutableStateListOf
import com.etds.hourglass.model.Device.BLEDevice
import com.etds.hourglass.model.Device.GameDevice
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.UUID
import javax.inject.Inject

class BLERemoteDatasource @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val bluetoothManager: BluetoothManager =
        context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val bluetoothAdapter: BluetoothAdapter = bluetoothManager.adapter
    private val bluetoothLeScanner: BluetoothLeScanner = bluetoothAdapter.bluetoothLeScanner

    private val _devices = MutableStateFlow<MutableList<BluetoothDevice>>(mutableStateListOf())
    val devices: StateFlow<List<BluetoothDevice>> = _devices


    private val leScanCallback: ScanCallback = object : ScanCallback() {
        @SuppressLint("MissingPermission")
        override fun onScanResult(callbackType: Int, result: ScanResult?) {

            super.onScanResult(callbackType, result)
            val uuids = (result?.device?.uuids ?: arrayOf<ParcelUuid>()).map { it.uuid }
            val name = result?.device?.name ?: ""
            if (name.contains("FISCHER")) {
                result?.device?.let {
                    if (!discoveredDevices.map { gameDevice ->
                        gameDevice.bluetoothDevice
                    }.contains(result.device)) {
                        discoveredDevices.add(
                            BLEDevice(
                                name = it.name,
                                address = it.address,
                                bluetoothDevice = it,
                                context = context
                            )
                        )
                    }
                }
            }
        }
    }


    private val connectedDevices: MutableList<GameDevice> = mutableListOf()
    private val discoveredDevices: MutableList<BLEDevice> = mutableListOf()

    @SuppressLint("MissingPermission")
    fun startDeviceSearch() {
        bluetoothLeScanner.startScan(leScanCallback)
    }

    @SuppressLint("MissingPermission")
    fun stopDeviceSearch() {
        bluetoothLeScanner.stopScan(leScanCallback)
    }

    suspend fun fetchGameDevices(): List<BLEDevice> {
        return discoveredDevices
    }

    suspend fun fetchConnectedDevices(): List<GameDevice> {
        return connectedDevices
    }

    fun resetDatasource() {
        stopDeviceSearch()
        discoveredDevices.clear()
        connectedDevices.clear()
    }
}