package com.etds.hourglass.data.game

import android.util.Log
import com.etds.hourglass.data.BLEData.remote.BLERemoteDatasource
import com.etds.hourglass.data.game.local.LocalGameDatasource
import com.etds.hourglass.model.Device.GameDevice
import com.etds.hourglass.model.Device.LocalDevice
import com.etds.hourglass.model.DeviceState.DeviceState
import com.etds.hourglass.model.Game.Round
import com.etds.hourglass.model.Player.Player
import com.etds.hourglass.util.CountDownTimer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.min

// @Singleton
abstract class GameRepository(
    private val localGameDatasource: LocalGameDatasource,
    private val bluetoothDatasource: BLERemoteDatasource,
    private val scope: CoroutineScope
) {
    private val _defaultPausedValue: Boolean = false
    private val _defaultEnforceTimer: Boolean = false
    private val _defaultTotalTimerDuration: Long = 900000
    private val _defaultTimerDuration: Long = 6000
    private val _defaultGameActive: Boolean = false

    private val _numberOfLocalDevices =
        MutableStateFlow(localGameDatasource.fetchNumberOfLocalDevices())
    val numberOfLocalDevices: StateFlow<Int> = _numberOfLocalDevices

    private val _isPaused = MutableStateFlow(_defaultPausedValue)
    val isPaused: StateFlow<Boolean> = _isPaused

    private val _timerDuration = MutableStateFlow<Long>(_defaultTimerDuration)
    val timerDuration: StateFlow<Long> = _timerDuration

    private val _totalTimerDuration = MutableStateFlow(_defaultTotalTimerDuration)
    val totalTimerDuration: StateFlow<Long> = _totalTimerDuration

    private val _enforceTimer = MutableStateFlow(_defaultEnforceTimer)
    val enforceTimer: StateFlow<Boolean> = _enforceTimer

    protected val mutableSkippedPlayers = MutableStateFlow<Set<Player>>(setOf())
    val skippedPlayers: StateFlow<Set<Player>> = mutableSkippedPlayers

    protected val mutablePlayers = MutableStateFlow(getPlayers())
    val players: StateFlow<List<Player>> = mutablePlayers

    private val _gameActive = MutableStateFlow(_defaultGameActive)
    val gameActive: StateFlow<Boolean> = _gameActive

    protected val mutableElapsedTurnTime = MutableStateFlow<Long>(0)
    val elapsedTurnTime: StateFlow<Long> = mutableElapsedTurnTime

    protected val mutableTotalElapsedTurnTime = MutableStateFlow<Long>(0)
    val totalElapsedTurnTime: StateFlow<Long> = mutableTotalElapsedTurnTime

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching

    protected val mutableTotalTurnCount = MutableStateFlow(0)
    val totalTurnCount: StateFlow<Int> = mutableTotalTurnCount

    private val _rounds = MutableStateFlow<List<Round>>(listOf())

    private var _startTime = Instant.now()
    val startTime: Instant
        get() = _startTime

    val currentRound: StateFlow<Round> = _rounds.map { it.lastOrNull() ?: Round() }.stateIn(
        scope = scope, started = SharingStarted.Eagerly, initialValue = Round()
    )

    val currentRoundNumber: StateFlow<Int> = _rounds.map { it.size }.stateIn(
        scope = scope, started = SharingStarted.Eagerly, initialValue = 0
    )

    protected var activeTimers: MutableList<CountDownTimer?> = mutableListOf()

    protected var needsRestart: Boolean = true


    fun connectToDevice(gameDevice: GameDevice) {
        if (gameDevice.connecting.value || gameDevice.connected.value) {
            return
        }
        scope.launch {
            gameDevice.onServicesDiscoveredCallback = { onDeviceServicesDiscovered() }
            if (gameDevice.connectToDevice()) {
                addConnectedDevice(gameDevice)
                addPlayer(
                    player = Player(
                        name = gameDevice.name, device = gameDevice
                    )
                )
            }
        }
    }

    // MARK: Timer Maintenance
    protected fun pauseTimers() {
        activeTimers.forEach { it?.pause() }
    }

    protected fun stopTimers() {
        activeTimers.forEach { it?.cancel() }
    }

    protected fun resumeTimers() {
        activeTimers.forEach { it?.start() }
    }

    protected fun startTimers(onComplete: (() -> Unit)? = null) {
        activeTimers.forEach { it?.start(onComplete) }
    }

    protected fun clearTimers() {
        stopTimers()
        activeTimers.clear()
    }

    // MARK: Functions

    suspend fun fetchGameDevices(): List<GameDevice> {
        return bluetoothDatasource.fetchGameDevices()
    }

    fun fetchConnectedDevices(): List<GameDevice> {
        return localGameDatasource.fetchConnectedDevices()
    }

    suspend fun removeConnectedDevice(gameDevice: GameDevice) {
        localGameDatasource.removeConnectedDevice(gameDevice)
    }

    private suspend fun addConnectedDevice(gameDevice: GameDevice) {
        localGameDatasource.addConnectedDevice(gameDevice)
    }

    fun addLocalDevice() {
        if (fetchNumberOfLocalDevices() >= 4) {
            return
        }
        localGameDatasource.addLocalDevice()
        _numberOfLocalDevices.value = fetchNumberOfLocalDevices()
        updatePlayersList()
        return
    }

    fun removeLocalDevice() {
        if (fetchNumberOfLocalDevices() <= 0) {
            return
        }
        localGameDatasource.removeLocalDevice()
        _numberOfLocalDevices.value = fetchNumberOfLocalDevices()
        updatePlayersList()
        return
    }

    suspend fun fetchConnectedBLEDevices(): List<GameDevice> {
        return bluetoothDatasource.fetchConnectedDevices()
    }

    private fun fetchNumberOfLocalDevices(): Int {
        return localGameDatasource.fetchNumberOfLocalDevices()
    }

    protected open fun setDeviceCallbacks(player: Player) {
        player.setDeviceOnSkipCallback { playerValue: Player, newValue: Boolean ->
            onPlayerSkippedChange(playerValue, newValue)
        }
        player.setDeviceOnDisconnectCallback { onPlayerConnectionDisconnect(player) }
    }

    fun startGame() {
        bluetoothDatasource.stopDeviceSearch()
        _gameActive.value = true
        _startTime = Instant.now()
        mutablePlayers.value = getPlayers()

        for (player in players.value) {
            setDeviceCallbacks(player)
        }

        updateDevicesTotalPlayers()
        updateDevicesPlayerOrder()
        updateDevicesTurnTimer()
        updateDevicesTurnTimeEnabled()
    }

    fun endGame() {
        // TODO: Implement the end of the game
        // Disconnect from all devices
        // Remove all players
        // Reset all values

        scope.launch {
            for (player in players.value) {
                player.device.disconnectFromDevice()
            }
        }
        localGameDatasource.resetDatasource()
        bluetoothDatasource.resetDatasource()
        stopBLESearch()
    }

    fun quitGame() {
        scope.launch {
            for (player in players.value) {
                player.device.disconnectFromDevice()
            }
        }
        localGameDatasource.resetDatasource()
        bluetoothDatasource.resetDatasource()
        stopBLESearch()
        needsRestart = true
    }

    protected open fun startTurn() {
        currentRound.value.incrementTotalTurns()
        mutableTotalTurnCount.value += 1
    }

    private fun getPlayers(): List<Player> {
        return localGameDatasource.fetchPlayers()
    }

    fun updatePlayersList() {
        mutablePlayers.value = getPlayers().toMutableList()
        updateDevicesTotalPlayers()
        updateDevicesPlayerOrder()
    }

    fun addPlayer(player: Player) {
        if (players.value.contains(player)) {
            Log.d(TAG, "Player $player is already in the game")
            return
        }
        localGameDatasource.addPlayer(player)
        updatePlayersList()
    }

    fun playerWithDevice(gameDevice: GameDevice): Player? {
        if (players.value.any { it.device == gameDevice }) {
            return players.value.first { it.device == gameDevice }
        }
        return null
    }

    private fun updateTurnTime() {
        _timerDuration.value = localGameDatasource.fetchTurnTime()
        updateDevicesTurnTimer()
    }

    fun setTurnTime(duration: Long) {
        localGameDatasource.setTurnTime(duration)
        updateTurnTime()
    }

    fun setTurnTimerEnforced() {
        _enforceTimer.value = true
        updateDevicesTurnTimeEnabled()
    }

    fun setTurnTimerNotEnforced() {
        _enforceTimer.value = false
        updateDevicesTurnTimeEnabled()
    }

    private fun updateTotalTurnTime() {
        _totalTimerDuration.value = localGameDatasource.fetchTotalTurnTime()
    }

    fun setTotalTurnTime(duration: Long) {
        localGameDatasource.setTotalTurnTime(duration)
        updateTotalTurnTime()
    }

    open fun pauseGame() {
        _isPaused.value = true
        pauseTimers()
        updatePlayersState()
    }

    open fun resumeGame() {
        _isPaused.value = false
        resumeTimers()

        updatePlayersState()
        if (needsRestart) {
            // If the game needs a restart, we consider that a new round
            Log.d(TAG, "resumeGame: Restarting round")
            startRound()

            needsRestart = false
        }
    }

    open fun setSkippedPlayer(player: Player) {
        localGameDatasource.setSkippedPlayer(player)
        updateSkippedPlayers()
        updatePlayerDevice(player)
        updatePlayersState()
    }

    fun setUnskippedPlayer(player: Player) {
        if (player.connected.value) {
            localGameDatasource.setUnskippedPlayer(player)
            updateSkippedPlayers()
        }
        updatePlayerDevice(player)
        updatePlayersState()
    }

    private fun updateSkippedPlayers() {
        mutableSkippedPlayers.value = localGameDatasource.fetchSkippedPlayers().toMutableSet()
    }

    protected fun checkAllSkipped(): Boolean {
        if (skippedPlayers.value.size == players.value.size) {
            pauseGame()
            players.value.forEach { player ->
                setUnskippedPlayer(player)
            }
            needsRestart = true
            return true
        }
        return false
    }



    abstract fun resolvePlayerDeviceState(player: Player): DeviceState

    /// Update the device with the current resolved device state
    abstract fun updatePlayerDevice(player: Player)

    protected fun updatePlayersState() {
        players.value.forEach {
            updatePlayerDevice(it)
        }
    }



    /// Update the device with all information necessary to display the AwaitingGameStart display
    protected fun updatePlayerDeviceCount(player: Player) {
        player.device.writeNumberOfPlayers(numberOfPlayers)
    }

    // Callbacks provided to the devices so that they can alert the repo when changes from the
    // peripheral devices are received
    // This allows the device to initiate Game Flow processing when the changes happen
    private fun onPlayerConnectionDisconnect(player: Player) {
        setSkippedPlayer(player)
        updateDevicesTotalPlayers()
        updateDevicesPlayerOrder()
        bluetoothDatasource.reconnectDevice(mac = player.device.address,
            deviceFoundCallback = { device ->
                player.device = device
                onPlayerConnectionReconnect(player)
            })
    }

    private fun onDeviceServicesDiscovered() {
        updateDevicesTotalPlayers()
        updateDevicesPlayerOrder()
    }

    private fun onPlayerConnectionReconnect(player: Player) {
        scope.launch {
            player.device.onServicesRediscoveredCallback = { onPlayerServicesRediscovered(player) }
            player.device.connectToDevice()
        }
    }

    private fun onPlayerServicesRediscovered(player: Player) {
        player.connected = player.device.connected
        setDeviceCallbacks(player)
        setUnskippedPlayer(player)
        updateDevicesTotalPlayers()
        updateDevicesPlayerOrder()
        updatePlayerState(player)
    }

    private fun onPlayerSkippedChange(player: Player, skippedValue: Boolean) {
        // BLE Notifications are fired from the peripheral device by performing a write of 1 followed
        // by a write of 0. Only the write of 1 will be used to initiate state change
        if (skippedValue) {
            if (skippedPlayers.value.contains(player)) {
                setUnskippedPlayer(player)
            } else {
                setSkippedPlayer(player)
            }
        }
    }

    /// Update the device with all information necessary to display the EnforcedTurn display
    protected open fun updatePlayerTimeData(player: Player) {
        player.device.writeElapsedTime(timerDuration.value - elapsedTurnTime.value)
    }

    private fun updateDevicesTurnTimer() {
        players.value.forEach { player ->
            player.device.writeTimer(timerDuration.value)
        }
    }

    private fun updateDevicesTurnTimeEnabled() {
        players.value.forEach { player ->
            player.device.writeTurnTimerEnforced(enforceTimer.value)
        }
    }

    private fun updateDevicesTotalPlayers() {
        players.value.forEach { player ->
            player.device.writeNumberOfPlayers(numberOfPlayers)
        }
    }

    protected val numberOfPlayers: Int
        get() {
            if (gameActive.value) {
                return players.value.size
            } else {
                // This will get the number of local players that have been added so they arent counted twice
                val numLocalPlayers = players.value.count { player ->
                    player.device::class == LocalDevice::class
                }
                return players.value.size + fetchNumberOfLocalDevices() - numLocalPlayers
            }
        }

    protected val encodedSkippedPlayers: Int
        get() {
            var skippedPlayersValue = 0
            players.value.forEachIndexed { index, player ->
                if (skippedPlayers.value.contains(player)) {
                    skippedPlayersValue = skippedPlayersValue or (1 shl index)
                }
            }
            Log.d(TAG, "Encoded skipped players: $skippedPlayersValue")
            return skippedPlayersValue
        }

    private fun updateDevicesPlayerOrder() {
        players.value.forEachIndexed { i, player ->
            player.device.writePlayerIndex(i)
        }
    }

    fun endRound() {
        needsRestart = true
        pauseGame()
        currentRound.value.roundEndTime = Instant.now()
        players.value.forEach { player ->
            setUnskippedPlayer(player)
        }
    }

    protected open fun startRound() {
        _rounds.value += Round()
        currentRound.value.roundStartTime = Instant.now()
        currentRound.value.setPlayerOrder(mutablePlayers.value)
        startTurn()
    }

    fun removePlayer(player: Player) {
        Log.d(TAG, "Removing player: ${player.name}")
        if (players.value.size == 1) {
            Log.d(TAG, "Cannot remove last player")
            return
        }
        localGameDatasource.removePlayer(player)
        updatePlayersList()
    }

    suspend fun startBLESearch() {
        bluetoothDatasource.startDeviceSearch()
        _isSearching.value = true

    }

    fun stopBLESearch() {
        _isSearching.value = false
        bluetoothDatasource.stopDeviceSearch()
    }

    protected open fun updatePlayerState(player: Player) {
        player.device.writeNumberOfPlayers(players.value.size)
        player.device.writePlayerIndex(players.value.indexOf(player))
        player.device.writeTurnTimerEnforced(enforceTimer.value)
        player.device.writeTimer(timerDuration.value)
        updatePlayerDevice(player)
    }

    companion object {
        const val TAG = "GameRepository"
    }
}