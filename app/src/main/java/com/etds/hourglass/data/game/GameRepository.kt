package com.etds.hourglass.data.game

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothGatt
import android.bluetooth.le.BluetoothLeScanner
import com.etds.hourglass.data.BLEData.remote.BLERemoteDatasource
import com.etds.hourglass.data.game.local.LocalGameDatasource
import com.etds.hourglass.model.Device.BLEDevice
import com.etds.hourglass.model.Device.GameDevice
import com.etds.hourglass.model.Player.Player
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.UUID

class GameRepository(
    private val localGameDatasource: LocalGameDatasource,
    private val bluetoothDatasource: BLERemoteDatasource
) {
    suspend fun fetchGameDevices(): List<GameDevice> {
        return bluetoothDatasource.fetchGameDevices()
    }

    suspend fun setSkippedPlayer(player: Player) {
        localGameDatasource.setSkippedPlayer(player)
    }

    companion object {
        val serviceUUID = UUID.fromString("d7560343-51d4-4c24-a0fe-118fd9078144")
    }

    private val bluetoothAdapter: BluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    private val bluetoothLeScanner: BluetoothLeScanner = bluetoothAdapter.bluetoothLeScanner
    private val connections: MutableList<BluetoothGatt> = mutableListOf()

    private val _discoveredDevices = MutableStateFlow<List<GameDevice>>(listOf())
    private val discoveredDevices: StateFlow<List<GameDevice>> = _discoveredDevices

    /*
    suspend fun discoverGameDevices(): List<BLEDevice> {
    bluetoothLeScanner.startScan(scanCallback)
    return remoteDatasource.getGameDevices()
    }

    private val scanCallback = object : ScanCallback() {
    override fun onScanResult(callbackType: Int, result: ScanResult?) {
        result?.scanRecord.serviceUuids. .serviceUuids?.contains(serviceUUID)
        super.onScanResult(callbackType, result)
    }
    */
}