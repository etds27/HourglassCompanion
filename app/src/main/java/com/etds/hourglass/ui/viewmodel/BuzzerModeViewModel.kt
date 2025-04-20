package com.etds.hourglass.ui.viewmodel

import androidx.lifecycle.viewModelScope
import com.etds.hourglass.data.game.BuzzerGameRepository
import com.etds.hourglass.model.DeviceState.BuzzerTurnState
import com.etds.hourglass.model.Game.buzzer_mode.BuzzerAwaitingBuzzTurnState
import com.etds.hourglass.model.Game.buzzer_mode.BuzzerTurnStateData
import com.etds.hourglass.model.Player.Player
import com.etds.hourglass.util.CountDownTimer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject


interface BuzzerModeViewModelProtocol : GameViewModelProtocol {
    // MARK: Setting Flows
    val allowImmediateAnswers: StateFlow<Boolean>
    val autoStartAwaitingBuzzTimer: StateFlow<Boolean>
    val awaitingBuzzTimerDuration: StateFlow<Long>
    val awaitingBuzzTimerEnforced: StateFlow<Boolean>
    val autoStartAnswerTimer: StateFlow<Boolean>
    val answerTimerEnforced: StateFlow<Boolean>
    val answerTimerDuration: StateFlow<Long>
    val allowFollowupAnswers: StateFlow<Boolean>
    val allowMultipleAnswersFromSameUser: StateFlow<Boolean>

    // MARK: Game Flows
    val turnState: StateFlow<BuzzerTurnState>
    val turnStateData: StateFlow<BuzzerTurnStateData>
    val awaitingBuzzerTimer: StateFlow<CountDownTimer?>
    val awaitingAnswerTimer: StateFlow<CountDownTimer?>

    // MARK: Setting Functions
    fun setAutoStartAwaitingBuzzTimer(value: Boolean)
    fun setAwaitBuzzTimerEnforced(value: Boolean)
    fun setAwaitBuzzTimerDuration(value: Number?)
    fun setAutoStartAnswerTimer(value: Boolean)
    fun setEnableAnswerTimer(value: Boolean)
    fun setAnswerTimerDuration(value: Number?)
    fun setAllowImmediateAnswers(value: Boolean)
    fun setAllowFollowupAnswers(value: Boolean)
    fun setAllowMultipleAnswersFromSameUser(value: Boolean)

    // MARK: Game Functions
    fun onStartTimerPress()
    fun onPauseTimerPress()
    fun onEnableBuzzersPress()
    fun onDisableBuzzersPress()
    fun onStartAnswerTimerPress()
    fun onPauseAnswerTimerPress()
    fun onIncorrectAnswerPress()
    fun onCorrectAnswerPress()
    fun onPlayerAnswer(player: Player)
    fun onStartTurnPress()
    fun onEndTurnPress()

}

