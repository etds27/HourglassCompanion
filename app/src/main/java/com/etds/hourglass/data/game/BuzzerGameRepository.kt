package com.etds.hourglass.data.game

import android.util.Log
import com.etds.hourglass.data.BLEData.remote.BLERemoteDatasource
import com.etds.hourglass.data.game.local.LocalGameDatasource
import com.etds.hourglass.model.DeviceState.DeviceState
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

    /// Represents if we are waiting for peripheral devices to generate an input event to answer a question
    private val mutableAwaitingBuzz: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val awaitingBuzz: StateFlow<Boolean> = mutableAwaitingBuzz

    /// If we are waiting for a user to answer a question
    private val mutableAnswerInProgress: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val answerInProgress: StateFlow<Boolean> = mutableAnswerInProgress

    /// The player that is answering the question
    private val mutableAnswerPlayer: MutableStateFlow<Player?> = MutableStateFlow(null)
    val answerPlayer: StateFlow<Player?> = mutableAnswerPlayer

    private val mutableAllowMultipleAnswersFromSameUser: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val allowMultipleAnswersFromSameUser: StateFlow<Boolean> = mutableAllowMultipleAnswersFromSameUser

    private val mutablePlayersWhoAlreadyAnswered: MutableStateFlow<List<Player>> = MutableStateFlow(listOf())
    val playersWhoAlreadyAnswered: StateFlow<List<Player>> = mutablePlayersWhoAlreadyAnswered

    override fun startTurn() {
        super.startTurn()

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

        // Reset buzzer properties at the start of each turn
        mutableAwaitingBuzz.value = false
        mutableAnswerInProgress.value = false
        mutableAnswerPlayer.value = null
        mutablePlayersWhoAlreadyAnswered.value = listOf()
        enterAwaitingBuzzState()
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
    /// 4. Check to see if player has buzzed
    /// 4.1 If no player has buzzed: BuzzerAwaitingBuzz
    /// 4.2 If no player has buzzed and the turn timer is enforced: BuzzerAwaitingBuzzTimed
    /// 5. Check to see if answer is in progress
    /// 5.1 If answer is in progress and this is the player answering: BuzzerWinnerPeriod
    /// 5.2 If answer is in progress and this is not the player answering: BuzzerAwaitingTurnEnd
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

        if (awaitingBuzz.value) {
            return if (playersWhoAlreadyAnswered.value.contains(player) && !allowMultipleAnswersFromSameUser.value) {
                    DeviceState.BuzzerAlreadyAnswered
            } else {
                if (enforceTimer.value) {
                    DeviceState.BuzzerAwaitingBuzzTimed
                } else {
                    DeviceState.BuzzerAwaitingBuzz
                }
            }
        }

        if (answerInProgress.value) {
            return if (player == answerPlayer.value) {
                if (answerInProgress.value) {
                    DeviceState.BuzzerWinnerPeriod
                } else {
                    DeviceState.BuzzerWinnerPeriodTimed
                }
            } else {
                DeviceState.BuzzerResults
            }
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
            if (awaitingBuzz.value) {
                // Immediately set awaiting buzz to false to prevent other players from buzzing
                // In this case, this buzz is the first received and wins the answer period

                // Ignore buzzes from user's who have already answered when the multi answer setting is off
                if (playersWhoAlreadyAnswered.value.contains(player) && !allowMultipleAnswersFromSameUser.value) {
                    return
                }

                exitAwaitingBuzzState()
                enterAwaitingAnswerState(player = player)
                updatePlayersState()
            }
        }
    }


    // Game Sequencing
    private fun enterAwaitingAnswerState(player: Player) {
        mutableAnswerInProgress.value = true
        mutableAnswerPlayer.value = player
        Log.d(TAG, "Player ${player.name} buzzed first")

        updatePlayerState(player)
        updatePlayersState()
    }

    /// Perform idle actions while waiting for a user peripheral to send a buzz event
    private fun enterAwaitingBuzzState() {
        mutableAwaitingBuzz.value = true
    }

    private fun exitAwaitingBuzzState() {
        mutableAwaitingBuzz.value = false
    }
}