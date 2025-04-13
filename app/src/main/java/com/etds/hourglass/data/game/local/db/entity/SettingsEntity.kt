package com.etds.hourglass.data.game.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class SettingsEntity (
    @PrimaryKey val configName: String,
    var default: Boolean,
    val autoStartAwaitingBuzzTimer: Boolean,
    val awaitingBuzzTimerDuration: Long,
    val autoStartAnswerTimer: Boolean,
    val answerTimerDuration: Long,
    val allowImmediateAnswers: Boolean,
    val allowFollowupAnswers: Boolean,
    val allowMultipleAnswersFromSameUser: Boolean,
)
