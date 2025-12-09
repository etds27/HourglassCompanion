package com.etds.hourglass.model.Device

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.util.Log
import androidx.annotation.RequiresPermission
import com.etds.hourglass.model.DeviceState.DeviceState
import com.etds.hourglass.model.config.ColorConfig
import java.nio.charset.Charset
import java.util.LinkedList
import java.util.UUID


// Top-level interface and implementations for BLE operations
private interface BLEOperation {
    val characteristic: BluetoothGattCharacteristic
    fun perform(connection: BluetoothGatt)
}

private data class WriteOperation(
    override val characteristic: BluetoothGattCharacteristic,
    val byteArray: ByteArray,
) : BLEOperation {

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override fun perform(connection: BluetoothGatt) {
        val byteArrayString = byteArray.joinToString(" ") { "%02X".format(it) }
        Log.d("BLEOperation", "Performing data operation: ${characteristic.uuid}: $byteArrayString")
        connection.writeCharacteristic(
            characteristic,
            byteArray,
            BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
        )
    }

    override fun toString(): String {
        return "${characteristic.uuid}: $byteArray"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as WriteOperation

        if (characteristic != other.characteristic) return false
        if (!byteArray.contentEquals(other.byteArray)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = characteristic.hashCode()
        result = 31 * result + byteArray.contentHashCode()
        return result
    }
}

private data class ReadOperation(
    override val characteristic: BluetoothGattCharacteristic
) : BLEOperation {
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override fun perform(connection: BluetoothGatt) {
        Log.d("BLEOperation", "Performing data read operation: ${characteristic.uuid}")
        connection.readCharacteristic(
            characteristic
        )
    }
}

private data class EnableNotificationOperation(
    override val characteristic: BluetoothGattCharacteristic
) : BLEOperation {
    @SuppressLint("MissingPermission")
    override fun perform(connection: BluetoothGatt) {
        val descriptor =
            characteristic.getDescriptor(BLEDevice.clientCharacteristicConfigUUID) // Use Companion object
        connection.setCharacteristicNotification(
            characteristic,
            true
        )
        descriptor?.let {
            connection.writeDescriptor(
                descriptor,
                BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
            )
            Log.d(
                BLEDevice.TAG,
                "Enabled notifications for ${characteristic.uuid}"
            ) // Use Companion object
        } ?: {
            Log.d(BLEDevice.TAG, "Unable to get descriptor") // Use Companion object
        }
    }

    override fun toString(): String {
        return "Enabling BLE Notify for ${characteristic.uuid}"
    }
}

class BLEDevice(
    initialName: String = "", // Renamed parameter to avoid conflict with inherited 'name'
    address: String = "",
    val bluetoothDevice: BluetoothDevice? = null,
    val context: Context
) : GameDevice(
    initialName = initialName, // Pass to super constructor
    address = address
) {

    // Companion Object
    companion object {
        const val TAG = "BLEDevice"
        val serviceUUID: UUID = UUID.fromString("d7560343-51d4-4c24-a0fe-118fd9078144")
        val totalPlayersUUID: UUID = UUID.fromString("d776071e-9584-42db-b095-798a90049ee0")
        val currentPlayerUUID: UUID = UUID.fromString("6efe0bd2-ad04-49bb-8436-b7e1d1902fea")
        val myPlayerUUID: UUID = UUID.fromString("f1223124-c708-4b98-a486-48515fa59d3d")
        val elapsedTimeUUID: UUID = UUID.fromString("4e1c05f6-c128-4bca-96c3-29c014e00eb6")
        val timerUUID: UUID = UUID.fromString("4661b4c1-093d-4db7-bb80-5b5fe3eae519")
        val deviceStateUUID: UUID = UUID.fromString("3f29c2e5-3837-4498-bcc1-cb33f1c10c3c")
        val skippedPlayersUUID: UUID = UUID.fromString("b31fa38e-a424-47ad-85d9-639cbab14e88")

        val skipToggleActionUUID: UUID = UUID.fromString("9b4fa66f-20cf-4a7b-ba6a-fc3890cbc0c7")
        val endTurnActionUUID: UUID = UUID.fromString("c27802ab-425e-4b15-8296-4a937da7125f")

        val deviceNameUUID: UUID = UUID.fromString("050753a4-2b7a-41f9-912e-4310f5e750e6")
        val deviceNameWriteUUID: UUID = UUID.fromString("ba60a34c-ff34-4439-ae35-e262d8f77b3e")
        val deviceColorConfigUUID: UUID = UUID.fromString("85f6ff14-861b-47cf-8e41-5f5b94100bd9")
        val deviceColorConfigStateUUID: UUID = UUID.fromString("f4c4d6e1-3b1e-4d2a-8f3a-2e5b8f0c6d7e")
        val deviceColorConfigWriteUUID: UUID = UUID.fromString("4408c2ec-10c0-4a76-87ab-4d9a5b51eaa7")
        val deviceLEDOffsetUUID: UUID = UUID.fromString("aea26019-8b62-4292-a999-b565ecc71e1b")
        val deviceLEDOffsetWriteUUID: UUID = UUID.fromString("7d73338d-cf99-4d10-a946-d2417ea9dca7")
        val deviceLEDCountUUID: UUID = UUID.fromString("d7ce0c0d-833e-45ab-96da-852dc61463af")
        val deviceLEDCountWriteUUID: UUID = UUID.fromString("8c00da28-1352-45d9-b7bb-c4a5ba4dea40")

        val clientCharacteristicConfigUUID: UUID =
            UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")

        /// This is in milliseconds
        const val defaultBLERequestDelay = 100L
        const val defaultBLERequestFrequency = 1000 / defaultBLERequestDelay
    }

    // Private Properties - BLE Connection & State
    private var _connection: BluetoothGatt? = null
    private var _isWriting: Boolean = false
    private var operationQueue: LinkedList<BLEOperation> = LinkedList()

    // Private Properties - GATT Characteristics
    private var numberOfPlayersCharacteristic: BluetoothGattCharacteristic? = null
    private var playerIndexCharacteristic: BluetoothGattCharacteristic? = null
    private var timerCharacteristic: BluetoothGattCharacteristic? = null
    private var elapsedTimeCharacteristic: BluetoothGattCharacteristic? = null
    private var currentPlayerCharacteristic: BluetoothGattCharacteristic? = null
    private var skipToggleActionCharacteristic: BluetoothGattCharacteristic? = null
    private var endTurnActionCharacteristic: BluetoothGattCharacteristic? = null
    private var skippedPlayersCharacteristic: BluetoothGattCharacteristic? = null
    private var gameStateCharacteristic: BluetoothGattCharacteristic? = null


    // Config Characteristics
    private var deviceColorConfigCharacteristic: BluetoothGattCharacteristic? = null
    private var deviceColorConfigStateCharacteristic: BluetoothGattCharacteristic? = null
    private var deviceColorConfigWriteCharacteristic: BluetoothGattCharacteristic? = null
    private var deviceNameCharacteristic: BluetoothGattCharacteristic? = null
    private var deviceNameWriteCharacteristic: BluetoothGattCharacteristic? = null
    private var deviceLEDOffsetCharacteristic: BluetoothGattCharacteristic? = null
    private var deviceLEDOffsetWriteCharacteristic: BluetoothGattCharacteristic? = null
    private var deviceLEDCountCharacteristic: BluetoothGattCharacteristic? = null
    private var deviceLEDCountWriteCharacteristic: BluetoothGattCharacteristic? = null



    // GATT Callback Object
    private val gattCallback = object : BluetoothGattCallback() {
        @SuppressLint("MissingPermission")
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.d(TAG, "Device connected")
                onConnectionCallback?.invoke()
                _connection?.discoverServices()
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.d(TAG, "Device disconnected")
                disconnect()
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d(TAG, "Services Discovered")
                val service = _connection?.getService(serviceUUID)
                numberOfPlayersCharacteristic = service?.getCharacteristic(totalPlayersUUID)
                playerIndexCharacteristic = service?.getCharacteristic(myPlayerUUID)
                endTurnActionCharacteristic = service?.getCharacteristic(endTurnActionUUID)
                skipToggleActionCharacteristic = service?.getCharacteristic(skipToggleActionUUID)
                skippedPlayersCharacteristic = service?.getCharacteristic(skippedPlayersUUID)
                timerCharacteristic = service?.getCharacteristic(timerUUID)
                elapsedTimeCharacteristic = service?.getCharacteristic(elapsedTimeUUID)
                currentPlayerCharacteristic = service?.getCharacteristic(currentPlayerUUID)
                gameStateCharacteristic = service?.getCharacteristic(deviceStateUUID)

                // Config Characteristics
                deviceNameCharacteristic = service?.getCharacteristic(deviceNameUUID)
                deviceNameWriteCharacteristic = service?.getCharacteristic(deviceNameWriteUUID)
                deviceColorConfigCharacteristic = service?.getCharacteristic(deviceColorConfigUUID)
                deviceColorConfigStateCharacteristic = service?.getCharacteristic(deviceColorConfigStateUUID)
                deviceColorConfigWriteCharacteristic = service?.getCharacteristic(deviceColorConfigWriteUUID)
                deviceLEDOffsetCharacteristic = service?.getCharacteristic(deviceLEDOffsetUUID)
                deviceLEDOffsetWriteCharacteristic = service?.getCharacteristic(deviceLEDOffsetWriteUUID)
                deviceLEDCountCharacteristic = service?.getCharacteristic(deviceLEDCountUUID)
                deviceLEDCountWriteCharacteristic = service?.getCharacteristic(deviceLEDCountWriteUUID)


                // Initialize device state after service discovery
                writeNumberOfPlayers(1)
                writeCurrentPlayer(0)
                writeTimer(60000)
                writeElapsedTime(0)
                writeSkippedPlayers(0)
                writePlayerIndex(0)
                writeAwaitingGameStart()

                // Read initial device properties
                readDeviceName()
                // readValue(deviceColorConfigCharacteristic)  // Not necessary until in configurator

                readLEDOffset()
                readLEDCount()

                // Enable notifications for actions
                enableNotifications(endTurnActionCharacteristic)
                enableNotifications(skipToggleActionCharacteristic)
                enableNotifications(deviceNameCharacteristic)
                enableNotifications(deviceColorConfigCharacteristic)

                onServicesDiscoveredCallback?.invoke()
                onServicesRediscoveredCallback?.invoke()

                mutableConnectionState.value = DeviceConnectionState.Connected

                Log.d(TAG, "Services discovered")
            } else {
                Log.w(TAG, "Failed to discover services, status: $status")
            }
        }

        override fun onCharacteristicRead(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray,
            status: Int
        ) {
            super.onCharacteristicRead(gatt, characteristic, value, status)
            Log.d(
                TAG,
                "Characteristic read: ${characteristic.uuid}: ${value.contentToString()}"
            ) // Log value properly
            if (status == BluetoothGatt.GATT_SUCCESS) {
                when (characteristic) {
                    skipToggleActionCharacteristic -> skippedChange(value)
                    endTurnActionCharacteristic -> activeTurnChange(value)
                    deviceNameCharacteristic -> handleDeviceNameRead(value)
                    deviceColorConfigCharacteristic -> handleDeviceColorConfigRead(value)
                    deviceLEDOffsetCharacteristic -> handleDeviceLEDOffsetRead(value)
                    deviceLEDCountCharacteristic -> handleDeviceLEDCountRead(value)
                }
            } else {
                Log.w(
                    TAG,
                    "onCharacteristicRead failed for ${characteristic.uuid}, status: $status"
                )
            }
            _isWriting = false // Also process next op here as it's an async op
            processNextOperation()
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray
        ) {
            super.onCharacteristicChanged(gatt, characteristic, value)
            Log.d(
                TAG,
                "Characteristic changed: ${characteristic.uuid}: ${value.contentToString()}"
            )
            when (characteristic) {
                skipToggleActionCharacteristic -> skippedChange(value)
                endTurnActionCharacteristic -> activeTurnChange(value)
                deviceNameCharacteristic -> handleDeviceNameRead(value)
                deviceColorConfigCharacteristic -> handleDeviceColorConfigRead(value)
                deviceLEDOffsetCharacteristic -> handleDeviceLEDOffsetRead(value)
                deviceLEDCountCharacteristic -> handleDeviceLEDCountRead(value)
            }
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
                Log.w(TAG, "Unable to write characteristic ${characteristic.uuid}, status: $status")
            }
            _isWriting = false
            processNextOperation()
        }

        override fun onDescriptorWrite(
            gatt: BluetoothGatt?,
            descriptor: BluetoothGattDescriptor?,
            status: Int
        ) {
            super.onDescriptorWrite(gatt, descriptor, status)
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d(
                    TAG,
                    "Descriptor successfully written for ${descriptor?.characteristic?.uuid}"
                )
            } else {
                Log.w(
                    TAG,
                    "Unable to write descriptor for ${descriptor?.characteristic?.uuid}, status: $status"
                )
            }
            _isWriting = false // Also process next op here as it's an async op
            processNextOperation()
        }
    }

    // GATT Callback Helper Methods
    private fun handleDeviceNameRead(value: ByteArray) {
        val nameString = value.toString(Charset.defaultCharset())
        Log.d(TAG, "Device name read: $nameString")
        mutableName.value = nameString
    }

    private fun handleDeviceColorConfigRead(value: ByteArray) {
        mutableColorConfig.value = ColorConfig.fromByteArray(value)
        Log.d(TAG, "Device color config read: ${mutableColorConfig.value}")
        // Emit the change so that observers can progress
        mutableColorConfigChannel.trySend(mutableColorConfig.value)
    }

    private fun handleDeviceLEDOffsetRead(value: ByteArray) {
        val intValue = value[0].toInt()
        Log.d(TAG, "Device LED offset read: $intValue")
        mutableLEDOffset.value = intValue
        mutableLEDOffsetChannel.trySend(intValue)
    }

    private fun handleDeviceLEDCountRead(value: ByteArray) {
        val intValue = value[0].toInt()
        Log.d(TAG, "Device LED count read: $intValue")
        mutableLEDCount.value = intValue
        mutableLEDCountChannel.trySend(intValue)
    }


    private fun skippedChange(value: ByteArray) {
        val newValue = byteArrayToBool(value)
        Log.d(TAG, "skippedChange invoked with: $newValue")
        onSkipCallback?.invoke(newValue)
    }

    private fun activeTurnChange(value: ByteArray) {
        val newValue = byteArrayToBool(value)
        Log.d(TAG, "activeTurnChange invoked with: $newValue")
        onActiveTurnCallback?.invoke(newValue)
    }

    // GameDevice Overrides - Connection
    @SuppressLint("MissingPermission")
    override suspend fun connectToDevice(): Boolean {
        if (bluetoothDevice == null) {
            Log.e(TAG, "BluetoothDevice is null, cannot connect.")
            return false
        }
        mutableConnectionState.value = DeviceConnectionState.Connecting
        _connection = bluetoothDevice.connectGatt(context, false, gattCallback)
        return _connection != null
    }

    @SuppressLint("MissingPermission")
    override suspend fun disconnectFromDevice(): Boolean {
        _connection?.disconnect() // Triggers onConnectionStateChange eventually
        // close() will be called in disconnect() method
        return true
    }

    // GameDevice Overrides - Write Operations
    override fun writeNumberOfPlayers(number: Int) {
        writeInt(numberOfPlayersCharacteristic, number)
    }

    override fun writePlayerIndex(index: Int) {
        writeInt(playerIndexCharacteristic, index)
    }

    override fun writeTimer(duration: Long) {
        Log.d(TAG, "writeTimer: ${this.name.value}: $duration")
        writeInt(timerCharacteristic, duration)
    }

    override fun writeElapsedTime(duration: Long) {
        Log.d(TAG, "writeElapsedTime: ${this.name.value}: $duration")
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

    override fun writeGamePaused(paused: Boolean) { // `paused` parameter not used
        writeDeviceState(DeviceState.Paused)
    }

    override fun writeSkippedPlayers(skippedPlayers: Int) {
        writeInt(skippedPlayersCharacteristic, skippedPlayers)
    }

    override fun writeDeviceName(name: String) {
        super.writeDeviceName(name) // Updates mutableName
        writeString(deviceNameCharacteristic, value = name)
    }

    override fun writeDeviceNameWrite(boolean: Boolean) {
        writeBool(deviceNameWriteCharacteristic, boolean)
    }

    override fun writeDeviceColorConfig(color: ColorConfig) {
        super.writeDeviceColorConfig(color) // Updates mutableColor
        Log.d(TAG, "writeDeviceColorConfig: ${this.name.value}: $color")
        writeByteArray(deviceColorConfigCharacteristic, color.toByteArray())
    }

    override fun writeColorConfigState(state: DeviceState) {
        super.writeColorConfigState(state)
        Log.d(TAG, "writeColorConfigState: ${this.name.value}: $state")
        writeInt(deviceColorConfigStateCharacteristic, state.value)
    }

    override fun writeColorConfigWrite(boolean: Boolean) {
        writeBool(deviceColorConfigWriteCharacteristic, boolean)
    }

    override fun writeLEDOffset(offset: Int) {
        Log.d(TAG, "writeLEDOffset: ${this.name.value}: $offset")
        writeInt(characteristic = deviceLEDOffsetCharacteristic, value = offset)
    }

    override fun writeLEDOffsetWrite(boolean: Boolean) {
        Log.d(TAG, "writeLEDOffsetWrite: ${this.name.value}: $boolean")
        writeBool(characteristic = deviceLEDOffsetWriteCharacteristic, value = boolean)
    }

    override fun writeLEDCount(count: Int) {
        Log.d(TAG, "writeLEDCount: ${this.name.value}: $count")
        writeInt(characteristic = deviceLEDCountCharacteristic, value = count)
    }

    override fun writeLEDCountWrite(boolean: Boolean) {
        Log.d(TAG, "writeLEDCountWrite: ${this.name.value}: $boolean")
        writeBool(characteristic = deviceLEDCountWriteCharacteristic, value = boolean)
    }

    override fun setDeviceState(deviceState: DeviceState) {
        super.setDeviceState(deviceState)
        writeDeviceState(deviceState)
    }

    // GameDevice Overrides - Read Operations (Getters for StateFlow values)
    override fun readDeviceName() {
        readValue(deviceNameCharacteristic)
    }

    override suspend fun readDeviceColorConfig() {
        readValue(deviceColorConfigCharacteristic)
    }

    override fun readLEDOffset() {
        readValue(deviceLEDOffsetCharacteristic)
    }

    override fun readLEDCount() {
        readValue(deviceLEDCountCharacteristic)
    }

    override fun fetchDeviceName(): String {
        return mutableName.value
    }

    override fun fetchDeviceColorConfig(): ColorConfig {
        return colorConfig.value
    }

    // BLE Operation Queue Management
    private fun enqueueOperation(operation: BLEOperation) {
        operationQueue.add(operation)
        processNextOperation() // Try to process immediately
    }

    private fun enqueueOperation(
        characteristic: BluetoothGattCharacteristic,
        byteArray: ByteArray
    ) {
        enqueueOperation(WriteOperation(characteristic = characteristic, byteArray = byteArray))
    }

    @SuppressLint("MissingPermission")
    private fun processNextOperation() {
        if (_isWriting || operationQueue.isEmpty() || _connection == null) {
            return
        }
        _isWriting = true
        val operation =
            operationQueue.peek() // Peek first to avoid issues if perform fails or is slow
        Log.d(TAG, "Processing operation: $operation")
        _connection?.let { gatt ->
            operation?.perform(gatt) // operation could be null if queue becomes empty concurrently
            operationQueue.poll() // Remove after successfully initiating perform
        } ?: run {
            Log.w(TAG, "Unable to perform operation. Connection is null. Operation still in queue.")
            _isWriting = false // Allow retry if connection re-establishes
        }
        // Log remaining after attempting one, if more, call processNext again.
        // The callback (onCharacteristicWrite/onDescriptorWrite) will also call processNextOperation.
        // This ensures the queue keeps moving if the current op finishes quickly or if a new op is added.
        Log.d(TAG, "Remaining operations to process: ${operationQueue.size}")
        if (!_isWriting && operationQueue.isNotEmpty()) { // If not writing (e.g. op failed to start) and queue has items
            processNextOperation()
        }
    }


    // Private BLE Helper Methods
    @SuppressLint("MissingPermission")
    private fun disconnect() { // This is called from onConnectionStateChange or by disconnectFromDevice
        mutableConnectionState.value = DeviceConnectionState.Disconnected
        onDisconnectCallback?.invoke()
        _connection?.close() // Close GATT client
        _connection = null
        operationQueue.clear() // Clear pending operations on disconnect
        _isWriting = false
    }

    @SuppressLint("MissingPermission")
    private fun enableNotifications(characteristic: BluetoothGattCharacteristic?) {
        characteristic?.let {
            enqueueOperation(EnableNotificationOperation(it))
        } ?: Log.w(TAG, "Characteristic not found, cannot enable notifications.")
    }

    @SuppressLint("MissingPermission")
    private fun readValue(characteristic: BluetoothGattCharacteristic?) {
        Log.d(TAG, "readValue: ${this.name.value}")
        val operation = ReadOperation(
            characteristic = characteristic ?: return
        )
        enqueueOperation(operation)
    }

    // Data Writing Helpers
    private fun writeDeviceState(deviceState: DeviceState) {
        writeInt(characteristic = gameStateCharacteristic, deviceState.value)
    }

    private fun writeInt(characteristic: BluetoothGattCharacteristic?, value: Int) {
        Log.d(TAG, "writeInt: ${this.name.value}: $value")
        val data = intToByteArray(value)
        characteristic?.let {
            enqueueOperation(it, data)
        } ?: Log.w(
            TAG,
            "${this.name.value}: Unable to write Int to null characteristic"
        )
    }

    private fun writeInt(
        characteristic: BluetoothGattCharacteristic?,
        value: Long
    ) { // Convenience for Long
        writeInt(characteristic, value.toInt())
    }

    private fun writeBool(characteristic: BluetoothGattCharacteristic?, value: Boolean) {
        val data = boolToByteArray(value)
        characteristic?.let {
            enqueueOperation(it, data)
        } ?: Log.w(
            TAG,
            "${this.name.value}: Unable to write Bool to null characteristic"
        )
    }

    private fun writeString(characteristic: BluetoothGattCharacteristic?, value: String) {
        val data = value.toByteArray(Charset.defaultCharset())
        characteristic?.let {
            enqueueOperation(it, data)
        } ?: Log.w(
            TAG,
            "${this.name.value}: Unable to write String to null characteristic"
        )
    }

    private fun writeByteArray(characteristic: BluetoothGattCharacteristic?, data: ByteArray) {
        characteristic?.let {
            enqueueOperation(it, data)
        } ?: Log.w(
            TAG,
            "${this.name.value}: Unable to write byte array to null characteristic"
        )
    }

    // Data Conversion Utilities
    private fun intToByteArray(value: Int): ByteArray {
        // Little-endian order
        return ByteArray(4).apply {
            this[0] = (value and 0xFF).toByte()
            this[1] = ((value shr 8) and 0xFF).toByte()
            this[2] = ((value shr 16) and 0xFF).toByte()
            this[3] = ((value shr 24) and 0xFF).toByte()
        }
    }

    private fun byteArrayToInt(byteArray: ByteArray): Int {
        if (byteArray.isEmpty()) return 0

        // Assuming little-endian from peripheral
        var result = 0
        for (i in byteArray.indices.reversed()) { // Iterate from last byte to first
            result = (result shl 8) or (byteArray[i].toInt() and 0xFF)
        }
        // If size is less than 4, this will still work correctly by shifting fewer times.
        // For robust padding if needed (e.g. peripheral sends variable length but expects fixed on our side):
        // val paddedArray = ByteArray(4)
        // byteArray.copyInto(paddedArray, 0, 0, byteArray.size.coerceAtMost(4))
        // return ByteBuffer.wrap(paddedArray).order(ByteOrder.LITTLE_ENDIAN).int // If using ByteBuffer
        return result
    }

    private fun boolToByteArray(value: Boolean): ByteArray {
        return byteArrayOf(if (value) 0x01 else 0x00)
    }

    private fun byteArrayToBool(byteArray: ByteArray): Boolean {
        return byteArray.isNotEmpty() && byteArray[0] > 0
    }
}
