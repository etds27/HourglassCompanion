package com.etds.hourglass.data.BLEData.remote

import com.etds.hourglass.model.Device.BLEDevice

class BLERemoteDatasource {
    suspend fun fetchGameDevices(): List<BLEDevice> {
        return listOf(
            BLEDevice(address = "13:15:52:62", name = "FISCHER1"),
            BLEDevice(address = "14:15:52:62", name = "FISCHER2"),
            BLEDevice(address = "15:15:52:62", name = "FISCHER3"),
            BLEDevice(address = "16:15:52:62", name = "FISCHER4"),
            BLEDevice(address = "17:15:52:62", name = "FISCHER5")
        );
    }
}