@HiltViewModel
class BuzzerModeViewModel @Inject constructor(
    private val gameRepository: BuzzerGameRepository
) : GameViewModel(gameRepository), BuzzerModeViewModelProtocol {

    // MARK: Setting Flows
    override val allowImmediateAnswers = gameRepository.allowImmediateAnswers
    override val autoStartAwaitingBuzzTimer = gameRepository.autoStartAwaitingBuzzTimer
    override val awaitingBuzzTimerDuration = gameRepository.awaitingBuzzTimerDuration
    override val awaitingBuzzTimerEnforced = gameRepository.awaitingBuzzTimerEnforced
    override val autoStartAnswerTimer = gameRepository.autoStartAnswerTimer
    override val answerTimerEnforced = gameRepository.answerTimerEnforced
    override val answerTimerDuration = gameRepository.answerTimerDuration
    override val allowFollowupAnswers = gameRepository.allowFollowupAnswers
    override val allowMultipleAnswersFromSameUser = gameRepository.allowMultipleAnswersFromSameUser

    // MARK: Game Flows
    override val turnState = gameRepository.turnState
    override val turnStateData = gameRepository.turnStateData

    override val awaitingBuzzerTimer: StateFlow<CountDownTimer?> =
        gameRepository.awaitingBuzzerTimer
    override val awaitingAnswerTimer: StateFlow<CountDownTimer?> = gameRepository.answerTimer

    // MARK: Setting Functions
    override fun setAwaitBuzzTimerEnforced(value: Boolean) {
        gameRepository.setEnableAwaitingBuzzTimer(value)
    }

    override fun setAwaitBuzzTimerDuration(value: Number?) {
        if (value == null) return
        gameRepository.setAwaitingBuzzTimerDuration(value.toLong() * 1000L)
    }

    override fun setAutoStartAnswerTimer(value: Boolean) {
        gameRepository.setAutoStartAnswerTimer(value)
    }

    override fun setEnableAnswerTimer(value: Boolean) {
        gameRepository.setEnableAnswerTimer(value)
    }

    override fun setAnswerTimerDuration(value: Number?) {
        if (value == null) return
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

    // MARK: Game Functions
    override fun onEnableBuzzersPress() {
        gameRepository.onEnableBuzzersPress()
    }

    override fun onDisableBuzzersPress() {
        gameRepository.onDisableBuzzersPress()
    }

    override fun onStartAnswerTimerPress() {
        gameRepository.onStartAnswerTimerPress()
    }

    override fun onPauseAnswerTimerPress() {
        gameRepository.onPauseAnswerTimerPress()
    }

    override fun onIncorrectAnswerPress() {
        gameRepository.onIncorrectAnswerPress()
    }

    override fun onCorrectAnswerPress() {
        gameRepository.onCorrectAnswerPress()
    }

    override fun onPlayerAnswer(player: Player) {
        gameRepository.onPlayerAnswer(player)
    }

    override fun onStartTurnPress() {
        gameRepository.onStartTurnPress()
    }

    override fun onEndTurnPress() {
        gameRepository.endTurn()
    }

    override fun onStartTimerPress() {
        gameRepository.onStartTimerPress()
    }

    override fun onPauseTimerPress() {
        gameRepository.onPauseTimerPress()
    }
}

class MockBuzzerModeViewModel : MockGameViewModel(), BuzzerModeViewModelProtocol {
    private val timer = CountDownTimer(
        scope = viewModelScope,
        duration = 60000L
    )


    val _allowImmediateAnswers = MutableStateFlow(false)
    override val allowImmediateAnswers: StateFlow<Boolean> = _allowImmediateAnswers

    val _autoStartAwaitingBuzzTimer = MutableStateFlow(false)
    override val autoStartAwaitingBuzzTimer: StateFlow<Boolean> = _autoStartAwaitingBuzzTimer

    val _awaitingBuzzTimerDuration = MutableStateFlow(5000L)
    override val awaitingBuzzTimerDuration: StateFlow<Long> = _awaitingBuzzTimerDuration

    val _awaitingBuzzTimerEnforced = MutableStateFlow(false)
    override val awaitingBuzzTimerEnforced: StateFlow<Boolean> = _awaitingBuzzTimerEnforced

    val _autoStartAnswerTimer = MutableStateFlow(false)
    override val autoStartAnswerTimer: StateFlow<Boolean> = _autoStartAnswerTimer

    val _answerTimerEnforced = MutableStateFlow(false)
    override val answerTimerEnforced: StateFlow<Boolean> = _answerTimerEnforced

    val _answerTimerDuration = MutableStateFlow(10000L)
    override val answerTimerDuration: StateFlow<Long> = _answerTimerDuration

    val _allowFollowupAnswers = MutableStateFlow(false)
    override val allowFollowupAnswers: StateFlow<Boolean> = _allowFollowupAnswers

    val _allowMultipleAnswersFromSameUser = MutableStateFlow(false)
    override val allowMultipleAnswersFromSameUser: StateFlow<Boolean> =
        _allowMultipleAnswersFromSameUser

    val _turnState = MutableStateFlow(BuzzerTurnState.BuzzerAwaitingBuzz)
    override val turnState: StateFlow<BuzzerTurnState> = _turnState

    val _turnStateData = MutableStateFlow(
        BuzzerAwaitingBuzzTurnState.applyStateTo(
            BuzzerTurnStateData().copy(playersWhoAlreadyAnswered = mutableListOf(players.value[1]))
        )
    )
    override val turnStateData: StateFlow<BuzzerTurnStateData> = _turnStateData

    override val awaitingBuzzerTimer = MutableStateFlow(timer)
    override val awaitingAnswerTimer: StateFlow<CountDownTimer?> = MutableStateFlow(timer)

    override fun setAwaitBuzzTimerEnforced(value: Boolean) {
        _awaitingBuzzTimerEnforced.value = value
    }

    override fun setAwaitBuzzTimerDuration(value: Number?) {
        if (value == null) return
        _awaitingBuzzTimerDuration.value = value.toLong()
    }

    override fun setAutoStartAnswerTimer(value: Boolean) {
        _autoStartAnswerTimer.value = value
    }

    override fun setEnableAnswerTimer(value: Boolean) {
        _answerTimerEnforced.value = value
    }

    override fun setAnswerTimerDuration(value: Number?) {
        if (value == null) return
        _answerTimerDuration.value = value.toLong()
    }

    override fun setAllowImmediateAnswers(value: Boolean) {
        _allowImmediateAnswers.value = value
    }

    override fun setAllowFollowupAnswers(value: Boolean) {
        _allowFollowupAnswers.value = value
    }

    override fun setAllowMultipleAnswersFromSameUser(value: Boolean) {
        _allowMultipleAnswersFromSameUser.value = value
    }

    override fun setAutoStartAwaitingBuzzTimer(value: Boolean) {
        _autoStartAwaitingBuzzTimer.value = value
    }

    // MARK: Game Functions

    override fun pauseGame() {
        super.pauseGame()
        _awaitingBuzzTimerEnforced.value = false
        _answerTimerEnforced.value = false
        timer.pause()
    }

    override fun onEnableBuzzersPress() {
        _turnState.value = BuzzerTurnState.BuzzerAwaitingBuzzerEnabled
        _turnStateData.value = BuzzerTurnState.BuzzerAwaitingBuzzerEnabled.getConfig()
            .applyStateTo(turnStateData.value)
    }

    override fun onDisableBuzzersPress() {
        _turnState.value = BuzzerTurnState.BuzzerAwaitingBuzz
        _turnStateData.value =
            BuzzerTurnState.BuzzerAwaitingBuzz.getConfig().applyStateTo(turnStateData.value)
    }

    override fun onStartAnswerTimerPress() {
        _answerTimerEnforced.value = true
        timer.start()
    }

    override fun onPauseAnswerTimerPress() {
        _answerTimerEnforced.value = false
        timer.start()
    }

    override fun onIncorrectAnswerPress() {
        _turnStateData.value = BuzzerTurnState.BuzzerAwaitingBuzz.getConfig().applyStateTo(
            BuzzerTurnState.BuzzerEnterTurnLoop.getConfig().applyStateTo(
                turnStateData.value
            )
        )
        _turnState.value = BuzzerTurnState.BuzzerAwaitingBuzz
    }

    override fun onCorrectAnswerPress() {
        _turnStateData.value = BuzzerTurnState.BuzzerAwaitingBuzz.getConfig().applyStateTo(
            BuzzerTurnState.BuzzerEnterTurnLoop.getConfig().applyStateTo(
                BuzzerTurnState.BuzzerTurnStart.getConfig().applyStateTo(turnStateData.value)
            )
        )
        _turnState.value = BuzzerTurnState.BuzzerAwaitingBuzz

    }

    override fun onPlayerAnswer(player: Player) {
        _turnStateData.value =
            BuzzerTurnState.BuzzerAwaitingAnswer.getConfigForPlayer(player = player)
                .applyStateTo(turnStateData.value)
        _turnState.value = BuzzerTurnState.BuzzerAwaitingAnswer
    }

    override fun onStartTurnPress() {
        _turnStateData.value = BuzzerTurnState.BuzzerAwaitingBuzz.getConfig().applyStateTo(
            BuzzerTurnState.BuzzerEnterTurnLoop.getConfig().applyStateTo(
                turnStateData.value
            )
        )
        _turnState.value = BuzzerTurnState.BuzzerAwaitingBuzz
    }

    override fun onEndTurnPress() {
        _turnState.value = BuzzerTurnState.BuzzerAwaitingTurnStart
        _turnStateData.value = BuzzerTurnState.BuzzerAwaitingTurnStart.getConfig().applyStateTo(
            turnStateData.value
        )
    }

    override fun onStartTimerPress() {
        _awaitingBuzzTimerEnforced.value = true
        timer.start()
    }

    override fun onPauseTimerPress() {
        _awaitingBuzzTimerEnforced.value = false
        timer.pause()
    }

    override fun onInitialize() {}
}
