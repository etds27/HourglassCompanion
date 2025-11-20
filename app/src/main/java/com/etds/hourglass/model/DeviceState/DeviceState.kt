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
    ConfigurationMode(18),
    DeviceColorMode(19), // This mode is used to set the user colors in the app
    DeviceLEDOffsetMode(20),
    Debug(0),
    Unknown(100)
}

fun DeviceState.displayName(): String {
    return when (this) {
        DeviceState.Off -> "Off"
        DeviceState.AwaitingConnection -> "Awaiting Connection"
        DeviceState.AwaitingGameStart -> "Awaiting Game Start"
        DeviceState.ActiveTurnEnforced -> "Active Turn (Enforced)"
        DeviceState.ActiveTurnNotEnforced -> "Active Turn (Not Enforced)"
        DeviceState.AwaitingTurn -> "Awaiting Turn"
        DeviceState.Skipped -> "Skipped"
        DeviceState.Paused -> "Paused"
        DeviceState.BuzzerAwaitingBuzz -> "Buzzer Awaiting Buzz"
        DeviceState.BuzzerAwaitingBuzzTimed -> "Buzzer Awaiting Buzz (Timed)"
        DeviceState.BuzzerAwaitingTurnEnd -> "Buzzer Awaiting Turn End"
        DeviceState.BuzzerResults -> "Buzzer Results"
        DeviceState.BuzzerWinnerPeriod -> "Buzzer Winner Period"
        DeviceState.BuzzerWinnerPeriodTimed -> "Buzzer Winner Period (Timed)"
        DeviceState.BuzzerAlreadyAnswered -> "Buzzer Already Answered"
        DeviceState.BuzzerAwaitingBuzzerEnabled -> "Buzzer Awaiting Enabled"
        DeviceState.BuzzerAwaitingTurnStart -> "Buzzer Awaiting Turn Start"
        DeviceState.ConfigurationMode -> "Configuration Mode"
        DeviceState.DeviceColorMode -> "Device Color Mode"
        DeviceState.DeviceLEDOffsetMode -> "Device LED Offset Mode"
        DeviceState.Debug -> "Debug"
        DeviceState.Unknown -> "Unknown"
    }
}

/**
 * Returns the number of colors expected to be displayed for a given device state.
 * This can be used by the UI to determine how many color slots to render.
 */
fun DeviceState.displayColorCount(): Int {
    return when (this) {
        DeviceState.Off -> 0
        DeviceState.AwaitingConnection -> 1
        DeviceState.AwaitingGameStart -> 0
        DeviceState.ActiveTurnEnforced -> 0
        DeviceState.ActiveTurnNotEnforced -> 4
        DeviceState.AwaitingTurn -> 3
        DeviceState.Skipped -> 1
        DeviceState.Paused -> 0
        DeviceState.BuzzerAwaitingBuzz -> 4
        DeviceState.BuzzerAwaitingBuzzTimed -> 0
        DeviceState.BuzzerAwaitingTurnEnd -> 0
        DeviceState.BuzzerResults -> 2
        DeviceState.BuzzerWinnerPeriod -> 2
        DeviceState.BuzzerWinnerPeriodTimed -> 0
        DeviceState.BuzzerAlreadyAnswered -> 0
        DeviceState.BuzzerAwaitingBuzzerEnabled -> 0
        DeviceState.BuzzerAwaitingTurnStart -> 4
        DeviceState.ConfigurationMode -> 0
        DeviceState.DeviceColorMode -> 2 // Primary and Accent
        DeviceState.DeviceLEDOffsetMode -> 2 // Doesn't matter because we arent showing color selectors
        DeviceState.Debug -> 0
        DeviceState.Unknown -> 0
    }
}
