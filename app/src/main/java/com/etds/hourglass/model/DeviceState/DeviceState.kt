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
    Debug(0)
}