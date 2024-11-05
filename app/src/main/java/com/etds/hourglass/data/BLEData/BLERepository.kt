package com.etds.hourglass.data.BLEData

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothGatt
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import com.etds.hourglass.data.BLEData.local.BLELocalDatasource
import com.etds.hourglass.data.BLEData.remote.BLERemoteDatasource
import com.etds.hourglass.model.Device.BLEDevice
import com.etds.hourglass.model.Device.GameDevice
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.UUID

class BLERepository(
    private val localDatasource: BLELocalDatasource,
    private val remoteDatasource: BLERemoteDatasource
) {

    companion object {
        val serviceUUID = UUID.fromString("d7560343-51d4-4c24-a0fe-118fd9078144")
    }
}