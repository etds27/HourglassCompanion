package com.etds.hourglass.data.game

import android.util.Log
import com.etds.hourglass.data.BLEData.remote.BLERemoteDatasource
import com.etds.hourglass.data.game.local.LocalDatasource
import com.etds.hourglass.data.game.local.LocalGameDatasource
import com.etds.hourglass.data.game.local.db.daos.SettingsDao
import com.etds.hourglass.data.game.local.db.entity.SequentialSettingsEntity
import com.etds.hourglass.data.game.local.db.entity.SettingsEntity
import com.etds.hourglass.model.DeviceState.DeviceState
import com.etds.hourglass.model.Player.Player
import com.etds.hourglass.util.CountDownTimer
import com.etds.hourglass.util.Timer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SequentialGameRepository @Inject constructor(
    localGameDatasource: LocalGameDatasource,
    bluetoothDatasource: BLERemoteDatasource,
    localDatasource: LocalDatasource,
    sharedGameDatasource: GameRepositoryDataStore,
    scope: CoroutineScope,
) : GameRepository(
    localGameDatasource = localGameDatasource,
    bluetoothDatasource = bluetoothDatasource,
    localDatasource = localDatasource,
    sharedGameDatasource = sharedGameDatasource,
    scope = scope
) {

    init {
        Log.d(TAG, "Initializing Sequential Game Repository")
        Log.d(TAG, "Local Game Datasource: $localGameDatasource")
    }

    var settingsDao: SettingsDao<SequentialSettingsEntity> = localDatasource.sequentialSettingsDao
    protected var settingPresets: MutableList<SequentialSettingsEntity> = mutableListOf()

    private val _defaultActivePlayerIndex: Int = 0
    private val _defaultEnforceTotalTimer: Boolean = false

    private val _activePlayerIndex = MutableStateFlow(_defaultActivePlayerIndex)
    private val activePlayerIndex: StateFlow<Int> = _activePlayerIndex

    private val _activePlayer = MutableStateFlow<Player?>(null)
    val activePlayer: StateFlow<Player?> = _activePlayer


    // MARK: Timer Properties
    /// Timer to track the current user's turn time
    private val mutableTurnTimer = MutableStateFlow<CountDownTimer?>(null)
    val turnTimer: StateFlow<CountDownTimer?> = mutableTurnTimer

    private val mutableOpenTurnTimer = MutableStateFlow<Timer?>(null)
    val openTurnTimer: StateFlow<Timer?> = mutableOpenTurnTimer

    private val mutableTurnTimerDuration = MutableStateFlow(60000L)

    /// Duration of the turn timer
    val turnTimerDuration: StateFlow<Long> = mutableTurnTimerDuration

    /// Timer to track the current user's total turn time
    private val mutableTotalTurnTimer = MutableStateFlow<CountDownTimer?>(null)
    val totalTurnTimer: StateFlow<CountDownTimer?> = mutableTotalTurnTimer

    private val mutableOpenTotalTurnTimer = MutableStateFlow<Timer?>(null)
    val openTotalTurnTimer: StateFlow<Timer?> = mutableOpenTotalTurnTimer

    private val mutableAutoStartTurnTimer = MutableStateFlow(false)

    /// Property to indicate if the Turn Timer should automatically start on turn start
    val autoStartTurnTimer: StateFlow<Boolean> = mutableAutoStartTurnTimer

    private val mutableEnforceTurnTimer = MutableStateFlow(false)

    /// Property to indicate if the Turn Timer is currently active/enforced
    val enforceTimer: StateFlow<Boolean> = mutableEnforceTurnTimer

    private val mutableAutoStartTotalTimer = MutableStateFlow(false)

    /// Property to indicate if the Total Turn Timer should automatically start on game start
    val autoStartTotalTimer: StateFlow<Boolean> = mutableAutoStartTotalTimer

    private val mutableEnforceTotalTimer = MutableStateFlow(_defaultEnforceTotalTimer)

    /// Property to indicate if the Total Turn Timer is currently active/enforced
    val enforceTotalTimer: StateFlow<Boolean> = mutableEnforceTotalTimer

    private val mutableTotalTurnTimerDuration = MutableStateFlow(60000L)

    /// Duration of the total turn timer
    val totalTurnTimerDuration: StateFlow<Long> = mutableTotalTurnTimerDuration


    override fun setDeviceCallbacks(player: Player) {
        player.setDeviceOnActiveTurnCallback { playerValue: Player, newValue: Boolean ->
            onPlayerActiveTurnChange(playerValue, newValue)
        }
        super.setDeviceCallbacks(player)
    }

    override fun startGame() {
        super.startGame()
        updateDevicesTurnTimeEnabled()
        updateDevicesTurnTimer()
        startTurn()
    }

    override fun pauseGame() {
        _activePlayer.value = null
        super.pauseGame()
    }

    override fun resumeGame() {
        Log.d(TAG, "resumeGame: Resuming game with player: ${activePlayerIndex.value}")
        // updateActivePlayer()
        // The active player is manually set here so that no BT updates are sent out when unpausing
        _activePlayer.value = sharedGameDatasource.mutablePlayers.value[_activePlayerIndex.value]

        if (needsRestart) {
            setActivePlayerIndex(0)
        }

        super.resumeGame()
    }

    override fun setSkippedPlayer(player: Player) {
        super.setSkippedPlayer(player)

        Log.d(TAG, "Skipped Player: ${player.name}, Active Player: ${activePlayer.value!!.name}")
        if (player == activePlayer.value) {
            Log.d(TAG, "Advancing to next player")
            nextPlayer()
        }
        checkAllSkipped()
    }

    // MARK: Turn Sequencing / Active Player Handling
    private fun getActivePlayer(): Player {
        return sharedGameDatasource.mutablePlayers.value[activePlayerIndex.value]
    }

    private fun setActivePlayerIndex(index: Int) {
        // Reset previous player
        _activePlayer.value?.let {
            updateActivePlayerTotalTimeFromTimer()

        }

        val previousPlayer = _activePlayer.value
        // Update to next player
        _activePlayerIndex.value = index
        _activePlayer.value = players.value[index]

        // First update the previous active player. This will provide the most responsive feedback
        // for the most recent action
        previousPlayer?.let {
            updatePlayerDevice(previousPlayer)
            previousPlayer.device.writeElapsedTime(0L)
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

    // MARK: Reordering Players
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

    private fun startTurnTimer() {
        mutableEnforceTurnTimer.value = true

        turnTimer.value?.start(
            onComplete = {
                Log.d(TAG, "Timer duration reached, advancing to next player")
                nextPlayer()
            },
            onCountDownTimerUpdate = {
                activePlayer.value?.let {
                    updateDeviceElapsedTime()
                }
            }
        )
    }

    private fun startTotalTurnTimer() {
        mutableEnforceTotalTimer.value = true

        totalTurnTimer.value?.start(
            onComplete = {
                Log.d(TAG, "Total Timer duration reached, advancing to next player")
                nextPlayer()
            }
        )
    }

    // MARK: Player State

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
    override fun resolvePlayerDeviceState(player: Player): DeviceState {
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
    override fun updatePlayerDevice(player: Player) {
        val deviceState = resolvePlayerDeviceState(player)

        // Ensure data is updated when updating to a new state that requires supplemental data
        when (deviceState) {
            DeviceState.AwaitingTurn -> {
                updatePlayerTurnSequence(player)
            }

            DeviceState.ActiveTurnEnforced -> {
                updatePlayerTimeData(player)
            }

            DeviceState.AwaitingGameStart -> {
                updatePlayerDeviceCount(player)
            }

            else -> {}
        }

        // Only update the device state if it differs from the current device state
        if (deviceState != player.device.getDeviceState()) {
            player.device.setDeviceState(deviceState)
        }
    }

    /// Update the device with all information necessary to display the AwaitingTurn display
    private fun updatePlayerTurnSequence(player: Player) {
        player.device.writeNumberOfPlayers(numberOfPlayers)
        player.device.writeCurrentPlayer(activePlayerIndex.value)
        player.device.writePlayerIndex(players.value.indexOf(player))
        player.device.writeSkippedPlayers(encodedSkippedPlayers)
    }


    override fun startTurn() {
        super.startTurn()

        stopTimers()
        clearTimers()


        val startingPlayer = activePlayer.value
        startingPlayer ?: return

        currentRound.value.incrementPlayerTurnCounter(startingPlayer)

        mutableOpenTurnTimer.value = Timer(
            scope = scope,
            startTime = 0L
        )

        mutableTurnTimer.value =
            CountDownTimer.fromStartingTime(
                scope,
                duration = turnTimerDuration.value,
                startingTime = 0,
                callbackResolution = 250L
            )

        mutableOpenTotalTurnTimer.value = Timer(
            scope = scope,
            startTime = startingPlayer.openTotalTurnTime
        )

        mutableTotalTurnTimer.value =
            CountDownTimer.fromStartingTime(
                scope,
                duration = totalTurnTimerDuration.value,
                startingTime = startingPlayer.totalTurnTime
            )


        startingPlayer.incrementTurnCounter()
        startingPlayer.lastTurnStart = Instant.now()

        mutableOpenTurnTimer.value?.start()
        if (autoStartTurnTimer.value || enforceTimer.value) {
            startTurnTimer()
        }

        mutableOpenTotalTurnTimer.value?.start()
        if (autoStartTotalTimer.value || enforceTotalTimer.value) {
            startTotalTurnTimer()
        }

        activeTimers.addAll(
            listOf(
                mutableOpenTurnTimer.value,
                mutableTurnTimer.value,
                mutableOpenTotalTurnTimer.value,
                mutableTotalTurnTimer.value
            )
        )
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

    private fun updateActivePlayerTotalTimeFromTimer() {
        totalTurnTimer.value ?: return
        openTotalTurnTimer.value ?: return
        activePlayer.value ?: return
        activePlayer.value?.totalTurnTime = totalTurnTimer.value!!.timeFlow.value
        activePlayer.value?.openTotalTurnTime = openTotalTurnTimer.value!!.timeFlow.value
    }

    private fun updateDeviceElapsedTime() {
        activePlayer.value?.let { player ->
            turnTimer.value?.let { timer ->
                player.device.writeElapsedTime(timer.timeFlow.value)
            }
        }
    }

    override fun updatePlayerState(player: Player) {
        player.device.writeCurrentPlayer(activePlayerIndex.value)
        player.device.writeTurnTimerEnforced(enforceTimer.value)
        player.device.writeTimer(turnTimerDuration.value)
        super.updatePlayerState(player)
    }

    override fun startRound() {
        setActivePlayerIndex(0)
        super.startRound()
    }

    // MARK: Timer Functions
    private fun updateDevicesTurnTimeEnabled() {
        players.value.forEach { player ->
            player.device.writeTurnTimerEnforced(enforceTimer.value)
        }
    }

    /// Update the device with all information necessary to display the EnforcedTurn display
    private fun updatePlayerTimeData(player: Player) {
        turnTimer.value ?: return
        player.device.writeTimer(duration = turnTimerDuration.value)
        player.device.writeElapsedTime(turnTimer.value!!.timeFlow.value)
    }

    private fun updateDevicesTurnTimer() {
        players.value.forEach { player ->
            player.device.writeTimer(turnTimerDuration.value)
        }
    }


    // MARK: Settings Functions
    fun setAutoStartTurnTime(value: Boolean) {
        mutableAutoStartTurnTimer.value = value
    }

    fun setTurnTimerDuration(value: Number) {
        if (value.toInt() > 10_000_000) return
        if (value.toInt() < 1) return
        mutableTurnTimerDuration.value = value.toLong()
    }

    fun setTurnTimerEnforced(value: Boolean) {
        mutableEnforceTurnTimer.value = value
        if (value) {
            startTurnTimer()
        } else {
            turnTimer.value?.pause()
        }
        activePlayer.value?.let { activePlayer ->
            updatePlayerState(activePlayer)
        }
    }

    fun setAutoStartTotalTurnTimer(value: Boolean) {
        mutableAutoStartTotalTimer.value = value
    }

    fun setTotalTurnTimerDuration(value: Number) {
        if (value.toInt() > 10_000_000) return
        if (value.toInt() < 1) return
        mutableTotalTurnTimerDuration.value = value.toLong()
    }

    fun setTotalTurnTimerEnforced(value: Boolean) {
        mutableEnforceTotalTimer.value = value
        if (value) {
            startTotalTurnTimer()
        } else {
            totalTurnTimer.value?.pause()
        }
    }


    // MARK: Preset Functions
    private fun applySettingsConfig(settingsEntity: SequentialSettingsEntity) {
        mutableAutoStartTurnTimer.value = settingsEntity.autoStartTurnTimer
        mutableTurnTimerDuration.value = settingsEntity.turnTimerDuration
        mutableAutoStartTotalTimer.value = settingsEntity.autoStartTotalTurnTimer
        mutableTotalTurnTimerDuration.value = settingsEntity.totalTurnTimerDuration
    }

    private fun getCurrentSettingsEntity(): SequentialSettingsEntity {
        return SequentialSettingsEntity(
            configName = "",
            default = false,
            autoStartTurnTimer = autoStartTurnTimer.value,
            turnTimerDuration = turnTimerDuration.value,
            autoStartTotalTurnTimer = autoStartTotalTimer.value,
            totalTurnTimerDuration = totalTurnTimerDuration.value
        )
    }

    override suspend fun refreshSettingsList() {
        settingPresets = settingsDao.getAll().toMutableList()
        mutableSettingPresetNames.value = settingsDao.getAllNames()
        mutableDefaultSettingPresetName.value = getDefaultPresetName()
    }

    override suspend fun saveCurrentSettings(presetName: String, makeDefault: Boolean) {
        Log.d(GameRepository.TAG, "Saving settings: $presetName")

        val currentSettingsEntity = getCurrentSettingsEntity()
        currentSettingsEntity.configName = presetName
        currentSettingsEntity.default = makeDefault

        settingsDao.insert(
            currentSettingsEntity
        )

        if (makeDefault) {
            setDefaultPreset(presetName = presetName)
        }
        refreshSettingsList()
    }

    override suspend fun selectSettingsPreset(presetName: String) {
        val settingsEntity = settingsDao.getByName(presetName)
        applySettingsConfig(settingsEntity)
    }

    override suspend fun setDefaultPreset(presetName: String) {
        localDatasource.setDefaultSequentialPreset(presetName)
        mutableDefaultSettingPresetName.value = presetName
    }

    protected suspend fun getDefaultSettingEntity(): SettingsEntity? {
        return localDatasource.getDefaultSequentialPreset()
    }

    override suspend fun getDefaultPresetName(): String? {
        return getDefaultSettingEntity()?.configName
    }

    override suspend fun deletePreset(presetName: String) {
        settingsDao.delete(presetName)
        refreshSettingsList()
    }


    // MARK: Companion Object
    companion object {
        const val TAG = "SequentialGameRepository"
    }

}