package com.etds.hourglass.model.Device

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.util.Log
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.nio.ByteBuffer
import java.util.UUID

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

    fun setConnection(connection: BluetoothGatt) {

    }

    private var numberOfPlayersCharacteristic: BluetoothGattCharacteristic? = null
    private var playerIndexCharacteristic: BluetoothGattCharacteristic? = null
    private var activeTurnCharacteristic: BluetoothGattCharacteristic? = null
    private var timerCharacteristic: BluetoothGattCharacteristic? = null
    private var elapsedTimeCharacteristic: BluetoothGattCharacteristic? = null
    private var currentPlayerCharacteristic: BluetoothGattCharacteristic? = null
    private var skippedCharacteristic: BluetoothGattCharacteristic? = null
    private var gameActiveCharacteristic: BluetoothGattCharacteristic? = null
    private var gamePausedCharacteristic: BluetoothGattCharacteristic? = null
    private var turnTimeEnforcedCharacteristic: BluetoothGattCharacteristic? = null



    private val gattCallback = object : BluetoothGattCallback() {
        @SuppressLint("MissingPermission")
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                // Connected to GATT server, now you can discover services
                _connection?.discoverServices()


                enableNotifications(activeTurnCharacteristic)
                enableNotifications(skippedCharacteristic)
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
                activeTurnCharacteristic = service?.getCharacteristic(activeTurnUUID)
                timerCharacteristic = service?.getCharacteristic(timerUUID)
                elapsedTimeCharacteristic = service?.getCharacteristic(elapsedTimeUUID)
                currentPlayerCharacteristic = service?.getCharacteristic(currentPlayerUUID)
                skippedCharacteristic = service?.getCharacteristic(skippedUUID)
                gameActiveCharacteristic = service?.getCharacteristic(gameActiveUUID)
                gamePausedCharacteristic = service?.getCharacteristic(gamePausedUUID)
                turnTimeEnforcedCharacteristic = service?.getCharacteristic(turnTimerEnforcedUUID)

                // Defaults
                writeGamePaused(true)
                writeGameActive(false)
                writeNumberOfPlayers(1)
                writeCurrentPlayer(0)
                writeTimer(6000)
                writePlayerIndex(0)
                writeTurnTimerEnforced(true)

            } else {
                Log.d(TAG, "Failed to discover servicesd")
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
                skippedCharacteristic -> skippedChange(value)
                activeTurnCharacteristic -> activeTurnChange(value)
            }
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray
        ) {
            super.onCharacteristicChanged(gatt, characteristic, value)
            when (characteristic) {
                skippedCharacteristic -> skippedChange(value)
                activeTurnCharacteristic -> activeTurnChange(value)
            }
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
        }
    }

    private fun skippedChange(value: ByteArray) {
        _skipped.value = byteArrayToBool(value)
        onSkipCallback?.invoke()
    }

    private fun activeTurnChange(value: ByteArray) {
        _activeTurn.value = byteArrayToBool(value)
        onActiveTurnCallback?.invoke()
    }

    private fun disconnect() {
        _connected.value = false
        onDisconnectCallback?.invoke()
    }

    @SuppressLint("MissingPermission")
    private fun enableNotifications(characteristic: BluetoothGattCharacteristic?) {
        characteristic?.let{
            val descriptor = characteristic.getDescriptor(clientCharacteristicConfigUUID)
            descriptor?.let {
                _connection?.writeDescriptor(
                    descriptor,
                    BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                )
            } ?: {
                Log.d(TAG, "Unable to get descriptor")
            }
        } ?: {
            Log.d(TAG, "Unable to get characteristic")
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

    @OptIn(DelicateCoroutinesApi::class)
    @SuppressLint("MissingPermission")
    private fun writeData(characteristic: BluetoothGattCharacteristic?, byteArray: ByteArray) {
        characteristic?.let {
            _connection?.writeCharacteristic(
                characteristic,
                byteArray,
                BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
            )
        } ?: {
            Log.d(TAG, "Characteristic not found")
        }
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
        writeData(characteristic, data)
    }

    private fun writeInt(characteristic: BluetoothGattCharacteristic?, value: Long) {
        writeInt(characteristic, value.toInt())
    }

    private fun writeBool(characteristic: BluetoothGattCharacteristic?, value: Boolean) {
        val data = boolToByteArray(value)
        writeData(characteristic, data)
    }

    override fun writeNumberOfPlayers(number: Int) {
        writeInt(numberOfPlayersCharacteristic, number)
    }

    override fun writePlayerIndex(index: Int) {
        writeInt(playerIndexCharacteristic, index)
    }

    override fun writeActiveTurn(active: Boolean) {
        writeBool(activeTurnCharacteristic, active)
    }

    override fun writeTimer(duration: Long) {
        writeInt(timerCharacteristic, duration)
    }

    override fun writeElapsedTime(duration: Long) {
        writeInt(elapsedTimeCharacteristic, duration)
    }

    override fun writeCurrentPlayer(index: Int) {
        writeInt(currentPlayerCharacteristic, index)
    }

    override fun writeSkipped(skipped: Boolean) {
        writeBool(skippedCharacteristic, skipped)
    }

    override fun writeGameActive(active: Boolean) {
        writeBool(gameActiveCharacteristic, active)
    }

    override fun writeGamePaused(paused: Boolean) {
        writeBool(gamePausedCharacteristic, paused)
    }

    override fun writeTurnTimerEnforced(enforced: Boolean) {
        writeBool(turnTimeEnforcedCharacteristic, enforced)
    }

    override fun readActiveTurn() {
        readValue(activeTurnCharacteristic)
    }

    override fun readSkipped() {
        readValue(skippedCharacteristic)
    }

    companion object {
        const val TAG = "BLEDevice"
        val serviceUUID: UUID = UUID.fromString("d7560343-51d4-4c24-a0fe-118fd9078144")
        val totalPlayersUUID: UUID = UUID.fromString("d776071e-9584-42db-b095-798a90049ee0")
        val currentPlayerUUID: UUID = UUID.fromString("6efe0bd2-ad04-49bb-8436-b7e1d1902fea")
        val myPlayerUUID: UUID = UUID.fromString("f1223124-c708-4b98-a486-48515fa59d3d")
        val activeTurnUUID: UUID = UUID.fromString("c27802ab-425e-4b15-8296-4a937da7125f")
        val elapsedTimeUUID: UUID = UUID.fromString("4e1c05f6-c128-4bca-96c3-29c014e00eb6")
        val skippedUUID: UUID = UUID.fromString("c1ed8823-7eb1-44b2-ac01-351e8c6a693c")
        val timerUUID: UUID = UUID.fromString("4661b4c1-093d-4db7-bb80-5b5fe3eae519")
        val gameActiveUUID: UUID = UUID.fromString("33280653-4d71-4714-a03c-83111b886aa7")
        val gamePausedUUID: UUID = UUID.fromString("643fda83-0c6b-4e8e-9829-cbeb20b70b8d")
        val turnTimerEnforcedUUID: UUID = UUID.fromString("8b732784-8a53-4a25-9436-99b9a5b9b73a")

        val clientCharacteristicConfigUUID: UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")
    }
}
