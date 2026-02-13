package com.etds.hourglass.data.BLEData.BLENotification

import java.nio.ByteBuffer
import java.nio.ByteOrder

enum class HourglassMotorNotificationType(val value: Int) {
    TurnStarted(0),
    Poke(1),
    DeviceConnected(value=2);

    companion object {
        const val SIZE: Int = 4
    }
}


class HourglassMotorNotification(
    data: HourglassMotorNotificationType
): HourglassNotification<HourglassMotorNotificationType>(
    data = data,
    size = HourglassMotorNotificationType.SIZE
){
    override fun dataToByteArray(): ByteArray {
        val buffer = ByteBuffer.allocate(size)
        buffer.order(ByteOrder.LITTLE_ENDIAN)
        buffer.putInt(data.value)
        return buffer.array()
    }
}