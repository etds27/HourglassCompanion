package com.etds.hourglass.model.DeviceState

import com.etds.hourglass.model.Game.buzzer_mode.BuzzerAwaitingAnswerTurnState
import com.etds.hourglass.model.Game.buzzer_mode.BuzzerAwaitingBuzzTurnState
import com.etds.hourglass.model.Game.buzzer_mode.BuzzerAwaitingBuzzerEnabledTurnState
import com.etds.hourglass.model.Game.buzzer_mode.BuzzerEnterTurnLoopTurnState
import com.etds.hourglass.model.Game.buzzer_mode.BuzzerInvalidTurnState
import com.etds.hourglass.model.Game.buzzer_mode.BuzzerTurnStartTurnState
import com.etds.hourglass.model.Game.buzzer_mode.BuzzerTurnStateConfig
import com.etds.hourglass.model.Player.Player

/// App state during a buzzer mode turn
enum class BuzzerTurnState {
    /// Period where the device is waiting for the peripheral devices to buzz
    /// The peripheral state can be one of the following while this state is active
    ///      BuzzerAwaitingBuzz(9),
    ///      BuzzerAwaitingBuzzTimed(10),
    ///      BuzzerAlreadyAnswered(15),
    ///      Skipped(6),
    BuzzerAwaitingBuzz,

    /// Period where app user is posing a question and the buzzer is not enabled
    /// The peripheral state can be one of the following while this state is active
    ///      BuzzerAwaitingBuzzerEnabled(16),
    ///      BuzzerAlreadyAnswered(15),
    ///      Skipped(6),
    BuzzerAwaitingBuzzerEnabled,

    /// A reset state for when the turn immediately has started this will completely overwrite all device state properties
    BuzzerTurnStart,

    /// State when the app enters the turn loop either from turn start or a previous incorrect answer
    BuzzerEnterTurnLoop,

    /// Period where a single user is answering a question, while others are waiting
    /// The peripheral state can be one of the following while this state is active
    ///     BuzzerResults(12),
    ///     BuzzerWinnerPeriod(13),
    ///     BuzzerWinnerPeriodTimed(14),
    ///     BuzzerAlreadyAnswered(15),
    ///      Skipped(6),
    BuzzerAwaitingAnswer;

    /// Get the turn state config for all states that do not require a specified player
    fun getConfig(player: Player? = null): BuzzerTurnStateConfig {
        when (this) {
            BuzzerAwaitingBuzz -> return BuzzerAwaitingBuzzTurnState
            BuzzerAwaitingBuzzerEnabled -> return BuzzerAwaitingBuzzerEnabledTurnState
            BuzzerTurnStart -> return BuzzerTurnStartTurnState
            BuzzerEnterTurnLoop -> return BuzzerEnterTurnLoopTurnState
            BuzzerAwaitingAnswer -> return BuzzerInvalidTurnState
        }
    }

    /// Get the turn state config for all states that require a specified player
    fun getConfigForPlayer(player: Player): BuzzerTurnStateConfig {
        when (this) {
            BuzzerAwaitingBuzz -> return BuzzerInvalidTurnState
            BuzzerAwaitingBuzzerEnabled -> return BuzzerInvalidTurnState
            BuzzerTurnStart -> return BuzzerInvalidTurnState
            BuzzerEnterTurnLoop -> return BuzzerInvalidTurnState
            BuzzerAwaitingAnswer -> return BuzzerAwaitingAnswerTurnState(player)
        }
    }
}

