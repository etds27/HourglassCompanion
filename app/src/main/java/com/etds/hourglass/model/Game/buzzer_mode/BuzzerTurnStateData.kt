package com.etds.hourglass.model.Game.buzzer_mode

import com.etds.hourglass.model.Player.Player

data class BuzzerTurnStateData(
    /// When `mutableAllowImmediateAnswers` is not set, this flag will be set so that the app operator must manually enable buzzing
    val awaitingBuzzerEnabled: Boolean = false,

    /// Represents if we are waiting for peripheral devices to generate an input event to answer a question
    val awaitingBuzz: Boolean = false,

    /// If we are waiting for a user to answer a question
    val answerInProgress: Boolean = false,

    /// The player that is answering the question
    val answerPlayer: Player? = null,

    /// Track the players that have already attempted and answer on this question
    val playersWhoAlreadyAnswered: MutableList<Player> = mutableListOf()
)