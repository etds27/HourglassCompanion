package com.etds.hourglass.data.game

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import com.etds.hourglass.data.BLEData.remote.BLERemoteDatasource
import com.etds.hourglass.data.game.local.LocalDatasource
import com.etds.hourglass.data.game.local.LocalGameDatasource
import com.etds.hourglass.model.Device.DeviceConnectionState
import com.etds.hourglass.model.Device.DevicePersonalizationConfig
import com.etds.hourglass.model.Device.GameDevice
import com.etds.hourglass.model.Device.LocalDevice
import com.etds.hourglass.model.DeviceState.DeviceState
import com.etds.hourglass.model.Game.Round
import com.etds.hourglass.model.Player.Player
import com.etds.hourglass.model.config.ColorConfig
import com.etds.hourglass.util.Timer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.withTimeoutOrNull
import java.time.Instant


// @Singleton
abstract class GameRepository(
    protected val localDatasource: LocalDatasource,
    protected val sharedGameDatasource: GameRepositoryDataStore,
    protected val localGameDatasource: LocalGameDatasource,
    protected val bluetoothDatasource: BLERemoteDatasource,
    protected val scope: CoroutineScope
) {
    private val _defaultPausedValue: Boolean = false
    private val _defaultGameActive: Boolean = false

    // MARK: Preset Properties

    protected val mutableSettingPresetNames: MutableStateFlow<List<String>> =
        MutableStateFlow(listOf())
    val settingPresetNames: StateFlow<List<String>> = mutableSettingPresetNames

    protected val mutableDefaultSettingPresetName: MutableStateFlow<String?> =
        MutableStateFlow(null)
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

    val mutableTurnActive: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val turnActive: StateFlow<Boolean> = mutableTurnActive

    val currentRound: StateFlow<Round> = _rounds.map { it.lastOrNull() ?: Round(scope) }.stateIn(
        scope = scope, started = SharingStarted.Eagerly, initialValue = Round(scope)
    )

    val currentRoundNumber: StateFlow<Int> = _rounds.map { it.size }.stateIn(
        scope = scope, started = SharingStarted.Eagerly, initialValue = 0
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    val roundActive: StateFlow<Boolean> = currentRound.flatMapLatest { round ->
        round.hasStarted
    }.stateIn(
        scope = scope, started = SharingStarted.Eagerly, initialValue = false
    )


    // Timer from the very start of the game
    protected val gameTimer: Timer = Timer(scope)
    val gameDuration: StateFlow<Long> = gameTimer.timeFlow

    // Timer for only when the game is unpaused
    protected val activeGameTimer: Timer = Timer(scope)
    val activeGameDuration: StateFlow<Long> = activeGameTimer.timeFlow


    protected var pausableTimers: MutableList<Timer?> = mutableListOf()
    protected var resumableTimers: MutableList<Timer?> = mutableListOf()
    protected var clearableTimers: MutableList<Timer?> = mutableListOf()
    protected var stoppableTimers: MutableList<Timer?> = mutableListOf()
    protected var startableTimers: MutableList<Timer?> = mutableListOf()

    protected var activeTimers: MutableList<Timer?> = mutableListOf()
        set(value) {
            pausableTimers = value
            resumableTimers = value
            clearableTimers = value
            stoppableTimers = value
            startableTimers = value
        }

    protected open var needsRestart: Boolean = true


    fun connectToDevice(gameDevice: GameDevice) {
        if (gameDevice.connectionState.value == DeviceConnectionState.Connected || gameDevice.connectionState.value == DeviceConnectionState.Connecting) {
            return
        }
        scope.launch {
            gameDevice.onServicesDiscoveredCallback = {
                onDeviceServicesDiscovered()
            }
            if (gameDevice.connectToDevice()) {
                val connected = withTimeoutOrNull(5000) {
                    gameDevice.connectionState.first { it == DeviceConnectionState.Connected }
                } != null

                if (!connected) {
                    Log.d(TAG, "Failed to connect to device")
                    return@launch
                }

                Log.d(TAG, "Connected to device")
                addConnectedDevice(gameDevice)
                addPlayer(
                    player = Player(
                        name = gameDevice.name.value, device = gameDevice
                    )
                )
                val colorConfig = gameDevice.performColorConfigRetrieval(deviceState = DeviceState.DeviceColorMode)
                gameDevice.setPrimaryColor(colorConfig.colors[0])
                gameDevice.setAccentColor(colorConfig.colors[1])
            }
        }
    }

    // MARK: Timer Maintenance
    protected fun pauseTimers() {
        pausableTimers.forEach { it?.pause() }
    }

    protected fun stopTimers() {
        stoppableTimers.forEach { it?.cancel() }
    }

    protected fun resumeTimers() {
        resumableTimers.forEach { it?.start() }
    }

    protected fun startTimers(onComplete: (() -> Unit)? = null) {
        startableTimers.forEach { it?.start(onComplete) }
    }

    protected fun clearTimers() {
        stopTimers()
        clearableTimers.clear()
    }

    // MARK: Functions

    fun fetchGameDevices(): List<GameDevice> {
        return bluetoothDatasource.fetchGameDevices()
    }

    fun fetchConnectedDevices(): List<GameDevice> {
        return localGameDatasource.fetchConnectedDevices()
    }

    fun removeConnectedDevice(gameDevice: GameDevice) {
        localGameDatasource.removeConnectedDevice(gameDevice)
    }

    private fun addConnectedDevice(gameDevice: GameDevice) {
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

    fun fetchConnectedBLEDevices(): List<GameDevice> {
        return bluetoothDatasource.fetchConnectedDevices()
    }

    private fun fetchNumberOfLocalDevices(): Int {
        return localGameDatasource.fetchNumberOfLocalDevices()
    }

    private fun setDeviceCallbacks(player: Player) {
        player.setDeviceOnSkipCallback { playerValue: Player, newValue: Boolean ->
            onLeadingEdgeUserDoubleInputEvent(playerValue, newValue)
        }
        player.setDeviceOnActiveTurnCallback { playerValue: Player, newValue: Boolean ->
            onLeadingEdgeUserInputEvent(playerValue, newValue)
        }
        player.setDeviceOnDisconnectCallback { onPlayerConnectionDisconnect(player) }
    }

    // MARK: Game Flow

    /// Perform preparations for the app to transition to game settings
    open fun prepareSettingsNavigate() {
        pauseGame()
    }

    /// Set up the variables before the game has officially started.
    /// This state should be maintained until the game has been unpaused for the first time
    open fun prepareStartGame() {
        if (gameActive.value) {
            return
        }

        Log.d(TAG, "Preparing to start game")
        bluetoothDatasource.stopDeviceSearch()

        for (player in players.value) {
            setDeviceCallbacks(player)
        }
        pauseGame()

        updateDevicesTotalPlayers()
        updateDevicesPlayerOrder()

        prepareStartRound()
    }

    open fun startGame() {
        bluetoothDatasource.stopDeviceSearch()
        _gameActive.value = true
        _startTime = Instant.now()
        gameTimer.start()
        activeGameTimer.start()
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
        mutableTurnActive.value = true
    }


    open fun endTurn() {
        mutableTurnActive.value = false
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
        activeGameTimer.pause()
        currentRound.value.activeRoundTimer.pause()
        updatePlayersState()
    }

    open fun resumeGame() {

        // On the first resume of the game, we should immediately start the game
        if (!gameActive.value) {
            startGame()
        }

        // If we are between rounds, then we need to run a start round call
        if (!roundActive.value) {
            startRound()
        }

        if (!turnActive.value) {
            startTurn()
        }

        _isPaused.value = false
        resumeTimers()
        activeGameTimer.start()
        currentRound.value.activeRoundTimer.start()

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
        if (player.connectionState.value == DeviceConnectionState.Connected) {
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
        player.connectionState = player.device.connectionState
        setDeviceCallbacks(player)
        setUnskippedPlayer(player)
        updateDevicesTotalPlayers()
        updateDevicesPlayerOrder()
        updatePlayerState(player)
    }

    protected fun onUserSkippedEvent(player: Player) {
        if (skippedPlayers.value.contains(player)) {
            setUnskippedPlayer(player)
        } else {
            setSkippedPlayer(player)
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
        if (needsRestart) return // Round already ended
        needsRestart = true
        pauseGame()
        mutableTurnActive.value = false
        currentRound.value.endRound()
        players.value.forEach { player ->
            setUnskippedPlayer(player)
        }

        prepareStartRound()
    }

    /// Sets up the next round before the game is unpaused to start the round
    protected open fun prepareStartRound() {
        Log.d(TAG, "Preparing to start round")
        _rounds.value += Round(scope)
    }

    /// Starts the next round and kicks off the first turn
    protected open fun startRound() {
        Log.d(TAG, "Starting round: ${currentRoundNumber.value}")
        currentRound.value.setPlayerOrder(sharedGameDatasource.mutablePlayers.value)
        currentRound.value.startRound()
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

    suspend fun switchDeviceConfigState(device: GameDevice, state: DeviceState) {
        device.writeColorConfigState(state)
        device.readDeviceColorConfig()
    }

    fun fetchDeviceColorConfig(device: GameDevice): ColorConfig {
        return device.fetchDeviceColorConfig()
    }

    fun updateDevicePersonalizationSettings(device: GameDevice, settings: DevicePersonalizationConfig, originalSettings: DevicePersonalizationConfig) {

        // Write the name and then toggle the write bit so that the name is written into the EEPROM
        if (originalSettings.name != settings.name) {
            Log.d(TAG, "Updating device name: ${settings.name}")
            updateDeviceName(device, settings.name)
            updateDeviceNameWrite(device, write = true)
            updateDeviceNameWrite(device, write = false)
        }

        if (originalSettings.ledCount != settings.ledCount) {
            Log.d(TAG, "Updating device LED count: ${settings.ledCount}")
            updateDeviceLEDCount(device, settings.ledCount)
            updateDeviceLEDCountWrite(device, write = true)
            updateDeviceLEDCountWrite(device, write = false)
        }

        if (originalSettings.ledOffset != settings.ledOffset) {
            Log.d(TAG, "Updating device LED offset: ${settings.ledOffset}")
            updateDeviceLEDOffset(device, settings.ledOffset)
            updateDeviceLEDOffsetWrite(device, write = true)
            updateDeviceLEDOffsetWrite(device, write = false)
        }

        // Write the color config and the device state so both variables are set when writing to EEPROM
        if (originalSettings.colorConfig != settings.colorConfig) {
            Log.d(TAG, "Updating device color config: ${settings.colorConfig}")
            updateDeviceColorConfig(device, settings.colorConfig)
            // updateDeviceColorConfigState(device, settings.deviceState) We do not need to rewrite the state value
            updateDeviceColorConfigWrite(device, write = true)
            updateDeviceColorConfigWrite(device, write = false)
        }
    }

    fun updateDeviceName(device: GameDevice, name: String) {
        device.writeDeviceName(name)
    }

    fun updateDeviceNameWrite(device: GameDevice, write: Boolean) {
        device.writeDeviceNameWrite(boolean = write)
    }

    fun updateDeviceColorConfig(device: GameDevice, colorConfig: ColorConfig) {
        device.writeDeviceColorConfig(colorConfig)
    }

    fun updateDeviceLEDOffset(device: GameDevice, offset: Int) {
        device.writeLEDOffset(offset)
    }

    fun updateDeviceLEDOffsetWrite(device: GameDevice, write: Boolean) {
        device.writeLEDOffsetWrite(boolean = write)
    }

    fun updateDeviceLEDCount(device: GameDevice, count: Int) {
        device.writeLEDCount(count)
        device.readLEDOffset()
    }

    fun updateDeviceLEDCountWrite(device: GameDevice, write: Boolean) {
        device.writeLEDCountWrite(boolean = write)
        device.readLEDCount()
    }

    fun updateDeviceColorConfigWrite(device: GameDevice, write: Boolean) {
        device.writeColorConfigWrite(boolean = write)
    }

    fun updateDeviceColorConfigState(device: GameDevice, state: DeviceState) {
        device.writeColorConfigState(state)
    }

    fun startBLESearch() {
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
    protected abstract suspend fun saveCurrentSettings(
        presetName: String,
        makeDefault: Boolean = false
    )

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

    // User Input Handlers
    // These functions directly handle the specific user input provided by the player and routes
    // the function calls to the appropriate user input handler which is implemented by the
    // child class
    abstract fun onUserInputEvent(player: Player)
    private fun onLeadingEdgeUserInputEvent(player: Player, inputValue: Boolean) {
        // BLE Notifications are fired from the peripheral device by performing a write of 1 followed
        // by a write of 0. Only the write of 1 will be used to initiate state change
        if (inputValue) {
            onUserInputEvent(player)
        }
    }

    abstract fun onUserDoubleInputEvent(player: Player)
    private fun onLeadingEdgeUserDoubleInputEvent(player: Player, inputValue: Boolean) {
        // BLE Notifications are fired from the peripheral device by performing a write of 1 followed
        // by a write of 0. Only the write of 1 will be used to initiate state change
        if (inputValue) {
            onUserDoubleInputEvent(player)
        }
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
