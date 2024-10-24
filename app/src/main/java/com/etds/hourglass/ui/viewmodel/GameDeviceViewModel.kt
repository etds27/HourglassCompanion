package com.etds.hourglass.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.compose.viewModel
import com.etds.hourglass.data.BLEData.BLERepository
import com.etds.hourglass.data.BLEData.remote.BLERemoteDatasource
import com.etds.hourglass.data.game.GameRepository
import com.etds.hourglass.data.game.local.LocalGameDatasource
import com.etds.hourglass.model.Device.GameDevice
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlin.random.Random

class GameDeviceViewModel(
    context: Context
) : ViewModel() {
    private val gameRepository: GameRepository = GameRepository(
        localGameDatasource = LocalGameDatasource(),
        bluetoothDatasource = BLERemoteDatasource(context),
        viewModelScope = viewModelScope,
    )

    private val _autoConnectEnabled = MutableStateFlow<Boolean>(false)
    val autoConnectEnabled: StateFlow<Boolean> = _autoConnectEnabled

    private val _currentDevices = MutableStateFlow(mutableListOf<GameDevice>())
    val currentDevices: StateFlow<List<GameDevice>> = _currentDevices

    private val _connectedDevices = MutableStateFlow(mutableListOf<GameDevice>())
    val connectedDevices: StateFlow<List<GameDevice>> = _connectedDevices

    private val _localDevices = MutableStateFlow(mutableListOf<GameDevice>())
    val localDevices: StateFlow<List<GameDevice>> = _localDevices

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching

    init {
        viewModelScope.launch {
            while (true) {
                if (isSearching.value) {
                    fetchGameDevices()
                }
                if (_autoConnectEnabled.value) {
                    currentDevices.value.forEach { device ->
                        if (device.connected.value) {
                            return@forEach
                        }
                        connectToDevice(device)
                        delay(1000)
                    }
                }
                delay(250)
            }
        }
    }

    fun fetchGameDevices() {
        viewModelScope.launch {
            _localDevices.value = mutableListOf(gameRepository.fetchLocalDevice()).toMutableList()
            _isSearching.value = true
            _currentDevices.value = mutableListOf()
            delay(2500)
            val devices: MutableList<GameDevice> = gameRepository.fetchGameDevices().toMutableList()
            _currentDevices.value = devices
            _isSearching.value = false
        }
    }


    fun stopSearching() {
        _isSearching.value = false
        _currentDevices.value = mutableListOf()
    }

    fun toggleDeviceConnection(gameDevice: GameDevice) {
        if (gameDevice.connected.value) {
            disconnectFromDevice(gameDevice)
        } else {
            connectToDevice(gameDevice)
        }
    }

    private fun connectToDevice(gameDevice: GameDevice) {
        // Ignore request if already attempting to connect
        if (gameDevice.connecting.value || gameDevice.connected.value) {
            return
        }
        viewModelScope.launch {
            recomposeLists()
            if (gameDevice.connectToDevice()) {
                gameRepository.addConnectedDevice(gameDevice)
            }
            recomposeLists()
        }
    }

    private fun disconnectFromDevice(gameDevice: GameDevice) {
        // Ignore request if already attempting to connect
        if (gameDevice.connecting.value || !gameDevice.connected.value) {
            return
        }
        viewModelScope.launch {
            recomposeLists()
            if (gameDevice.disconnectFromDevice()) {
                gameRepository.removeConnectedDevice(gameDevice)
            }
            recomposeLists()
        }
    }

    private suspend fun recomposeLists() {
        _connectedDevices.value = gameRepository.fetchConnectedDevices().toMutableList()
        _currentDevices.value = gameRepository.fetchGameDevices().toMutableList()
        _localDevices.value = mutableListOf(gameRepository.fetchLocalDevice()).toMutableList()
    }

    fun toggleAutoConnect() {
        _autoConnectEnabled.value = !_autoConnectEnabled.value
    }
}

class GameDeviceViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(
        modelClass: Class<T>,
        extras: CreationExtras
    ): T {
        return GameDeviceViewModel(
            context = context
        ) as T
    }
}
