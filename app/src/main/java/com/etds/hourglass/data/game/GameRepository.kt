package com.etds.hourglass.data.game

import android.util.Log
import com.etds.hourglass.data.BLEData.remote.BLERemoteDatasource
import com.etds.hourglass.data.game.local.LocalGameDatasource
import com.etds.hourglass.model.Device.GameDevice
import com.etds.hourglass.model.Device.LocalDevice
import com.etds.hourglass.model.DeviceState.DeviceState
import com.etds.hourglass.model.Game.Round
import com.etds.hourglass.model.Player.Player
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Duration
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.min

@Singleton
class GameRepository @Inject constructor(
    private val localGameDatasource: LocalGameDatasource,
    private val bluetoothDatasource: BLERemoteDatasource,
    private val scope: CoroutineScope
) {
    private val _defaultPausedValue: Boolean = false
    private val _defaultEnforceTimer: Boolean = false
    private val _defaultEnforceTotalTimer: Boolean = false
    private val _defaultTotalTimerDuration: Long = 900000
    private val _defaultTimerDuration: Long = 6000
    private val _defaultActivePlayerIndex: Int = 0
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

    private val _enforceTotalTimer = MutableStateFlow(_defaultEnforceTotalTimer)
    val enforceTotalTimer: StateFlow<Boolean> = _enforceTotalTimer

    private val _activePlayerIndex = MutableStateFlow(_defaultActivePlayerIndex)
    private val activePlayerIndex: StateFlow<Int> = _activePlayerIndex

    private val _activePlayer = MutableStateFlow<Player?>(null)
    val activePlayer: StateFlow<Player?> = _activePlayer

    private val _skippedPlayers = MutableStateFlow<Set<Player>>(setOf())
    val skippedPlayers: StateFlow<Set<Player>> = _skippedPlayers

    private val _players = MutableStateFlow(getPlayers())
    val players: StateFlow<List<Player>> = _players

    private val _gameActive = MutableStateFlow(_defaultGameActive)
    val gameActive: StateFlow<Boolean> = _gameActive

    private val _turnStart = MutableStateFlow(Instant.now())
    val turnStart: StateFlow<Instant> = _turnStart

    private val _elapsedTurnTime = MutableStateFlow<Long>(0)
    val elapsedTurnTime: StateFlow<Long> = _elapsedTurnTime

    private val _totalElapsedTurnTime = MutableStateFlow<Long>(0)
    val totalElapsedTurnTime: StateFlow<Long> = _totalElapsedTurnTime

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching

    private val _totalTurnCount = MutableStateFlow(0)
    val totalTurnCount: StateFlow<Int> = _totalTurnCount

    private val _rounds = MutableStateFlow<List<Round>>(listOf())

    private var _startTime = Instant.now()
    val startTime: Instant
        get() = _startTime

    val currentRound: StateFlow<Round> = _rounds.map { it.lastOrNull() ?: Round() }.stateIn(
        scope = scope,
        started = SharingStarted.Eagerly,
        initialValue = Round()
    )

    val currentRoundNumber: StateFlow<Int> = _rounds.map { it.size }.stateIn(
        scope = scope,
        started = SharingStarted.Eagerly,
        initialValue = 0
    )

    private var needsRestart: Boolean = true


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
                        name = gameDevice.name,
                        device = gameDevice
                    )
                )
            }
        }
    }

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

    fun setDeviceCallbacks(player: Player) {
        player.setDeviceOnSkipCallback { playerValue: Player, newValue: Boolean ->
            onPlayerSkippedChange(playerValue, newValue)
        }
        player.setDeviceOnActiveTurnCallback { playerValue: Player, newValue: Boolean ->
            onPlayerActiveTurnChange(playerValue, newValue)
        }
        player.setDeviceOnDisconnectCallback { onPlayerConnectionDisconnect(player) }
    }

    fun startGame() {
        bluetoothDatasource.stopDeviceSearch()
        _gameActive.value = true
        _startTime = Instant.now()
        _players.value = getPlayers()

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

    private fun getPlayers(): List<Player> {
        return localGameDatasource.fetchPlayers()
    }

    fun updatePlayersList() {
        _players.value = getPlayers().toMutableList()
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

    fun setTotalTurnTimerEnforced() {
        _enforceTotalTimer.value = true
    }

    fun setTotalTurnTimerNotEnforced() {
        _enforceTotalTimer.value = false
    }

    fun pauseGame() {
        _isPaused.value = true
        _activePlayer.value = null
        updatePlayersState()
    }

    fun resumeGame() {
        _isPaused.value = false
        Log.d(TAG, "resumeGame: Resuming game with player: ${activePlayerIndex.value}")
        // updateActivePlayer()
        // The active player is manually set here so that no BT updates are sent out when unpausing

        _activePlayer.value = _players.value[_activePlayerIndex.value]
        updatePlayersState()
        if (needsRestart) {
            // If the game needs a restart, we consider that a new round
            Log.d(TAG, "resumeGame: Restarting round")
            startRound()
            setActivePlayerIndex(0)
            startTurn()
            needsRestart = false
        }
    }

    fun setSkippedPlayer(player: Player) {
        localGameDatasource.setSkippedPlayer(player)
        updateSkippedPlayers()
        updatePlayerDevice(player)
        updatePlayersState()

        Log.d(TAG, "Skipped Player: ${player.name}, Active Player: ${activePlayer.value!!.name}")
        if (player == activePlayer.value) {
            Log.d(TAG, "Advancing to next player")
            nextPlayer()
        }
        checkAllSkipped()
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
        _skippedPlayers.value = localGameDatasource.fetchSkippedPlayers().toMutableSet()
    }

    private fun getActivePlayer(): Player {
        return _players.value[activePlayerIndex.value]
    }

    private fun setActivePlayerIndex(index: Int) {
        // Reset previous player
        _activePlayer.value?.let {
            _activePlayer.value!!.device.writeElapsedTime(0L)
        }

        val previousPlayer = _activePlayer.value
        // Update to next player
        _activePlayerIndex.value = index
        _activePlayer.value = players.value[index]

        // First update the previous active player. This will provide the most responsive feedback
        // for the most recent action
        previousPlayer?.let {
            updatePlayerDevice(previousPlayer)
        }

        // Then update the new active player to reduce dead time between turns
        _activePlayer.value?.let {
            updatePlayerDevice(it)
        }

        // Then update the turn sequences for all players
        players.value.forEach {
            it.device.writeCurrentPlayer(activePlayerIndex.value)
        }
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
            needsRestart = true
            return true
        }
        return false
    }

    fun nextPlayer() {
        if (checkAllSkipped()) {
            return
        }
        // Prevent the turn from being skipped if there is only one person left
        if (skippedPlayers.value.size + 1 == players.value.size) {
            if (!skippedPlayers.value.contains(activePlayer.value)) {
                return
            }
        }

        // Find the next non-skipped index
        for (i in 1..<players.value.size + 1) {
            val index = (activePlayerIndex.value + i) % players.value.size
            if (!skippedPlayers.value.contains(players.value[index])) {
                setActivePlayerIndex(index)
                break
            }
        }

        activePlayer.value?.let {
            updatePlayerState(player = activePlayer.value!!)
        }
        startTurn()
    }

    fun previousPlayer() {
        if (checkAllSkipped()) {
            return
        }
        val index = (activePlayerIndex.value - 1 + players.value.size) % players.value.size
        setActivePlayerIndex(index)
        if (skippedPlayers.value.contains(activePlayer.value)) {
            previousPlayer()
        }
        startTurn()
    }

    fun reorderPlayers(from: Int, to: Int) {
        if (0 <= from && from < players.value.size && 0 <= to && to < players.value.size) {
            localGameDatasource.movePlayer(from, to)
            updatePlayersList()
        }
        endRound()
    }

    fun shiftPlayerOrderForward() {
        localGameDatasource.shiftPlayerOrderForward()
        updatePlayersList()
        endRound()
    }

    fun shiftPlayerOrderBackward() {
        localGameDatasource.shiftPlayerOrderBackward()
        updatePlayersList()
        endRound()
    }

    private suspend fun runTimer(
        startingTime: Long = 0L,
        startingPlayer: Player,
        elapsedTimeStateFlow: MutableStateFlow<Long>,
        enforceTimer: Boolean,
        updateTimerCallback: (() -> Unit)? = null,
        updateTimerInterval: Int? = 100,
        timerMaxLength: Long
    ): Long {
        var lastUpdate = Instant.now()
        Log.d(
            TAG,
            "Starting Timer: Elapsed: ${elapsedTimeStateFlow.value}, duration: $timerMaxLength"
        )
        var timerElapsedTime = startingTime
        var lastDeviceUpdate = lastUpdate
        elapsedTimeStateFlow.value =
            if (enforceTimer) timerMaxLength - timerElapsedTime else timerElapsedTime
        while (true) {
            delay(25L)
            if (needsRestart) {
                break
            }
            if (isPaused.value) {
                lastUpdate = Instant.now()
                continue
            }
            if (timerElapsedTime >= timerMaxLength && enforceTimer) {
                Log.d(TAG, "Time limit reached")
                break
            }

            if (startingPlayer != activePlayer.value) {
                Log.d(
                    TAG,
                    "Active player changed from ${startingPlayer.name} to ${activePlayer.value!!.name}"
                )
                break
            }


            elapsedTimeStateFlow.value =
                if (enforceTimer) timerMaxLength - timerElapsedTime else timerElapsedTime


            val now = Instant.now()

            updateTimerCallback?.let {
                updateTimerInterval?.let {
                    if (Duration.between(lastDeviceUpdate, now).toMillis() > updateTimerInterval) {
                        updateTimerCallback()
                        lastDeviceUpdate = now
                    }
                }
            }

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
                updateTimerCallback = {
                    activePlayer.value?.let {
                        updateDeviceElapsedTime()
                    }
                },
                updateTimerInterval = 250,
                enforceTimer = enforceTimer.value
            )

            // Skip to the next player if the turn timer was reached and then enforce timer was set
            if (activePlayer.value == startingPlayer &&
                timerElapsedTime >= timerDuration.value &&
                enforceTimer.value
            ) {
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
                updateTimerInterval = 250,
                enforceTimer = enforceTotalTimer.value
            )

            // Skip to the next player if the turn timer was reached and then enforce timer was set
            if (activePlayer.value == startingPlayer &&
                timerElapsedTime >= totalTimerDuration.value &&
                enforceTimer.value
            ) {
                Log.d(TAG, "Total timer duration reached, advancing to next player")
                nextPlayer()
            }

            // Add the elapsed turn time to the starting players total time
            Log.d(
                TAG,
                "Adding elapsed time to starting player: ${startingPlayer.name} ${startingPlayer.totalTurnTime} -> $timerElapsedTime"
            )
            startingPlayer.totalTurnTime = timerElapsedTime
        }
    }

    /// Determine the expected Player Device state based on the current game information
    ///
    /// The following decision tree is followed to determine the expected state of the device:
    /// 1. Check if the game hasn't started yet: AwaitingGameStart
    /// 2. Check if the game is paused: Paused
    /// 3. Check if the current player is the active player
    /// 3.1 Determine if turn timer is enforced: ActiveTurnEnforced
    /// 3.2 Determine if turn timer is not enforced: ActiveTurnNotEnforced
    /// 4. Check if the player is skipped: Skipped
    /// 5. Otherwise: AwaitingTurn
    private fun resolvePlayerDeviceState(player: Player): DeviceState {
        if (!gameActive.value) {
            return DeviceState.AwaitingGameStart
        }

        if (isPaused.value) {
            return DeviceState.Paused
        }

        if (player == activePlayer.value) {
            return if (enforceTimer.value) {
                DeviceState.ActiveTurnEnforced
            } else {
                DeviceState.ActiveTurnNotEnforced
            }
        }

        if (skippedPlayers.value.contains(player)) {
            return DeviceState.Skipped
        }

        return DeviceState.AwaitingTurn
    }


    /// Update the device with the current resolved device state
    private fun updatePlayerDevice(player: Player) {
        val deviceState = resolvePlayerDeviceState(player)

        // Ensure data is updated when updating to a new state that requires supplemental data
        if (deviceState == DeviceState.AwaitingTurn) {
            updatePlayerTurnSequence(player)
        } else if (deviceState == DeviceState.ActiveTurnEnforced) {
            updatePlayerDeviceCount(player)
        } else if (deviceState == DeviceState.AwaitingGameStart) {
            updatePlayerTimeData(player)
        }

        // Only update the device state if it differs from the current device state
        if (deviceState != player.device.getDeviceState()) {
            player.device.setDeviceState(deviceState)
        }
    }

    private fun updatePlayersState() {
        players.value.forEach {
            updatePlayerDevice(it)
        }
    }

    /// Update the device with all information necessary to display the AwaitingTurn display
    private fun updatePlayerTurnSequence(player: Player) {
        player.device.writeNumberOfPlayers(numberOfPlayers)
        player.device.writeCurrentPlayer(activePlayerIndex.value)
        player.device.writePlayerIndex(players.value.indexOf(player))
        player.device.writeSkippedPlayers(encodedSkippedPlayers)
    }

    /// Update the device with all information necessary to display the AwaitingGameStart display
    private fun updatePlayerDeviceCount(player: Player) {
        player.device.writeNumberOfPlayers(numberOfPlayers)
    }

    /// Update the device with all information necessary to display the EnforcedTurn display
    private fun updatePlayerTimeData(player: Player) {
        player.device.writeElapsedTime(timerDuration.value - elapsedTurnTime.value)
    }

    private fun startTurn() {
        scope.launch {
            val startingPlayer = activePlayer.value
            startingPlayer ?: return@launch
            currentRound.value.incrementTotalTurns()
            currentRound.value.incrementPlayerTurnCounter(startingPlayer)
            _totalTurnCount.value += 1
            startingPlayer.incrementTurnCounter()
            startingPlayer.lastTurnStart = Instant.now()
            launch { startTotalTurnTimer() }
            launch { startTurnTimer() }
        }
    }

    // Callbacks provided to the devices so that they can alert the repo when changes from the
    // peripheral devices are received
    // This allows the device to initiate Game Flow processing when the changes happen
    private fun onPlayerConnectionDisconnect(player: Player) {
        setSkippedPlayer(player)
        updateDevicesTotalPlayers()
        updateDevicesPlayerOrder()
        bluetoothDatasource.reconnectDevice(
            mac = player.device.address,
            deviceFoundCallback = { device ->
                player.device = device
                onPlayerConnectionReconnect(player)
            }
        )
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

    private fun onPlayerActiveTurnChange(player: Player, turnValue: Boolean) {
        // BLE Notifications are fired from the peripheral device by performing a write of 1 followed
        // by a write of 0. Only the write of 1 will be used to initiate state change
        if (turnValue) {
            if (player != activePlayer.value) {
                Log.d(TAG, "Player $player is not the active player")
                return
            }

            nextPlayer()
        }
    }

    private fun updateDevicesTurnTimer() {
        players.value.forEach { player ->
            player.device.writeTimer(timerDuration.value)
        }
    }

    private fun updateDeviceElapsedTime() {
        activePlayer.value?.device?.writeElapsedTime(timerDuration.value - elapsedTurnTime.value)
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

    private val numberOfPlayers: Int
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

    private fun startRound() {
        _rounds.value += Round()
        currentRound.value.roundStartTime = Instant.now()
        currentRound.value.setPlayerOrder(_players.value)
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

    private fun updatePlayerState(player: Player) {
        player.device.writeNumberOfPlayers(players.value.size)
        player.device.writePlayerIndex(players.value.indexOf(player))
        player.device.writeCurrentPlayer(activePlayerIndex.value)
        player.device.writeTurnTimerEnforced(enforceTimer.value)
        player.device.writeTimer(timerDuration.value)
        updatePlayerDevice(player)
    }

    companion object {
        const val TAG = "GameRepository"
    }
}