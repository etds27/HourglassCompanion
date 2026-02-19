package com.etds.hourglass.data.BLEData.BLENotification

import java.nio.ByteBuffer
import java.nio.ByteOrder

/// The constructor takes in two pieces of data
/// One is the data that will be encoded and sent to the peripheral
/// The other is the size of the data that will be sent. This is the size of the data that
/// the data portion of the notification will be. This does not include the size of the timestamp
/// that will be appended to the end of the data.
/// E.g
/// data: Int
/// size: 4 (not 12 which is the size of the int plus the size of the timestamp)
abstract class HourglassNotification<T>(
    val data: T,
    val size: Int
) {
    fun toByteArray(): ByteArray {
        val buffer = ByteBuffer.allocate(size + TIMESTAMP_SIZE)
        buffer.order(ByteOrder.LITTLE_ENDIAN)
        buffer.putLong(System.currentTimeMillis())

        buffer.put(dataToByteArray())

        return buffer.array()
    }

    /// Convert the data portion of the notification to a byte array
    /// This should be a maximum of 16 bytes
    internal abstract fun dataToByteArray(): ByteArray

    companion object {
        private const val TIMESTAMP_SIZE = 8
    }
}