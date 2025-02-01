package com.etds.hourglass.model.Device

import android.annotation.SuppressLint
import android.app.GameState
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.util.Log
import com.etds.hourglass.model.Device.BLEDevice.Companion.TAG
import com.etds.hourglass.model.Device.BLEDevice.Companion.clientCharacteristicConfigUUID
import com.etds.hourglass.model.DeviceState.DeviceState
import java.nio.ByteBuffer
import java.util.LinkedList
import java.util.UUID

private interface BLEOperation {
    val characteristic: BluetoothGattCharacteristic
    fun perform(connection: BluetoothGatt)
}

private data class DataOperation(
    override val characteristic: BluetoothGattCharacteristic,
    val byteArray: ByteArray,
) : BLEOperation {

    @SuppressLint("MissingPermission")
    override fun perform(connection: BluetoothGatt) {
        Log.d("BLEOperation", "Performing data operation: ${characteristic.uuid}: $byteArray")
        connection.writeCharacteristic(
            characteristic,
            byteArray,
            BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
        )
    }

    override fun toString(): String {
        return "${characteristic.uuid}: $byteArray"
    }
}

private data class EnableNotificationOperation(
    override val characteristic: BluetoothGattCharacteristic
) : BLEOperation {
    @SuppressLint("MissingPermission")
    override fun perform(connection: BluetoothGatt) {
        val descriptor = characteristic.getDescriptor(clientCharacteristicConfigUUID)
        connection.setCharacteristicNotification(
            characteristic,
            true
        )
        descriptor?.let {
            connection.writeDescriptor(
                descriptor,
                BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
            )
            Log.d(TAG, "Enabled notifications for ${characteristic.uuid}")
        } ?: {
            Log.d(TAG, "Unable to get descriptor")
        }
    }

    override fun toString(): String {
        return "Enabling BLE Notify for ${characteristic.uuid}"
    }
}

