package com.etds.hourglass.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.etds.hourglass.data.BLEData.BLERepository
import com.etds.hourglass.data.game.GameRepository
import com.etds.hourglass.model.Device.GameDevice
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class GameDeviceViewModel(
    private val gameRepository: GameRepository
): ViewModel() {
    private val _currentDevices = MutableStateFlow<List<GameDevice>>(listOf<GameDevice>())
    val currentDevices: StateFlow<List<GameDevice>> = _currentDevices

    private val _isSearching = MutableStateFlow<Boolean>(false)
    val isSearching: StateFlow<Boolean> = _isSearching

    private val _startingPlayerDevice = MutableStateFlow<GameDevice?>(null);
    val startingPlayerDevice: StateFlow<GameDevice?> = _startingPlayerDevice;

    fun fetchGameDevices() {
        viewModelScope.launch {
            _isSearching.value = true
            _currentDevices.value = listOf()
            _startingPlayerDevice.value = null
            delay(2000)
            // val devices = bleDeviceRepository.discoverGameDevices()
            val devices: List<GameDevice> = gameRepository.f
            _currentDevices.value = devices
            _startingPlayerDevice.value = devices.first()
            _isSearching.value = false
        }
    }

    fun selectStartingDevice(gameDevice: GameDevice) {
        _startingPlayerDevice.value = gameDevice
    }
}