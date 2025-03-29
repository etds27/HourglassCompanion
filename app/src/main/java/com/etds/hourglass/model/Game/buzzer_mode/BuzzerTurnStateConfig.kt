package com.etds.hourglass.model.Game.buzzer_mode

import com.etds.hourglass.model.Player.Player

sealed interface BuzzerTurnStateConfig {
    fun applyStateTo(currentState: BuzzerTurnStateData): BuzzerTurnStateData
}

data object BuzzerAwaitingBuzzTurnState : BuzzerTurnStateConfig {
    override fun applyStateTo(currentState: BuzzerTurnStateData): BuzzerTurnStateData {
        return currentState.copy(awaitingBuzz = true, awaitingBuzzerEnabled = false)
    }
}

data object BuzzerAwaitingBuzzTimedTurnState : BuzzerTurnStateConfig {
    override fun applyStateTo(currentState: BuzzerTurnStateData): BuzzerTurnStateData {
        return currentState.copy(awaitingBuzz = true, awaitingBuzzerEnabled = false)
    }
}

data object BuzzerAwaitingBuzzerEnabledTurnState : BuzzerTurnStateConfig {
    override fun applyStateTo(currentState: BuzzerTurnStateData): BuzzerTurnStateData {
        return currentState.copy(awaitingBuzzerEnabled = true, awaitingBuzz = false)
    }
}

data object BuzzerTurnStartTurnState: BuzzerTurnStateConfig {
    override fun applyStateTo(currentState: BuzzerTurnStateData): BuzzerTurnStateData {
        return BuzzerTurnStateData()
    }

    /// Reset the state of the game
    fun getDefaultTurnStartState(): BuzzerTurnStateData {
        return BuzzerTurnStateData()
    }
}

data object BuzzerEnterTurnLoopTurnState: BuzzerTurnStateConfig {
    override fun applyStateTo(currentState: BuzzerTurnStateData): BuzzerTurnStateData {
        return currentState.copy(awaitingBuzz = false, answerInProgress = false, answerPlayer = null, awaitingBuzzerEnabled = false)
    }
}

data class BuzzerAwaitingAnswerTurnState(val winningPlayer: Player): BuzzerTurnStateConfig {
    override fun applyStateTo(currentState: BuzzerTurnStateData): BuzzerTurnStateData {
        val newState = currentState.copy(awaitingBuzz = false, answerInProgress = true, answerPlayer = winningPlayer)
        newState.playersWhoAlreadyAnswered.add(winningPlayer)
        return newState
    }
}

data object BuzzerInvalidTurnState: BuzzerTurnStateConfig {
    override fun applyStateTo(currentState: BuzzerTurnStateData): BuzzerTurnStateData {
        throw  Exception("Invalid turn state")
    }
}