class BLEDevice(
    name: String = "",
    address: String = "",
    val bluetoothDevice: BluetoothDevice? = null,
    val context: Context
) : GameDevice(
    name = name,
    address = address
) {
    private var _connection: BluetoothGatt? = null

    private var _isWriting: Boolean = false
    private var operationQueue: LinkedList<BLEOperation> = LinkedList()

    private var numberOfPlayersCharacteristic: BluetoothGattCharacteristic? = null
    private var playerIndexCharacteristic: BluetoothGattCharacteristic? = null
    private var timerCharacteristic: BluetoothGattCharacteristic? = null
    private var elapsedTimeCharacteristic: BluetoothGattCharacteristic? = null
    private var currentPlayerCharacteristic: BluetoothGattCharacteristic? = null
    private var turnTimeEnforcedCharacteristic: BluetoothGattCharacteristic? = null

    private var skipToggleActionCharacteristic: BluetoothGattCharacteristic? = null
    private var endTurnActionCharacteristic: BluetoothGattCharacteristic? = null
    private var skippedPlayersCharacteristic: BluetoothGattCharacteristic? = null
    private var gameStateCharacteristic: BluetoothGattCharacteristic? = null


    private val gattCallback = object : BluetoothGattCallback() {
        @SuppressLint("MissingPermission")
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.d(TAG, "Device connected")
                onConnectionCallback?.invoke()
                // Connected to GATT server, now you can discover services
                _connection?.discoverServices()
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.d(TAG, "Device disconnected")
                disconnect()
                // Disconnected from GATT server
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d(TAG, "Device Discovered")
                val service = _connection?.getService(serviceUUID)
                numberOfPlayersCharacteristic = service?.getCharacteristic(totalPlayersUUID)
                playerIndexCharacteristic = service?.getCharacteristic(myPlayerUUID)
                endTurnActionCharacteristic = service?.getCharacteristic(endTurnActionUUID)
                skipToggleActionCharacteristic = service?.getCharacteristic(skipToggleActionUUID)
                skippedPlayersCharacteristic = service?.getCharacteristic(skippedPlayersUUID)
                timerCharacteristic = service?.getCharacteristic(timerUUID)
                elapsedTimeCharacteristic = service?.getCharacteristic(elapsedTimeUUID)
                currentPlayerCharacteristic = service?.getCharacteristic(currentPlayerUUID)
                turnTimeEnforcedCharacteristic = service?.getCharacteristic(turnTimerEnforcedUUID)
                gameStateCharacteristic = service?.getCharacteristic(deviceStateUUID)

                // Defaults
                writeNumberOfPlayers(1)
                writeCurrentPlayer(0)
                writeTimer(60000)
                writeElapsedTime(0)
                writeTurnTimerEnforced(false)
                writeSkippedPlayers(0)
                writePlayerIndex(0)
                writeAwaitingGameStart()

                enableNotifications(endTurnActionCharacteristic)
                enableNotifications(skipToggleActionCharacteristic)

                onServicesDiscoveredCallback?.invoke()
                onServicesRediscoveredCallback?.invoke()
            } else {
                Log.d(TAG, "Failed to discover services")
            }
        }

        override fun onCharacteristicRead(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray,
            status: Int
        ) {
            super.onCharacteristicRead(gatt, characteristic, value, status)
            when (characteristic) {
                skipToggleActionCharacteristic -> skippedChange(value)
                endTurnActionCharacteristic -> activeTurnChange(value)
            }
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray
        ) {
            Log.d(TAG, "Characteristic changed: ${characteristic.uuid}: $value")
            when (characteristic) {
                skipToggleActionCharacteristic -> skippedChange(value)
                endTurnActionCharacteristic -> activeTurnChange(value)
            }
        }

        override fun onDescriptorWrite(
            gatt: BluetoothGatt?,
            descriptor: BluetoothGattDescriptor?,
            status: Int
        ) {
            super.onDescriptorWrite(gatt, descriptor, status)
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d(TAG, "Descriptor successfully written")
            } else {
                Log.d(TAG, "Unable to write descriptor")
            }
            _isWriting = false
            processNextOperation()
        }

        override fun onCharacteristicWrite(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d(TAG, "Characteristic successfully written ${characteristic.uuid}")
            } else {
                Log.d(TAG, "Unable to write characteristic ${characteristic.uuid}")
            }

            _isWriting = false
            processNextOperation()
        }
    }

    private fun skippedChange(value: ByteArray) {
        val newValue = byteArrayToBool(value)
        onSkipCallback?.let {
            onSkipCallback!!.invoke(newValue)
        }
    }

    private fun activeTurnChange(value: ByteArray) {
        val newValue = byteArrayToBool(value)
        onActiveTurnCallback?.let {
            onActiveTurnCallback!!.invoke(newValue)
        }
    }

    @SuppressLint("MissingPermission")
    private fun disconnect() {
        _connected.value = false
        onDisconnectCallback?.invoke()
        _connection?.close()
        _connection = null
    }

    @SuppressLint("MissingPermission")
    private fun enableNotifications(characteristic: BluetoothGattCharacteristic?) {
        characteristic?.let {
            val operation = EnableNotificationOperation(characteristic)
            enqueueOperation(operation)
        } ?: {
            Log.d(TAG, "Characteristic not found")
        }
    }

    @SuppressLint("MissingPermission")
    override suspend fun connectToDevice(): Boolean {
        _connecting.value = true
        _connection = bluetoothDevice?.connectGatt(context, false, gattCallback)
        _connecting.value = false
        _connected.value = true
        return true
    }

    @SuppressLint("MissingPermission")
    override suspend fun disconnectFromDevice(): Boolean {
        _connected.value = false
        _connection?.disconnect()
        return true
    }

    private fun intToByteArray(value: Int): ByteArray {
        val data = ByteArray(4)
        for (i in 0..<4) {
            data[i] = (value shr (i * 8)).toByte()
        }
        return data
    }

    private fun byteArrayToInt(byteArray: ByteArray): Int {
        return ByteBuffer.wrap(byteArray).getInt()
    }

    private fun boolToByteArray(value: Boolean): ByteArray {
        return byteArrayOf(if (value) 0x01 else 0x00)
    }

    private fun byteArrayToBool(byteArray: ByteArray): Boolean {
        return byteArray[0] > 0
    }

    private fun processNextOperation() {
        if (_isWriting || operationQueue.isEmpty()) {
            return
        }
        _isWriting = true

        val operation = operationQueue.removeAt(0)

        Log.d(TAG, "Processing operation: $operation")

        _connection?.let {
            operation.perform(it)
        } ?: {
            Log.d(TAG, "Unable to perform operation. Connection is null")
        }

        Log.d(TAG, "Remaining operations to process: ${operationQueue.size}")
        processNextOperation()
    }

    private fun enqueueOperation(operation: BLEOperation) {
        operationQueue.add(operation)
        processNextOperation()
    }

    private fun enqueueOperation(
        characteristic: BluetoothGattCharacteristic,
        byteArray: ByteArray
    ) {
        enqueueOperation(
            DataOperation(
                characteristic = characteristic,
                byteArray = byteArray
            )
        )
    }

    @SuppressLint("MissingPermission")
    private fun readValue(characteristic: BluetoothGattCharacteristic?) {
        characteristic?.let {
            _connection?.readCharacteristic(characteristic)
        } ?: {
            Log.d(TAG, "Characteristic not found")
        }
    }

    private fun writeInt(characteristic: BluetoothGattCharacteristic?, value: Int) {
        val data = intToByteArray(value)
        characteristic?.let {
            enqueueOperation(characteristic, data)
        } ?: {
            Log.d(TAG, "${name}: Unable to write data to characteristic")
        }
    }

    private fun writeInt(characteristic: BluetoothGattCharacteristic?, value: Long) {
        writeInt(characteristic, value.toInt())
    }

    private fun writeBool(characteristic: BluetoothGattCharacteristic?, value: Boolean) {
        val data = boolToByteArray(value)
        characteristic?.let {
            enqueueOperation(characteristic, data)
        } ?: {
            Log.d(TAG, "${name}: Unable to write data to characteristic")
        }
    }

    private fun writeDeviceState(deviceState: DeviceState) {
        writeInt(characteristic = gameStateCharacteristic, deviceState.value)
    }

    override fun writeNumberOfPlayers(number: Int) {
        writeInt(numberOfPlayersCharacteristic, number)
    }

    override fun writePlayerIndex(index: Int) {
        writeInt(playerIndexCharacteristic, index)
    }

    override fun writeTimer(duration: Long) {
        Log.d(LocalDevice.TAG, "writeTimer: $name: $duration")
        writeInt(timerCharacteristic, duration)
    }

    override fun writeElapsedTime(duration: Long) {
        Log.d(LocalDevice.TAG, "writeElapsedTime: $name: $duration")
        writeInt(elapsedTimeCharacteristic, duration)
    }

    override fun writeCurrentPlayer(index: Int) {
        writeInt(currentPlayerCharacteristic, index)
    }

    override fun writeSkipped() {
        writeDeviceState(DeviceState.Skipped)
    }

    override fun writeUnskipped() {
        writeDeviceState(DeviceState.AwaitingTurn)
    }

    override fun writeAwaitingGameStart() {
        writeDeviceState(DeviceState.AwaitingGameStart)
    }

    override fun writeGamePaused(paused: Boolean) {
        writeDeviceState(DeviceState.Paused)
    }

    override fun writeTurnTimerEnforced(enforced: Boolean) {
        writeBool(turnTimeEnforcedCharacteristic, enforced)
    }

    override fun writeSkippedPlayers(skippedPlayers: Int) {
        writeInt(skippedPlayersCharacteristic, skippedPlayers)
    }

    override fun setDeviceState(deviceState: DeviceState) {
        super.setDeviceState(deviceState)
        writeDeviceState(deviceState)
    }

    companion object {
        const val TAG = "BLEDevice"
        val serviceUUID: UUID = UUID.fromString("d7560343-51d4-4c24-a0fe-118fd9078144")
        val totalPlayersUUID: UUID = UUID.fromString("d776071e-9584-42db-b095-798a90049ee0")
        val currentPlayerUUID: UUID = UUID.fromString("6efe0bd2-ad04-49bb-8436-b7e1d1902fea")
        val myPlayerUUID: UUID = UUID.fromString("f1223124-c708-4b98-a486-48515fa59d3d")
        val elapsedTimeUUID: UUID = UUID.fromString("4e1c05f6-c128-4bca-96c3-29c014e00eb6")
        val timerUUID: UUID = UUID.fromString("4661b4c1-093d-4db7-bb80-5b5fe3eae519")
        val turnTimerEnforcedUUID: UUID = UUID.fromString("8b732784-8a53-4a25-9436-99b9a5b9b73a")
        val deviceStateUUID: UUID = UUID.fromString("3f29c2e5-3837-4498-bcc1-cb33f1c10c3c")
        val skippedPlayersUUID: UUID = UUID.fromString("b31fa38e-a424-47ad-85d9-639cbab14e88")

        val skipToggleActionUUID: UUID = UUID.fromString("9b4fa66f-20cf-4a7b-ba6a-fc3890cbc0c7")
        val endTurnActionUUID: UUID = UUID.fromString("c27802ab-425e-4b15-8296-4a937da7125f")

        val clientCharacteristicConfigUUID: UUID =
            UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")
    }
}
