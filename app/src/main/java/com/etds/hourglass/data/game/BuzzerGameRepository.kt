package com.etds.hourglass.data.game

import android.util.Log
import com.etds.hourglass.data.BLEData.remote.BLERemoteDatasource
import com.etds.hourglass.data.game.local.LocalGameDatasource
import com.etds.hourglass.model.DeviceState.BuzzerTurnState
import com.etds.hourglass.model.DeviceState.DeviceState
import com.etds.hourglass.model.Game.buzzer_mode.BuzzerAwaitingAnswerTurnState
import com.etds.hourglass.model.Game.buzzer_mode.BuzzerAwaitingBuzzTimedTurnState
import com.etds.hourglass.model.Game.buzzer_mode.BuzzerAwaitingBuzzTurnState
import com.etds.hourglass.model.Game.buzzer_mode.BuzzerAwaitingBuzzerEnabledTurnState
import com.etds.hourglass.model.Game.buzzer_mode.BuzzerEnterTurnLoopTurnState
import com.etds.hourglass.model.Game.buzzer_mode.BuzzerTurnStartTurnState
import com.etds.hourglass.model.Game.buzzer_mode.BuzzerTurnStateData
import com.etds.hourglass.model.Player.Player
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject


class BuzzerGameRepository @Inject constructor(
    private val localGameDatasource: LocalGameDatasource,
    private val bluetoothDatasource: BLERemoteDatasource,
    private val scope: CoroutineScope
) : GameRepository(
    localGameDatasource = localGameDatasource,
    bluetoothDatasource = bluetoothDatasource,
    scope = scope
) {

    // MARK: State Properties

    /// The current state of a turn for the Buzzer mode
    private val mutableTurnState: MutableStateFlow<BuzzerTurnState> = MutableStateFlow(BuzzerTurnState.BuzzerTurnStart)
    val turnState: StateFlow<BuzzerTurnState> = mutableTurnState

    /// Current resolved turn state data properties that are mutated during state transitions
    private val mutableTurnStateData: MutableStateFlow<BuzzerTurnStateData> =
        MutableStateFlow(BuzzerTurnStartTurnState.getDefaultTurnStartState())
    val turnStateData: StateFlow<BuzzerTurnStateData> = mutableTurnStateData

    // MARK: Setting Properties

    /// Allows users to be able to immediately buzz when the turn loop starts
    private val mutableAllowImmediateAnswers: MutableStateFlow<Boolean> = MutableStateFlow(false)
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


    override fun setDeviceCallbacks(player: Player) {
        player.setDeviceOnActiveTurnCallback { playerValue: Player, newValue: Boolean ->
            onUserInputEvent(playerValue, newValue)
        }
        super.setDeviceCallbacks(player)
    }


    override fun startTurn() {
        super.startTurn()
        mutableTurnStateData.value =
            BuzzerTurnStartTurnState.applyStateTo(currentState = turnStateData.value)
        enterTurnLoop()
    }

    fun enterTurnLoop() {
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

        if (autoStartAwaitingBuzzTimer.value) {

        } else {
            enterAwaitingBuzzerEnabledState()
        }
        if (awaitingBuzzTimerEnforced.value) {
            if (allowImmediateAnswers.value) {
                enterAwaitingBuzzState()
            } else {
                enterAwaitingBuzzerEnabledState()
            }
        } else {
            if (autoStartAwaitingBuzzTimer.value) {
                enterAwaitingBuzzTimedState()
            } else {
                if (allowImmediateAnswers.value) {
                    enterAwaitingBuzzState()
                } else {
                    enterAwaitingBuzzerEnabledState()
                }
            }
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

        if (turnState.value == BuzzerTurnState.BuzzerAwaitingAnswer) {
            return if (player == turnStateData.value.answerPlayer) {
                if (answerTimerEnforced.value) {
                    DeviceState.BuzzerWinnerPeriod
                } else {
                    DeviceState.BuzzerWinnerPeriodTimed
                }
            } else {
                DeviceState.BuzzerResults
            }
        }

        if (turnStateData.value.playersWhoAlreadyAnswered.contains(player) && !allowMultipleAnswersFromSameUser.value) {
            return DeviceState.BuzzerAlreadyAnswered
        }


        if (turnState.value == BuzzerTurnState.BuzzerAwaitingBuzz) {
            if (enforceTimer.value) {
                DeviceState.BuzzerAwaitingBuzzTimed
            } else {
                DeviceState.BuzzerAwaitingBuzz
            }
        }

        if (!allowImmediateAnswers.value && turnState.value == BuzzerTurnState.BuzzerAwaitingBuzzerEnabled) {
            return DeviceState.BuzzerAwaitingBuzzerEnabled
        }

        return DeviceState.Unknown
    }


    override fun updatePlayerDevice(player: Player) {
        val deviceState = resolvePlayerDeviceState(player)

        // Ensure data is updated when updating to a new state that requires supplemental data
        when (deviceState) {
            DeviceState.BuzzerAwaitingBuzzTimed -> {
                updatePlayerTimeData(player)
            }

            DeviceState.BuzzerWinnerPeriodTimed -> {
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


    // MARK: Input Event Handlers
    private fun onUserInputEvent(player: Player, turnValue: Boolean) {
        // BLE Notifications are fired from the peripheral device by performing a write of 1 followed
        // by a write of 0. Only the write of 1 will be used to initiate state change
        if (turnValue) {
            if (turnStateData.value.awaitingBuzz) {
                // Immediately set awaiting buzz to false to prevent other players from buzzing
                // In this case, this buzz is the first received and wins the answer period

                // Ignore buzzes from user's who have already answered when the multi answer setting is off
                if (turnStateData.value.playersWhoAlreadyAnswered.contains(player) && !allowMultipleAnswersFromSameUser.value) {
                    return
                }

                enterAwaitingAnswerState(player = player)

                updatePlayersState()
            }
        }
    }


    // Game Sequencing
    private fun enterAwaitingAnswerState(player: Player) {
        transitionTurnStateTo(BuzzerTurnState.BuzzerAwaitingAnswer, player = player)
        Log.d(TAG, "Player ${player.name} buzzed first")

        updatePlayerState(player)
        updatePlayersState()
    }

    /// Perform idle actions while waiting for a user peripheral to send a buzz event
    private fun enterAwaitingBuzzState() {
        transitionTurnStateTo(BuzzerTurnState.BuzzerAwaitingBuzz)
    }

    private fun enterAwaitingBuzzTimedState() {
        transitionTurnStateTo(BuzzerTurnState.BuzzerAwaitingBuzz)
    }

    private fun enterAwaitingBuzzerEnabledState() {
        transitionTurnStateTo(BuzzerTurnState.BuzzerAwaitingBuzzerEnabled)
    }

    /// Change the current turn state to the new value and apply the state transition to the current turn properties
    private fun transitionTurnStateTo(newState: BuzzerTurnState) {
        mutableTurnState.value = newState
        mutableTurnStateData.value = newState.getConfig().applyStateTo(turnStateData.value)
    }

    /// Change the current turn state to the new value and apply the state transition to the current turn properties
    private fun transitionTurnStateTo(newState: BuzzerTurnState, player: Player) {
        mutableTurnState.value = newState
        mutableTurnStateData.value = newState.getConfigForPlayer(player = player).applyStateTo(turnStateData.value)
    }
}