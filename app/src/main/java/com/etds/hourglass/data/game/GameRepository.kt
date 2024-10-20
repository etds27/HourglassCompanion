package com.etds.hourglass.data.game

import android.util.Log
import com.etds.hourglass.data.BLEData.remote.BLERemoteDatasource
import com.etds.hourglass.data.game.local.LocalGameDatasource
import com.etds.hourglass.model.Device.GameDevice
import com.etds.hourglass.model.Player.Player
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Duration
import java.time.Instant
import java.util.UUID
import kotlin.math.min
import kotlin.time.toDuration

class GameRepository(
    private val localGameDatasource: LocalGameDatasource,
    private val bluetoothDatasource: BLERemoteDatasource,
    private val viewModelScope: CoroutineScope
) {
    suspend fun fetchGameDevices(): List<GameDevice> {
        return bluetoothDatasource.fetchGameDevices()
    }

    private val _isPaused = MutableStateFlow<Boolean>(false)
    val isPaused: StateFlow<Boolean> = _isPaused

    private val _timerDuration = MutableStateFlow<Long>(6000)
    val timerDuration: StateFlow<Long> = _timerDuration

    private val _totalTimerDuration = MutableStateFlow<Long>(900000)
    val totalTimerDuration: StateFlow<Long> = _totalTimerDuration

    private val _enforceTimer = MutableStateFlow<Boolean>(false)
    val enforceTimer: StateFlow<Boolean> = _enforceTimer

    private val _enforceTotalTimer = MutableStateFlow<Boolean>(true)
    val enforceTotalTimer: StateFlow<Boolean> = _enforceTotalTimer

    private val _activePlayerIndex = MutableStateFlow(0)
    private val activePlayerIndex: StateFlow<Int> = _activePlayerIndex

    private val _activePlayer = MutableStateFlow<Player?>(null)
    val activePlayer: StateFlow<Player?> = _activePlayer

    private val _skippedPlayers = MutableStateFlow<Set<Player>>(setOf())
    val skippedPlayers: StateFlow<Set<Player>> = _skippedPlayers

    private val _players = MutableStateFlow(getPlayers())
    val players: StateFlow<List<Player>> = _players

    private val _turnStart = MutableStateFlow(Instant.now())
    val turnStart: StateFlow<Instant> = _turnStart

    private val _elapsedTurnTime = MutableStateFlow<Long>(0)
    val elapsedTurnTime: StateFlow<Long> = _elapsedTurnTime

    private val _totalElapsedTurnTime = MutableStateFlow<Long>(value = 0)
    val totalElapsedTurnTime: StateFlow<Long> = _totalElapsedTurnTime

    private fun getPlayers(): List<Player> {
        return localGameDatasource.fetchPlayers()
    }

    private fun updateTurnTime() {
        _timerDuration.value = localGameDatasource.fetchTurnTime()
    }

    fun setTurnTime(duration: Long) {
        localGameDatasource.setTurnTime(duration)
        updateTurnTime()
    }

    private fun updateTotalTurnTime() {
        _totalTimerDuration.value = localGameDatasource.fetchTotalTurnTime()
    }

    fun setTotalTurnTime(duration: Long) {
        localGameDatasource.setTotalTurnTime(duration)
        updateTotalTurnTime()
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
        startTurn()
    }

    fun previousPlayer() {
        if (checkAllSkipped()) { return }
        val index = (activePlayerIndex.value - 1 + players.value.size) % players.value.size
        setActivePlayerIndex(index)
        if (skippedPlayers.value.contains(activePlayer.value)) {
            previousPlayer()
        }
        startTurn()
    }

    private suspend fun runTimer(
        startingTime: Long = 0L,
        startingPlayer: Player,
        elapsedTimeStateFlow: MutableStateFlow<Long>,
        enforceTimer: Boolean,
        timerMaxLength: Long
    ): Long {
        var lastUpdate = Instant.now()
        Log.d(TAG, "Starting Timer: Elapsed: ${elapsedTimeStateFlow.value}, duration: $timerMaxLength")
        var timerElapsedTime = startingTime
        elapsedTimeStateFlow.value = timerElapsedTime
        while (true) {
            delay(100L)
            if (isPaused.value) {
                lastUpdate = Instant.now()
                continue
            }
            if (elapsedTimeStateFlow.value >= timerMaxLength && enforceTimer) {
                Log.d(TAG, "Time limit reached")
                break
            }

            if (startingPlayer != activePlayer.value) {
                Log.d(TAG, "Active player changed from ${startingPlayer.name} to ${activePlayer.value!!.name}")
                break
            }
            elapsedTimeStateFlow.value = timerElapsedTime

            val now = Instant.now()
            Log.d(TAG, "elapsedTime: ${startingPlayer.name} ${elapsedTimeStateFlow.value} ${Duration.between(lastUpdate, now).toMillis()}")

            timerElapsedTime += Duration.between(lastUpdate, now).toMillis()
            lastUpdate = Instant.now()
        }
        if (enforceTimer && startingPlayer == activePlayer.value) {
            timerElapsedTime = min(timerElapsedTime, timerMaxLength)
            // elapsedTimeStateFlow.value = timerElapsedTime
        }

        return timerElapsedTime
    }

    private suspend fun startTurnTimer() {
        val startingPlayer = activePlayer.value ?: return
        var timerElapsedTime = 0L
        withContext(Dispatchers.Default) {
            timerElapsedTime = runTimer(
                startingPlayer = startingPlayer,
                startingTime = 0,
                elapsedTimeStateFlow = _elapsedTurnTime,
                timerMaxLength = timerDuration.value,
                enforceTimer = enforceTimer.value
            )

            // Skip to the next player if the turn timer was reached and then enforce timer was set
            if (activePlayer.value == startingPlayer &&
                elapsedTurnTime.value == timerElapsedTime &&
                enforceTimer.value) {
                Log.d(TAG, "Timer duration reached, advancing to next player")
                nextPlayer()
            }
        }
    }

    private suspend fun startTotalTurnTimer() {
        val startingPlayer = activePlayer.value ?: return
        withContext(Dispatchers.Default) {
            val timerElapsedTime = runTimer(
                startingPlayer = startingPlayer,
                startingTime = startingPlayer.totalTurnTime,
                elapsedTimeStateFlow = _totalElapsedTurnTime,
                timerMaxLength = totalTimerDuration.value,
                enforceTimer = enforceTotalTimer.value
            )

            // Skip to the next player if the turn timer was reached and then enforce timer was set
            if (activePlayer.value == startingPlayer &&
                _totalElapsedTurnTime.value == totalTimerDuration.value &&
                enforceTimer.value
            ) {
                Log.d(TAG, "Total timer duration reached, advancing to next player")
                nextPlayer()
            }

            // Add the elapsed turn time to the starting players total time
            Log.d(TAG, "Adding elapsed time to starting player: ${startingPlayer.name} ${startingPlayer.totalTurnTime} -> $timerElapsedTime")
            startingPlayer.totalTurnTime = timerElapsedTime
        }
    }

    fun startTurn() {
        viewModelScope.launch {
            val startingPlayer = activePlayer.value
            startingPlayer?: return@launch
            launch { startTotalTurnTimer() }
            launch { startTurnTimer() }
        }
    }

    companion object {
        val serviceUUID = UUID.fromString("d7560343-51d4-4c24-a0fe-118fd9078144")
        const val TAG = "GameRepository"
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