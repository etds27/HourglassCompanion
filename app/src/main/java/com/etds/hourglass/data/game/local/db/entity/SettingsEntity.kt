package com.etds.hourglass.data.game.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey


interface SettingsEntity {
    var configName: String
    var default: Boolean
}

@Entity
data class SequentialSettingsEntity(
    @PrimaryKey override var configName: String,
    override var default: Boolean,
    val autoStartTurnTimer: Boolean,
    val turnTimerDuration: Long,
    val autoStartTotalTurnTimer: Boolean,
    val totalTurnTimerDuration: Long
): SettingsEntity

@Entity
data class BuzzerSettingsEntity (
    @PrimaryKey override var configName: String,
    override var default: Boolean,
    val autoStartAwaitingBuzzTimer: Boolean,
    val awaitingBuzzTimerDuration: Long,
    val autoStartAnswerTimer: Boolean,
    val answerTimerDuration: Long,
    val allowImmediateAnswers: Boolean,
    val allowFollowupAnswers: Boolean,
    val allowMultipleAnswersFromSameUser: Boolean,
): SettingsEntity