package com.etds.hourglass.data.game

import android.util.Log
import com.etds.hourglass.data.BLEData.remote.BLERemoteDatasource
import com.etds.hourglass.data.game.local.LocalDatasource
import com.etds.hourglass.data.game.local.LocalGameDatasource
import com.etds.hourglass.data.game.local.db.daos.SettingsDao
import com.etds.hourglass.data.game.local.db.entity.SettingsEntity
import com.etds.hourglass.model.Device.GameDevice
import com.etds.hourglass.model.Device.LocalDevice
import com.etds.hourglass.model.DeviceState.DeviceState
import com.etds.hourglass.model.Game.Round
import com.etds.hourglass.model.Player.Player
import com.etds.hourglass.util.CountDownTimer
import com.etds.hourglass.util.Timer
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
abstract class GameRepository (
    protected val localDatasource: LocalDatasource,
    protected val sharedGameDatasource: GameRepositoryDataStore,
    protected val localGameDatasource: LocalGameDatasource,
    protected val bluetoothDatasource: BLERemoteDatasource,
    protected val scope: CoroutineScope
) {
    private val _defaultPausedValue: Boolean = false
    private val _defaultEnforceTimer: Boolean = false
    private val _defaultTotalTimerDuration: Long = 900000
    private val _defaultTimerDuration: Long = 6000
    private val _defaultGameActive: Boolean = false

    // MARK: Preset Properties

    protected val mutableSettingPresetNames: MutableStateFlow<List<String>> = MutableStateFlow(listOf())
    val settingPresetNames: StateFlow<List<String>> = mutableSettingPresetNames

    protected val mutableDefaultSettingPresetName: MutableStateFlow<String?> = MutableStateFlow(null)
    val defaultSettingPresetName: StateFlow<String?> = mutableDefaultSettingPresetName

    // Data store player properties
    val numberOfLocalDevices: StateFlow<Int> = sharedGameDatasource.mutableNumberOfLocalDevices
    val skippedPlayers: StateFlow<Set<Player>> = sharedGameDatasource.mutableSkippedPlayers
    val players: StateFlow<List<Player>> = sharedGameDatasource.mutablePlayers


    protected val _isPaused = MutableStateFlow(_defaultPausedValue)
    val isPaused: StateFlow<Boolean> = _isPaused


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

    protected var activeTimers: MutableList<Timer?> = mutableListOf()

    protected open var needsRestart: Boolean = true


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
        sharedGameDatasource.setNumberOfLocalDevices(fetchNumberOfLocalDevices())
        updatePlayersList()
        return
    }

    fun removeLocalDevice() {
        if (fetchNumberOfLocalDevices() <= 0) {
            return
        }
        localGameDatasource.removeLocalDevice()
        sharedGameDatasource.setNumberOfLocalDevices(fetchNumberOfLocalDevices())
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

    open fun startGame() {
        bluetoothDatasource.stopDeviceSearch()
        _gameActive.value = true
        _isPaused.value = true
        _startTime = Instant.now()

        for (player in players.value) {
            setDeviceCallbacks(player)
        }

        updateDevicesTotalPlayers()
        updateDevicesPlayerOrder()
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
        return sharedGameDatasource.mutablePlayers.value
    }

    fun updatePlayersList() {
        updateDevicesTotalPlayers()
        updateDevicesPlayerOrder()
    }

    private fun updateDevicesTotalPlayers() {
        players.value.forEach { player ->
            player.device.writeNumberOfPlayers(numberOfPlayers)
        }
    }

    fun addPlayer(player: Player) {
        if (players.value.contains(player)) {
            Log.d(TAG, "Player $player is already in the game")
            return
        }
        sharedGameDatasource.addPlayer(player)
        updatePlayersList()
    }

    fun playerWithDevice(gameDevice: GameDevice): Player? {
        if (players.value.any { it.device == gameDevice }) {
            return players.value.first { it.device == gameDevice }
        }
        return null
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
        sharedGameDatasource.setSkippedPlayers(localGameDatasource.fetchSkippedPlayers())
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
        currentRound.value.setPlayerOrder(sharedGameDatasource.mutablePlayers.value)
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
        updatePlayerDevice(player)
    }

    // MARK: Preset Functions
    suspend fun initializeDatabaseSettings() {
        mutableDefaultSettingPresetName.value = getDefaultPresetName()
        refreshSettingsList()
    }


    protected abstract suspend fun getDefaultPresetName(): String?
    protected abstract suspend fun selectSettingsPreset(presetName: String)
    protected abstract suspend fun deletePreset(presetName: String)
    protected abstract suspend fun setDefaultPreset(presetName: String)
    protected abstract suspend fun saveCurrentSettings(presetName: String, makeDefault: Boolean = false)
    protected abstract suspend fun refreshSettingsList()

    // MARK: Preset Input Handlers
    suspend fun onSelectPreset(presetName: String) {
        selectSettingsPreset(presetName)
    }

    suspend fun onSavePreset(presetName: String, makeDefault: Boolean = false) {
        saveCurrentSettings(presetName, makeDefault)
    }

    suspend fun onSetDefaultPreset(presetName: String) {
        setDefaultPreset(presetName)
    }

    suspend fun onDeletePreset(presetName: String) {
        deletePreset(presetName)
    }

    // MARK: Turn Maintenance
    fun onStartTurnPress() {
        startTurn()
    }

    // MARK: Companion Object

    companion object {
        const val TAG = "GameRepository"
    }
}