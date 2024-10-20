package com.etds.hourglass.data.game

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothGatt
import android.bluetooth.le.BluetoothLeScanner
import androidx.compose.runtime.collectAsState
import com.etds.hourglass.data.BLEData.remote.BLERemoteDatasource
import com.etds.hourglass.data.game.local.LocalGameDatasource
import com.etds.hourglass.model.Device.BLEDevice
import com.etds.hourglass.model.Device.GameDevice
import com.etds.hourglass.model.Player.Player
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.UUID

class GameRepository(
    private val localGameDatasource: LocalGameDatasource,
    private val bluetoothDatasource: BLERemoteDatasource
) {
    suspend fun fetchGameDevices(): List<GameDevice> {
        return bluetoothDatasource.fetchGameDevices()
    }

    private val _isPaused = MutableStateFlow<Boolean>(false)
    val isPaused: StateFlow<Boolean> = _isPaused

    private val _timerDuration = MutableStateFlow<Int>(60)
    val timerDuration: StateFlow<Int> = _timerDuration

    private val _enforceTimer = MutableStateFlow<Boolean>(false)
    val enforceTimer: StateFlow<Boolean> = _enforceTimer

    private val _activePlayerIndex = MutableStateFlow(0)
    private val activePlayerIndex: StateFlow<Int> = _activePlayerIndex

    private val _activePlayer = MutableStateFlow<Player?>(null)
    val activePlayer: StateFlow<Player?> = _activePlayer

    private val _skippedPlayers = MutableStateFlow<Set<Player>>(setOf())
    val skippedPlayers: StateFlow<Set<Player>> = _skippedPlayers

    private val _players = MutableStateFlow(getPlayers())
    val players: StateFlow<List<Player>> = _players

    private fun getPlayers(): List<Player> {
        return localGameDatasource.fetchPlayers()
    }

    fun pauseGame() {
        _isPaused.value = true
        _activePlayer.value = null
    }

    fun resumeGame() {
        _isPaused.value = false
        updateActivePlayer()
    }

    fun setSkippedPlayer(player: Player) {
        localGameDatasource.setSkippedPlayer(player)
        updateSkippedPlayers()
        if (player == activePlayer.value) {
            nextPlayer()
        }
        checkAllSkipped()
    }

    fun setUnskippedPlayer(player: Player) {
        localGameDatasource.setUnskippedPlayer(player)
        updateSkippedPlayers()
    }

    private fun updateSkippedPlayers() {
        _skippedPlayers.value = localGameDatasource.fetchSkippedPlayers().toMutableSet()
    }

    private fun getActivePlayer(): Player {
        return _players.value[activePlayerIndex.value]
    }

    private fun setActivePlayerIndex(index: Int) {
        _activePlayerIndex.value = index
        _activePlayer.value = players.value[index]
    }

    private fun updateActivePlayer() {
        setActivePlayerIndex(activePlayerIndex.value)
    }

    private fun checkAllSkipped(): Boolean {
        if (skippedPlayers.value.size == players.value.size) {
            pauseGame()
            players.value.forEach { player ->
                setUnskippedPlayer(player)
            }
            return true
        }
        return false
    }

    fun nextPlayer() {
        if (checkAllSkipped()) { return }
        val index = (activePlayerIndex.value + 1) % players.value.size
        setActivePlayerIndex(index)
        if (skippedPlayers.value.contains(activePlayer.value)) {
            nextPlayer()
        }
    }

    fun previousPlayer() {
        if (checkAllSkipped()) { return }
        val index = (activePlayerIndex.value - 1 + players.value.size) % players.value.size
        setActivePlayerIndex(index)
        if (skippedPlayers.value.contains(activePlayer.value)) {
            previousPlayer()
        }
    }

    companion object {
        val serviceUUID = UUID.fromString("d7560343-51d4-4c24-a0fe-118fd9078144")
    }

    /*

    private val bluetoothAdapter: BluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    private val bluetoothLeScanner: BluetoothLeScanner = bluetoothAdapter.bluetoothLeScanner
    private val connections: MutableList<BluetoothGatt> = mutableListOf()

    private val _discoveredDevices = MutableStateFlow<List<GameDevice>>(listOf())
    private val discoveredDevices: StateFlow<List<GameDevice>> = _discoveredDevices
    */
    /*
    suspend fun discoverGameDevices(): List<BLEDevice> {
    bluetoothLeScanner.startScan(scanCallback)
    return remoteDatasource.getGameDevices()
    }

    private val scanCallback = object : ScanCallback() {
    override fun onScanResult(callbackType: Int, result: ScanResult?) {
        result?.scanRecord.serviceUuids. .serviceUuids?.contains(serviceUUID)
        super.onScanResult(callbackType, result)
    }
    */
}