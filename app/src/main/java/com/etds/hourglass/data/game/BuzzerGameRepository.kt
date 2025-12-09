package com.etds.hourglass.data.game

import android.util.Log
import com.etds.hourglass.data.BLEData.remote.BLERemoteDatasource
import com.etds.hourglass.data.game.local.LocalDatasource
import com.etds.hourglass.data.game.local.LocalGameDatasource
import com.etds.hourglass.data.game.local.db.daos.SettingsDao
import com.etds.hourglass.data.game.local.db.entity.BuzzerSettingsEntity
import com.etds.hourglass.data.game.local.db.entity.SettingsEntity
import com.etds.hourglass.model.Device.BLEDevice
import com.etds.hourglass.model.DeviceState.BuzzerTurnState
import com.etds.hourglass.model.DeviceState.DeviceState
import com.etds.hourglass.model.Game.buzzer_mode.BuzzerEnterTurnLoopTurnState
import com.etds.hourglass.model.Game.buzzer_mode.BuzzerTurnStartTurnState
import com.etds.hourglass.model.Game.buzzer_mode.BuzzerTurnStateData
import com.etds.hourglass.model.Player.Player
import com.etds.hourglass.util.CountDownTimer
import com.etds.hourglass.util.Timer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BuzzerGameRepository @Inject constructor(
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
        Log.d(TAG, "Initializing Buzzer Game Repository")
        Log.d(TAG, "Local Game Datasource: $localGameDatasource")
    }

    var settingsDao: SettingsDao<BuzzerSettingsEntity> = localDatasource.buzzerSettingsDao
    protected var settingPresets: MutableList<BuzzerSettingsEntity> = mutableListOf()

    // MARK: State Properties

    /// The current state of a turn for the Buzzer mode
    private val mutableTurnState: MutableStateFlow<BuzzerTurnState> =
        MutableStateFlow(BuzzerTurnState.BuzzerAwaitingTurnStart)
    val turnState: StateFlow<BuzzerTurnState> = mutableTurnState

    /// Current resolved turn state data properties that are mutated during state transitions
    private val mutableTurnStateData: MutableStateFlow<BuzzerTurnStateData> =
        MutableStateFlow(BuzzerTurnStartTurnState.getDefaultTurnStartState())
    val turnStateData: StateFlow<BuzzerTurnStateData> = mutableTurnStateData

    // MARK: Setting Properties


    /// Allows users to be able to immediately buzz when the turn loop starts
    private val mutableAllowImmediateAnswers: MutableStateFlow<Boolean> = MutableStateFlow(true)
    val allowImmediateAnswers: StateFlow<Boolean> = mutableAllowImmediateAnswers

    /// Automatically starts the buzz timer when the turn loop starts
    private val mutableAutoStartAwaitingBuzzTimer: MutableStateFlow<Boolean> =
        MutableStateFlow(false)
    val autoStartAwaitingBuzzTimer: StateFlow<Boolean> = mutableAutoStartAwaitingBuzzTimer

    /// Maximum allowed time for users to buzz while or after the question was posed
    private val mutableAwaitingBuzzTimerDuration: MutableStateFlow<Long> = MutableStateFlow(60000L)
    val awaitingBuzzTimerDuration: StateFlow<Long> = mutableAwaitingBuzzTimerDuration

    /// Determines if a timer should be enforced while or after the question is being posed
    private val mutableAwaitingBuzzTimerEnforced: MutableStateFlow<Boolean> =
        MutableStateFlow(false)
    val awaitingBuzzTimerEnforced: StateFlow<Boolean> = mutableAwaitingBuzzTimerEnforced

    private val mutableAutoStartAnswerTimer: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val autoStartAnswerTimer: StateFlow<Boolean> = mutableAutoStartAnswerTimer

    /// Determines if a timer should be enforced while the user that buzzed is answering the question
    private val mutableAnswerTimerEnforced: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val answerTimerEnforced: StateFlow<Boolean> = mutableAnswerTimerEnforced

    /// Maximum allowed time for users to answer a question
    private val mutableAnswerTimerDuration: MutableStateFlow<Long> = MutableStateFlow(60000L)
    val answerTimerDuration: StateFlow<Long> = mutableAnswerTimerDuration

    /// Determines if the game loop will go back to awaiting buzz after an incorrect answer or timeout
    /// Otherwise, the game will end the round and return to pause screen
    private val mutableAllowFollowupAnswers: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val allowFollowupAnswers: StateFlow<Boolean> = mutableAllowFollowupAnswers

    /// If `allowFollowupAnswers` is set, this will prevent a previous buzzed player from buzzing again
    private val mutableAllowMultipleAnswersFromSameUser: MutableStateFlow<Boolean> =
        MutableStateFlow(false)
    val allowMultipleAnswersFromSameUser: StateFlow<Boolean> =
        mutableAllowMultipleAnswersFromSameUser

    private val mutablePlayersAlreadyAnswered: MutableStateFlow<Set<Player>> =
        MutableStateFlow(setOf())

    // MARK: Turn Timers
    private val mutableAwaitingBuzzerTimer: MutableStateFlow<CountDownTimer?> =
        MutableStateFlow(null)
    var awaitingBuzzerTimer: StateFlow<CountDownTimer?> = mutableAwaitingBuzzerTimer

    private val mutableAnswerTimer: MutableStateFlow<CountDownTimer?> = MutableStateFlow(null)
    var answerTimer: StateFlow<CountDownTimer?> = mutableAnswerTimer

    override var needsRestart: Boolean = false
    // MARK: Functions

    // MARK: View Model Interface

    // Settings

    fun setAllowMultipleAnswersFromSameUser(value: Boolean) {
        mutableAllowMultipleAnswersFromSameUser.value = value
    }

    fun setAllowImmediateAnswers(value: Boolean) {
        mutableAllowImmediateAnswers.value = value
    }

    fun setAutoStartAwaitingBuzzTimer(value: Boolean) {
        mutableAutoStartAwaitingBuzzTimer.value = value
    }

    fun setAllowFollowupAnswers(value: Boolean) {
        mutableAllowFollowupAnswers.value = value
    }

    fun setAutoStartAnswerTimer(value: Boolean) {
        mutableAutoStartAnswerTimer.value = value
    }

    fun setEnableAnswerTimer(value: Boolean) {
        mutableAnswerTimerEnforced.value = value
        updatePlayersState()
    }

    fun setAnswerTimerDuration(value: Number) {
        if (value.toInt() > 10_000_000) return
        if (value.toInt() < 1) return
        mutableAnswerTimerDuration.value = value.toLong()
    }

    fun setEnableAwaitingBuzzTimer(value: Boolean) {
        mutableAwaitingBuzzTimerEnforced.value = value
        updatePlayersState()
    }

    fun setAwaitingBuzzTimerDuration(value: Number) {
        if (value.toInt() > 10_000_000) return
        if (value.toInt() < 1) return
        mutableAwaitingBuzzTimerDuration.value = value.toLong()
    }

    // MARK: Game Interface

    override fun startGame() {
        super.startGame()
        _isPaused.value = true
        updatePlayersState()
    }

    override fun startTurn() {
        super.startTurn()
        mutableTurnStateData.value = BuzzerTurnStartTurnState.getDefaultTurnStartState()
        enterTurnLoop()
    }

    override fun endTurn() {
        super.endTurn()
        enterAwaitingTurnStartState()
    }

    private fun enterTurnLoop() {
        // Turn sequence for Buzzer Mode:
        // Immediately, all devices enter a state where they are able to buzz and the app
        //      will accept the user input from the start
        //      Normally, the question/prompt is posed during this period
        // During the question time, the timer has not started
        // Once the question has started, the app user can choose to enforce a timer for when user's must buzz by
        //
        // The app continues to monitor for buzzer events from the peripherals
        // If a device registers a buzz event, the app immediately sets that device into BuzzerWinnerPeriod states
        // All other devices are in BuzzerResults state

        // If followup answers are allowed and the user did not complete the question,
        // the app will reenter the BuzzerAwaitingBuzz state. And can choose to reenact the timer
        // Additionally, the app can choose to disallow answers from contestants that have already answered.
        // In this case, the user who has answered will be skipped

        // Reset buzzer properties at the start of each loop
        mutableTurnStateData.value =
            BuzzerEnterTurnLoopTurnState.applyStateTo(currentState = turnStateData.value)


        clearTimers()

        if (allowImmediateAnswers.value || autoStartAwaitingBuzzTimer.value) {
            enterAwaitingBuzzState()
        } else {
            enterAwaitingBuzzerEnabledState()
        }
    }


    override fun setSkippedPlayer(player: Player) {
        super.setSkippedPlayer(player)
        checkAllSkipped()
    }

    /// Determine the expected Player Device state based on the current game information
    ///
    /// The following decision tree is followed to determine the expected state of the device:
    /// 1. Check if the game hasn't started yet: AwaitingGameStart
    /// 2. Check if the game is paused: Paused
    /// 3. Check if the player is skipped: Skipped
    /// 4.1 If answer is in progress and this is the player answering: BuzzerWinnerPeriod
    /// 4.2 If answer is in progress and this is not the player answering: BuzzerAwaitingTurnEnd
    /// 5 Check to see if a player has already answered and if multiple answers arent allowed
    /// 6. Check to see if player has buzzed
    /// 6.1 If no player has buzzed: BuzzerAwaitingBuzz
    /// 6.2 If no player has buzzed and the turn timer is enforced: BuzzerAwaitingBuzzTimed
    /// 7. Check to see if immediate answers if off and the app user is asking the question

    /// 6. Unable to resolve state: Unknown
    override fun resolvePlayerDeviceState(player: Player): DeviceState {
        if (!gameActive.value) {
            return DeviceState.AwaitingGameStart
        }

        if (isPaused.value) {
            return DeviceState.Paused
        }

        if (skippedPlayers.value.contains(player)) {
            return DeviceState.Skipped
        }

        if (turnState.value == BuzzerTurnState.BuzzerAwaitingTurnStart) {
            return DeviceState.BuzzerAwaitingTurnStart
        }

        if (turnState.value == BuzzerTurnState.BuzzerAwaitingAnswer) {
            return if (player == turnStateData.value.answerPlayer) {
                if (answerTimerEnforced.value) {
                    DeviceState.BuzzerWinnerPeriodTimed
                } else {
                    DeviceState.BuzzerWinnerPeriod
                }
            } else {
                DeviceState.BuzzerResults
            }
        }

        if (turnStateData.value.playersWhoAlreadyAnswered.contains(player) && !allowMultipleAnswersFromSameUser.value) {
            return DeviceState.BuzzerAlreadyAnswered
        }


        if (turnState.value == BuzzerTurnState.BuzzerAwaitingBuzz) {
            return if (awaitingBuzzTimerEnforced.value) {
                DeviceState.BuzzerAwaitingBuzzTimed
            } else {
                DeviceState.BuzzerAwaitingBuzz
            }
        }

        if (turnState.value == BuzzerTurnState.BuzzerAwaitingBuzzerEnabled) {
            return DeviceState.BuzzerAwaitingBuzzerEnabled
        }

        return DeviceState.Unknown
    }


    override fun updatePlayerDevice(player: Player) {
        val deviceState = resolvePlayerDeviceState(player)

        // Ensure data is updated when updating to a new state that requires supplemental data
        when (deviceState) {
            DeviceState.BuzzerAwaitingBuzzTimed -> {
                updatePlayerTimeData(player, deviceState)
            }

            DeviceState.BuzzerWinnerPeriodTimed -> {
                updatePlayerTimeData(player, deviceState)
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

    /// Update the device with all information necessary to display the EnforcedTurn display
    private fun updatePlayerTimeData(player: Player, deviceState: DeviceState) {
        val timer: Timer?
        val timerDuration: Long

        when (deviceState) {
            DeviceState.BuzzerAwaitingBuzzTimed -> {
                timer = awaitingBuzzerTimer.value
                timerDuration = awaitingBuzzTimerDuration.value
            }

            DeviceState.BuzzerWinnerPeriodTimed -> {
                timer = answerTimer.value
                timerDuration = answerTimerDuration.value
            }

            else -> return
        }

        timer?.let {
            player.device.writeTimer(duration = timerDuration)
            player.device.writeElapsedTime(it.timeFlow.value)
        }
    }

    private fun updatePlayersTimeData() {
        players.value.forEach {
            updatePlayerTimeData(it, it.device.getDeviceState())
        }
    }


    // MARK: Input Event Handlers
    override fun onUserInputEvent(player: Player) {
        if (userCanBuzz(player)) {
            enterAwaitingAnswerState(player = player)
            updatePlayersState()
        }
    }

    override fun onUserDoubleInputEvent(player: Player) {
        onUserSkippedEvent(player)
    }

    private fun userCanBuzz(player: Player): Boolean {
        if (turnStateData.value.awaitingBuzz && !isPaused.value) {
            // Ignore buzzes from user's who have already answered when the multi answer setting is off
            if (allowMultipleAnswersFromSameUser.value || !turnStateData.value.playersWhoAlreadyAnswered.contains(
                    player
                )
            ) {
                if (!skippedPlayers.value.contains(player)) {
                    return true
                }
            }
        }
        return false
    }


    // Game Sequencing
    private fun enterAwaitingTurnStartState() {
        transitionTurnStateTo(BuzzerTurnState.BuzzerAwaitingTurnStart)
        pauseTimers()
        clearTimers()
        resumeGame()
        updatePlayersState()
    }

    private fun enterAwaitingAnswerState(player: Player) {
        transitionTurnStateTo(BuzzerTurnState.BuzzerAwaitingAnswer, player = player)
        Log.d(TAG, "Player ${player.name} buzzed first")

        val timer = CountDownTimer(
            scope,
            duration = answerTimerDuration.value,
            resolution = 10L,
            callbackResolution = BLEDevice.defaultBLERequestDelay,
        )
        mutableAnswerTimer.value = timer
        activeTimers.add(answerTimer.value)

        pauseTimers()
        if (autoStartAnswerTimer.value) {
            startAwaitingAnswerTimer()
        } else {
            mutableAnswerTimerEnforced.value = false
        }

        updatePlayerState(player)
        updatePlayersState()
    }

    /// Perform idle actions while waiting for a user peripheral to send a buzz event
    private fun enterAwaitingBuzzState() {
        transitionTurnStateTo(BuzzerTurnState.BuzzerAwaitingBuzz)

        val timer = CountDownTimer(
            scope,
            duration = awaitingBuzzTimerDuration.value,
            resolution = 10L,
            callbackResolution = BLEDevice.defaultBLERequestDelay,
        )
        mutableAwaitingBuzzerTimer.value = timer
        activeTimers.add(awaitingBuzzerTimer.value)



        mutableAnswerTimerEnforced.value = false
        pauseTimers()
        if (autoStartAwaitingBuzzTimer.value) {
            startAwaitingBuzzTimer()
        } else {
            mutableAwaitingBuzzTimerEnforced.value = false
        }
        updatePlayersState()
    }

    private fun enterAwaitingBuzzerEnabledState() {
        mutableAwaitingBuzzTimerEnforced.value = false
        pauseTimers()
        transitionTurnStateTo(BuzzerTurnState.BuzzerAwaitingBuzzerEnabled)
    }

    /// Change the current turn state to the new value and apply the state transition to the current turn properties
    private fun transitionTurnStateTo(newState: BuzzerTurnState) {
        mutableTurnState.value = newState
        mutableTurnStateData.value = newState.getConfig().applyStateTo(turnStateData.value)
        updatePlayersState()
    }

    /// Change the current turn state to the new value and apply the state transition to the current turn properties
    private fun transitionTurnStateTo(newState: BuzzerTurnState, player: Player) {
        mutableTurnState.value = newState
        mutableTurnStateData.value =
            newState.getConfigForPlayer(player = player).applyStateTo(turnStateData.value)
        updatePlayersState()
    }

    // Event Processing
    fun onEnableBuzzersPress() {
        enterAwaitingBuzzState()
    }

    fun onDisableBuzzersPress() {
        enterAwaitingBuzzerEnabledState()
    }

    private fun startAwaitingBuzzTimer() {
        if (awaitingBuzzerTimer.value == null) {
            mutableAwaitingBuzzerTimer.value =
                CountDownTimer(scope, duration = awaitingBuzzTimerDuration.value)
        }
        mutableAwaitingBuzzTimerEnforced.value = true

        awaitingBuzzerTimer.value?.start(
            onComplete = {
                if (allowFollowupAnswers.value) {
                    enterTurnLoop()
                } else {
                    enterAwaitingTurnStartState()
                }
            },
            onCountDownTimerUpdate = {
                updatePlayersTimeData()
            }
        )
        updatePlayersState()
    }

    private fun pauseAwaitingBuzzTimer() {
        mutableAwaitingBuzzTimerEnforced.value = false
        awaitingBuzzerTimer.value?.pause()
        updatePlayersState()
    }

    private fun startAwaitingAnswerTimer() {
        if (answerTimer.value == null) {
            mutableAnswerTimer.value = CountDownTimer(scope, duration = answerTimerDuration.value)
        }

        mutableAnswerTimerEnforced.value = true
        answerTimer.value?.start(
            onComplete = {
                if (allowFollowupAnswers.value) {
                    enterTurnLoop()
                } else {
                    enterAwaitingTurnStartState()
                }
            },
            onCountDownTimerUpdate = {
                turnStateData.value.answerPlayer?.let {
                    updatePlayerTimeData(it, DeviceState.BuzzerWinnerPeriodTimed)
                }
            }
        )
        updatePlayersState()
    }

    private fun pauseAwaitingAnswerTimer() {
        mutableAnswerTimerEnforced.value = false
        answerTimer.value?.pause()
        updatePlayersState()
    }

    fun onStartTimerPress() {
        startAwaitingBuzzTimer()
    }

    fun onPauseTimerPress() {
        pauseAwaitingBuzzTimer()
    }

    fun onStartAnswerTimerPress() {
        startAwaitingAnswerTimer()
    }

    fun onPauseAnswerTimerPress() {
        pauseAwaitingAnswerTimer()
    }

    fun onIncorrectAnswerPress() {
        if (allowFollowupAnswers.value) {
            enterTurnLoop()
        } else {
            enterAwaitingTurnStartState()
        }
    }

    fun onCorrectAnswerPress() {
        enterAwaitingTurnStartState()
    }

    fun onPlayerAnswer(player: Player) {
        enterAwaitingAnswerState(player)
    }

    // MARK: Preset Functions
    private fun applySettingsConfig(settingsEntity: BuzzerSettingsEntity) {
        mutableAutoStartAnswerTimer.value = settingsEntity.autoStartAnswerTimer
        mutableAnswerTimerDuration.value = settingsEntity.answerTimerDuration
        mutableAllowImmediateAnswers.value = settingsEntity.allowImmediateAnswers
        mutableAllowFollowupAnswers.value = settingsEntity.allowFollowupAnswers
        mutableAllowMultipleAnswersFromSameUser.value =
            settingsEntity.allowMultipleAnswersFromSameUser
        mutableAutoStartAwaitingBuzzTimer.value = settingsEntity.autoStartAwaitingBuzzTimer
        mutableAwaitingBuzzTimerDuration.value = settingsEntity.awaitingBuzzTimerDuration
        mutableAwaitingBuzzTimerEnforced.value = settingsEntity.autoStartAwaitingBuzzTimer
    }

    private fun getCurrentSettingsEntity(): BuzzerSettingsEntity {
        return BuzzerSettingsEntity(
            configName = "",
            default = false,
            autoStartAwaitingBuzzTimer = autoStartAwaitingBuzzTimer.value,
            awaitingBuzzTimerDuration = awaitingBuzzTimerDuration.value,
            autoStartAnswerTimer = autoStartAnswerTimer.value,
            answerTimerDuration = answerTimerDuration.value,
            allowImmediateAnswers = allowImmediateAnswers.value,
            allowFollowupAnswers = allowFollowupAnswers.value,
            allowMultipleAnswersFromSameUser = allowMultipleAnswersFromSameUser.value
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
        localDatasource.setDefaultBuzzerPreset(presetName)
        mutableDefaultSettingPresetName.value = presetName
    }

    private suspend fun getDefaultSettingEntity(): SettingsEntity? {
        return localDatasource.getDefaultBuzzerPreset()
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
        const val TAG = "BuzzerGameRepository"
    }
}