package com.etds.hourglass.model.DeviceState

enum class DeviceState(val value: Int) {
    Off(0),
    AwaitingConnection(1),
    AwaitingGameStart(2),
    ActiveTurnEnforced(3),
    ActiveTurnNotEnforced(4),
    AwaitingTurn(5),
    Skipped(6),
    Paused(7),
    BuzzerAwaitingBuzz(9),
    BuzzerAwaitingBuzzTimed(10),
    BuzzerAwaitingTurnEnd(11),
    BuzzerResults(12),
    BuzzerWinnerPeriod(13),
    BuzzerWinnerPeriodTimed(14),
    BuzzerAlreadyAnswered(15),
    BuzzerAwaitingBuzzerEnabled(16),
    BuzzerAwaitingTurnStart(17),
    Debug(0),
    Unknown(100)
}