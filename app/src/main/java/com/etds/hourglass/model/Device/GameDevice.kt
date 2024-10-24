package com.etds.hourglass.model.Device

import kotlinx.coroutines.flow.StateFlow

interface GameDevice {
    public val name: String
    public val address: String

    // Set to indicate that the device is in the process of pairing
    public var connecting: StateFlow<Boolean>
    public var connected: StateFlow<Boolean>


    public suspend fun connectToDevice(): Boolean
    public suspend fun disconnectFromDevice(): Boolean
}