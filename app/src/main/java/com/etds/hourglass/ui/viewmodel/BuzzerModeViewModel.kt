package com.etds.hourglass.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.etds.hourglass.data.game.BuzzerGameRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.lang.Thread.State
import javax.inject.Inject


interface BuzzerModeViewModelProtocol {
    // MARK: Setting Flows
    val allowImmediateAnswers: StateFlow<Boolean>
    val autoStartAwaitingBuzzTimer: StateFlow<Boolean>
    val awaitingBuzzTimerDuration: StateFlow<Long>
    val awaitingBuzzTimerEnforced: StateFlow<Boolean>
    val answerTimerEnforced: StateFlow<Boolean>
    val answerTimerDuration: StateFlow<Long>
    val allowFollowupAnswers: StateFlow<Boolean>
    val allowMultipleAnswersFromSameUser: StateFlow<Boolean>

    fun setAwaitBuzzTimerEnforced(value: Boolean)
    fun setAwaitBuzzTimerDuration(value: Number)
    fun setEnableAnswerTimer(value: Boolean)
    fun setAnswerTimerDuration(value: Number)
    fun setAllowImmediateAnswers(value: Boolean)
    fun setAllowFollowupAnswers(value: Boolean)
    fun setAllowMultipleAnswersFromSameUser(value: Boolean)
    fun setAutoStartAwaitingBuzzTimer(value: Boolean)
}

class BuzzerModeViewModel @Inject constructor(
    private val gameRepository: BuzzerGameRepository
) : ViewModel(), BuzzerModeViewModelProtocol {

    // MARK: Setting Flows
    override val allowImmediateAnswers = gameRepository.allowImmediateAnswers
    override val autoStartAwaitingBuzzTimer = gameRepository.autoStartAwaitingBuzzTimer
    override val awaitingBuzzTimerDuration = gameRepository.awaitingBuzzTimerDuration
    override val awaitingBuzzTimerEnforced = gameRepository.awaitingBuzzTimerEnforced
    override val answerTimerEnforced = gameRepository.answerTimerEnforced
    override val answerTimerDuration = gameRepository.answerTimerDuration
    override val allowFollowupAnswers = gameRepository.allowFollowupAnswers
    override val allowMultipleAnswersFromSameUser = gameRepository.allowMultipleAnswersFromSameUser

    override fun setAwaitBuzzTimerEnforced(value: Boolean) {
        gameRepository.setEnableAwaitingBuzzTimer(value)
    }

    override fun setAwaitBuzzTimerDuration(value: Number) {
        gameRepository.setAwaitingBuzzTimerDuration(value.toLong() * 1000L)
    }

    override fun setEnableAnswerTimer(value: Boolean) {
        gameRepository.setEnableAnswerTimer(value)
    }

    override fun setAnswerTimerDuration(value: Number) {
        gameRepository.setAnswerTimerDuration(value.toLong() * 1000L)
    }

    override fun setAllowImmediateAnswers(value: Boolean) {
        gameRepository.setAllowImmediateAnswers(value)
    }

    override fun setAllowFollowupAnswers(value: Boolean) {
        gameRepository.setAllowFollowupAnswers(value)
    }

    override fun setAllowMultipleAnswersFromSameUser(value: Boolean) {
        gameRepository.setAllowMultipleAnswersFromSameUser(value)
    }

    override fun setAutoStartAwaitingBuzzTimer(value: Boolean) {
        gameRepository.setAutoStartAwaitingBuzzTimer(value)
    }
}

class MockBuzzerModeViewModel: BuzzerModeViewModelProtocol {
    private val _allowImmediateAnswers = MutableStateFlow(false)
    override val allowImmediateAnswers: StateFlow<Boolean> = _allowImmediateAnswers

    private val _autoStartAwaitingBuzzTimer = MutableStateFlow(false)
    override val autoStartAwaitingBuzzTimer: StateFlow<Boolean> = _autoStartAwaitingBuzzTimer

    private val _awaitingBuzzTimerDuration = MutableStateFlow(5000L)
    override val awaitingBuzzTimerDuration: StateFlow<Long> = _awaitingBuzzTimerDuration

    private val _awaitingBuzzTimerEnforced = MutableStateFlow(false)
    override val awaitingBuzzTimerEnforced: StateFlow<Boolean> = _awaitingBuzzTimerEnforced

    private val _answerTimerEnforced = MutableStateFlow(false)
    override val answerTimerEnforced: StateFlow<Boolean> = _answerTimerEnforced

    private val _answerTimerDuration = MutableStateFlow(10000L)
    override val answerTimerDuration: StateFlow<Long> = _answerTimerDuration

    private val _allowFollowupAnswers = MutableStateFlow(false)
    override val allowFollowupAnswers: StateFlow<Boolean> = _allowFollowupAnswers

    private val _allowMultipleAnswersFromSameUser = MutableStateFlow(false)
    override val allowMultipleAnswersFromSameUser: StateFlow<Boolean> = _allowMultipleAnswersFromSameUser

    override fun setAwaitBuzzTimerEnforced(value: Boolean) {

    }

    override fun setAwaitBuzzTimerDuration(value: Number) {

    }

    override fun setEnableAnswerTimer(value: Boolean) {

    }

    override fun setAnswerTimerDuration(value: Number) {

    }

    override fun setAllowImmediateAnswers(value: Boolean) {

    }

    override fun setAllowFollowupAnswers(value: Boolean) {

    }

    override fun setAllowMultipleAnswersFromSameUser(value: Boolean) {

    }

    override fun setAutoStartAwaitingBuzzTimer(value: Boolean) {

    }
}
