package com.etds.hourglass.model.Device

import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class LocalDevice(
    override val name: String,
    override val address: String,

) : GameDevice {

    private var _connecting = MutableStateFlow(false)
    private var _connected = MutableStateFlow(false)
    override var connecting: StateFlow<Boolean> = _connecting
    override var connected: StateFlow<Boolean> = _connected

    override suspend fun connectToDevice(): Boolean {
        _connected.value = true
        return true
    }

    override suspend fun disconnectFromDevice(): Boolean {
        _connected.value = false
        return true
    }

}