package com.etds.hourglass.model.game_mode_navigation

import com.etds.hourglass.R

sealed interface GameModeNavigationConfig {
    val displayName: String
    val navigationName: String
    val enabled: Boolean
    val colorValue: Int
    val accentColorValue: Int
}

data object BuzzerGameModeNavigationConfig : GameModeNavigationConfig {
    override val displayName: String = "Trivia"
    override val navigationName: String = "buzzer_game"
    override val enabled: Boolean = true
    override val colorValue: Int = R.color.hourglass_light_red
    override val accentColorValue: Int = R.color.hourglass_dark_red
}

data object SequentialGameModeNavigationConfig : GameModeNavigationConfig {
    override val displayName: String = "Sequential"
    override val navigationName: String = "game"
    override val enabled: Boolean = true
    override val colorValue: Int
        get() = R.color.hourglass_light_green
    override val accentColorValue: Int
        get() = R.color.hourglass_dark_green
}

data object SoloGameModeNavigationConfig : GameModeNavigationConfig {
    override val displayName: String = "Solo"
    override val navigationName: String = "solo"
    override val enabled: Boolean = false
    override val colorValue: Int
        get() = R.color.hourglass_light_blue
    override val accentColorValue: Int
        get() = R.color.hourglass_dark_blue
}

data object ParallelGameModeNavigationConfig : GameModeNavigationConfig {
    override val displayName: String = "Simultaneous"
    override val navigationName: String = "parallel"
    override val enabled: Boolean = false
    override val colorValue: Int
        get() = R.color.hourglass_light_yellow
    override val accentColorValue: Int
        get() = R.color.hourglass_dark_yellow
}