package com.etds.hourglass.data.BLEData.remote

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.os.ParcelUuid
import android.util.Log
import com.etds.hourglass.model.Device.BLEDevice
import com.etds.hourglass.model.Device.GameDevice
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.UUID
import javax.inject.Inject

class BLERemoteDatasource @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val bluetoothManager: BluetoothManager =
        context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val bluetoothAdapter: BluetoothAdapter = bluetoothManager.adapter
    private val bluetoothLeScanner: BluetoothLeScanner = bluetoothAdapter.bluetoothLeScanner

    private val leScanCallback: ScanCallback = object : ScanCallback() {
        @SuppressLint("MissingPermission")
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            super.onScanResult(callbackType, result)
            val uuids = result?.scanRecord?.serviceUuids?.map { it.uuid }
                ?: arrayOf<ParcelUuid>().map { it.uuid }
            val name = result?.device?.name ?: ""
            if (uuids.contains(serviceUUID)) {
                Log.d(TAG, "Found device: $name")
                result?.device?.let {
                    if (!discoveredDevices.map { gameDevice ->
                            gameDevice.bluetoothDevice
                        }.contains(result.device)) {
                        discoveredDevices.add(
                            BLEDevice(
                                name = it.name.trim(),
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

    @SuppressLint("MissingPermission")
    fun reconnectionCallback(
        mac: String,
        deviceFoundCallback: (BLEDevice) -> Unit,
        context: Context
    ): ScanCallback {
        val callback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult?) {
                super.onScanResult(callbackType, result)
                result?.device?.let {

                    val foundMac = it.address
                    val name = it.name
                    if (mac == foundMac) {
                        Log.d(TAG, "Found device: $name")

                        deviceFoundCallback(
                            BLEDevice(
                                name = it.name.trim(),
                                address = it.address,
                                bluetoothDevice = it,
                                context = context
                            )
                        )
                        stopReconnectSearch(this)
                    }
                }
            }
        }
        return callback
    }

    private val connectedDevices: MutableList<GameDevice> = mutableListOf()
    private val discoveredDevices: MutableList<BLEDevice> = mutableListOf()

    @SuppressLint("MissingPermission")
    fun startDeviceSearch() {
        bluetoothLeScanner.startScan(leScanCallback)
    }

    @SuppressLint("MissingPermission")
    fun reconnectDevice(mac: String, deviceFoundCallback: (BLEDevice) -> Unit): ScanCallback {
        val callback = reconnectionCallback(
            mac = mac,
            deviceFoundCallback = deviceFoundCallback,
            context = context
        )
        bluetoothLeScanner.startScan(
            callback
        )
        return callback
    }

    fun stopDeviceSearch() {
        stopReconnectSearch(leScanCallback)
    }

    @SuppressLint("MissingPermission")
    fun stopReconnectSearch(scanCallback: ScanCallback) {
        bluetoothLeScanner.stopScan(scanCallback)
    }

    fun fetchGameDevices(): List<BLEDevice> {
        return discoveredDevices
    }

    fun fetchConnectedDevices(): List<GameDevice> {
        return connectedDevices
    }

    fun resetDatasource() {
        stopDeviceSearch()
        discoveredDevices.clear()
        connectedDevices.clear()
    }

    companion object {
        const val TAG = "BLERemoteDatasource"
        val serviceUUID: UUID = UUID.fromString("d7560343-51d4-4c24-a0fe-118fd9078144")
    }
}
