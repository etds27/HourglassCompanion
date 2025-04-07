package com.etds.hourglass.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.etds.hourglass.data.game.BuzzerGameRepository
import com.etds.hourglass.data.game.GameRepository
import com.etds.hourglass.model.Device.GameDevice
import com.etds.hourglass.model.Device.LocalDevice
import com.etds.hourglass.model.Player.Player
import com.etds.hourglass.model.game_mode_navigation.BuzzerGameModeNavigationConfig
import com.etds.hourglass.model.game_mode_navigation.GameModeNavigationConfig
import com.etds.hourglass.model.game_mode_navigation.ParallelGameModeNavigationConfig
import com.etds.hourglass.model.game_mode_navigation.SequentialGameModeNavigationConfig
import com.etds.hourglass.model.game_mode_navigation.SoloGameModeNavigationConfig
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton


interface GameDeviceViewModelProtocol {
    val autoConnectEnabled: StateFlow<Boolean>
    val currentDevices: StateFlow<List<GameDevice>>
    val connectedBLEDevices: StateFlow<List<GameDevice>>
    val localDevicesCount: StateFlow<Int>
    val isSearching: StateFlow<Boolean>
    val readyToStart: StateFlow<Boolean>
    val gameTypes: StateFlow<List<GameModeNavigationConfig>>

    fun updateGameReadyToStart()
    fun fetchGameDevices()
    fun startBLESearch()
    fun stopSearching()
    fun toggleDeviceConnection(gameDevice: GameDevice)
    fun addLocalPlayer()
    fun removeLocalPlayer()
    fun toggleAutoConnect()
    fun addLocalPlayers()

    fun connectToDevice(gameDevice: GameDevice)
    fun disconnectFromDevice(gameDevice: GameDevice)
}

abstract class BaseGameDeviceViewModel : ViewModel(), GameDeviceViewModelProtocol {
    override val gameTypes: StateFlow<List<GameModeNavigationConfig>> = MutableStateFlow(
        mutableListOf(
            SequentialGameModeNavigationConfig,
            BuzzerGameModeNavigationConfig,
            SoloGameModeNavigationConfig,
            ParallelGameModeNavigationConfig
        )
    )


    override fun toggleDeviceConnection(gameDevice: GameDevice) {
        if (gameDevice.connected.value) {
            disconnectFromDevice(gameDevice)
        } else {
            connectToDevice(gameDevice)
        }
    }

    fun isReadyToStart(): Boolean {
        return (connectedBLEDevices.value.count() + localDevicesCount.value) > 1
    }
}

@HiltViewModel
class GameDeviceViewModel @Inject constructor(
    private val gameRepository: BuzzerGameRepository
) : BaseGameDeviceViewModel(), GameDeviceViewModelProtocol {

    private val _autoConnectEnabled = MutableStateFlow<Boolean>(false)
    override val autoConnectEnabled: StateFlow<Boolean> = _autoConnectEnabled

    private val _currentDevices = MutableStateFlow(mutableListOf<GameDevice>())
    override val currentDevices: StateFlow<List<GameDevice>> = _currentDevices

    private val _connectedBLEDevices = MutableStateFlow(mutableListOf<GameDevice>())
    override val connectedBLEDevices: StateFlow<List<GameDevice>> = _connectedBLEDevices

    override val localDevicesCount: StateFlow<Int> = gameRepository.numberOfLocalDevices

    override val isSearching: StateFlow<Boolean> = gameRepository.isSearching

    private val _readyToStart = MutableStateFlow<Boolean>(false)
    override val readyToStart: StateFlow<Boolean> = _readyToStart

    init {
        viewModelScope.launch {
            _connectedBLEDevices.value =
                gameRepository.fetchConnectedBLEDevices().toMutableList()

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

    override fun updateGameReadyToStart() {
        _readyToStart.value = isReadyToStart()
    }

    override fun fetchGameDevices() {
        viewModelScope.launch {
            _currentDevices.value = mutableListOf()
            val devices: MutableList<GameDevice> =
                gameRepository.fetchGameDevices().toMutableList()
            _currentDevices.value = devices
        }
    }

    override fun startBLESearch() {
        viewModelScope.launch {
            gameRepository.startBLESearch()
        }
    }


    override fun stopSearching() {
        gameRepository.stopBLESearch()
    }


    override fun addLocalPlayer() {
        gameRepository.addLocalDevice()
        updateGameReadyToStart()
    }

    override fun removeLocalPlayer() {
        gameRepository.removeLocalDevice()
        updateGameReadyToStart()
    }

    override fun connectToDevice(gameDevice: GameDevice) {
        // Ignore request if already attempting to connect
        if (gameDevice.connecting.value || gameDevice.connected.value) {
            return
        }
        viewModelScope.launch {
            recomposeLists()
            gameRepository.connectToDevice(gameDevice)
            recomposeLists()
        }
    }

    override fun disconnectFromDevice(gameDevice: GameDevice) {
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

    override fun toggleAutoConnect() {
        _autoConnectEnabled.value = !_autoConnectEnabled.value
    }

    override fun addLocalPlayers() {
        (1..localDevicesCount.value).forEach { i ->
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

class MockGameDeviceViewModel() : BaseGameDeviceViewModel() {
    private val mutableAutoConnectEnabled = MutableStateFlow(false)
    override val autoConnectEnabled: StateFlow<Boolean> = mutableAutoConnectEnabled

    private val mutableCurrentDevices = MutableStateFlow(
        mutableListOf(
            LocalDevice(name = "Mock Device 1"),
            LocalDevice(name = "Mock Device 2"),
            LocalDevice(name = "Mock Device 3"),
            LocalDevice(name = "Mock Device 4"),
        )
    )
    override val currentDevices: StateFlow<List<GameDevice>> = mutableCurrentDevices

    private val mutableConnectedBLEDevices = MutableStateFlow(listOf<GameDevice>())
    override val connectedBLEDevices: StateFlow<List<GameDevice>> = mutableConnectedBLEDevices

    private val mutableLocalDevicesCount = MutableStateFlow(0)
    override val localDevicesCount: StateFlow<Int> = mutableLocalDevicesCount

    private val mutableIsSearching = MutableStateFlow(true)
    override val isSearching: StateFlow<Boolean> = mutableIsSearching

    private val mutableReadyToStart = MutableStateFlow(false)
    override val readyToStart: StateFlow<Boolean> = mutableReadyToStart

    override fun updateGameReadyToStart() {
        mutableReadyToStart.value = isReadyToStart()
    }

    override fun fetchGameDevices() {

    }

    override fun startBLESearch() {
        mutableIsSearching.value = true
    }

    override fun stopSearching() {
        mutableIsSearching.value = false
    }

    override fun addLocalPlayer() {
        mutableLocalDevicesCount.value++
    }

    override fun removeLocalPlayer() {
        mutableLocalDevicesCount.value--
    }

    override fun toggleAutoConnect() {
        mutableAutoConnectEnabled.value = !mutableAutoConnectEnabled.value
    }

    override fun addLocalPlayers() {
        mutableLocalDevicesCount.value++
    }

    override fun connectToDevice(gameDevice: GameDevice) {
        mutableConnectedBLEDevices.value = mutableConnectedBLEDevices.value.plus(gameDevice)
    }

    override fun disconnectFromDevice(gameDevice: GameDevice) {
        mutableConnectedBLEDevices.value = mutableConnectedBLEDevices.value.minus(gameDevice)
    }
}