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
import com.etds.hourglass.model.Device.LocalDevice
import com.etds.hourglass.model.Player.Player
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.random.Random

@HiltViewModel
class GameDeviceViewModel @Inject constructor(
    private val gameRepository: GameRepository
) : ViewModel() {

    private val _autoConnectEnabled = MutableStateFlow<Boolean>(false)
    val autoConnectEnabled: StateFlow<Boolean> = _autoConnectEnabled

    private val _currentDevices = MutableStateFlow(mutableListOf<GameDevice>())
    val currentDevices: StateFlow<List<GameDevice>> = _currentDevices

    private val _connectedBLEDevices = MutableStateFlow(mutableListOf<GameDevice>())
    val connectedBLEDevices: StateFlow<List<GameDevice>> = _connectedBLEDevices

    val localDevicesCount: StateFlow<Int> = gameRepository.numberOfLocalDevices

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching

    private val _readyToStart = MutableStateFlow<Boolean>(false)
    val readyToStart: StateFlow<Boolean> = _readyToStart

    init {
        viewModelScope.launch {
            _connectedBLEDevices.value = gameRepository.fetchConnectedBLEDevices().toMutableList()

            while (!gameRepository.gameActive.value) {
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
                // gameRepository.updatePlayersList()
                delay(250)
                updateGameReadyToStart()
            }
        }
    }

    fun updateGameReadyToStart() {
        _readyToStart.value = (gameRepository.fetchConnectedDevices().size + localDevicesCount.value) > 1
    }

    fun fetchGameDevices() {
        viewModelScope.launch {
            _currentDevices.value = mutableListOf()
            val devices: MutableList<GameDevice> = gameRepository.fetchGameDevices().toMutableList()
            _currentDevices.value = devices
        }
    }

    fun startBLESearch() {
        viewModelScope.launch {
            _isSearching.value = true
            gameRepository.startBLESearch()
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

    fun addLocalPlayer() {
         gameRepository.addLocalDevice()
        updateGameReadyToStart()
    }

    fun removeLocalPlayer() {
        gameRepository.removeLocalDevice()
        updateGameReadyToStart()
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
                gameRepository.addPlayer(player = Player(
                    name = gameDevice.name,
                    device = gameDevice
                ))
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
                val player = gameRepository.playerWithDevice(gameDevice)
                player?.let { gameRepository.removePlayer(player) }

            }
            recomposeLists()
        }
    }

    private suspend fun recomposeLists() {
        _connectedBLEDevices.value = gameRepository.fetchConnectedBLEDevices().toMutableList()
        _currentDevices.value = gameRepository.fetchGameDevices().toMutableList()
        updateGameReadyToStart()
    }

    fun toggleAutoConnect() {
        _autoConnectEnabled.value = !_autoConnectEnabled.value
    }

    fun addLocalPlayers() {
        (0..<localDevicesCount.value).forEach { i ->
            val name = "Local Device ${i + 1}"
            val player = Player(
                name = name,
                device = LocalDevice(
                    name = name
                )
            )
            gameRepository.addPlayer(player)
        }
    }
}

/*
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
*/