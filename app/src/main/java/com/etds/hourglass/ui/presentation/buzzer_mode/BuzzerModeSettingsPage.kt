package com.etds.hourglass.ui.presentation.buzzer_mode

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.etds.hourglass.ui.presentation.common.SettingNumericInputAndToggleCell
import com.etds.hourglass.ui.presentation.common.SettingPage
import com.etds.hourglass.ui.presentation.common.SettingSection
import com.etds.hourglass.ui.presentation.common.SettingToggleCell
import com.etds.hourglass.ui.viewmodel.BuzzerModeViewModel
import com.etds.hourglass.ui.viewmodel.BuzzerModeViewModelProtocol
import com.etds.hourglass.ui.viewmodel.MockBuzzerModeViewModel

@Composable
fun BuzzerModeSettingsPage(
    viewModel: BuzzerModeViewModelProtocol = hiltViewModel()
) {

    SettingPage(
        pageName = "Buzzer Mode Settings"
    ) {
        SettingSection(
            sectionName = "Timer Settings"
        ) {
            val enableBuzzerTimerValue by viewModel.awaitingBuzzTimerEnforced.collectAsState()
            val buzzerTimerDuration by viewModel.awaitingBuzzTimerDuration.collectAsState()
            SettingNumericInputAndToggleCell(
                settingName = "Enable Buzz Timer (s)",
                toggleValue = enableBuzzerTimerValue,
                numericValue = buzzerTimerDuration / 1000.0,
                onToggleChange = { value: Boolean ->
                    viewModel.setAwaitBuzzTimerEnforced(value)
                },
                onNumericChange = { value: Number ->
                    viewModel.setAwaitBuzzTimerDuration(value)
                }
            )
            HorizontalDivider(Modifier.padding(horizontal = 36.dp), color = Color.DarkGray)

            val enableAnswerTimer by viewModel.answerTimerEnforced.collectAsState()
            val answerTimerDuration by viewModel.answerTimerDuration.collectAsState()
            SettingNumericInputAndToggleCell(
                settingName = "Enable Answer Timer (s)",
                toggleValue = enableAnswerTimer,
                numericValue = answerTimerDuration / 1000.0,
                onToggleChange = { value: Boolean ->
                    viewModel.setEnableAnswerTimer(value)
                },
                onNumericChange = { value: Number ->
                    viewModel.setAnswerTimerDuration(value)
                }
            )
        }

        SettingSection(
            sectionName = "Answer Settings"
        ) {
            val allowImmediateAnswers by viewModel.allowImmediateAnswers.collectAsState()
            SettingToggleCell(
                settingName = "Allow Immediate Answers",
                value = allowImmediateAnswers,
                onToggleChange = { value: Boolean ->
                    viewModel.setAllowImmediateAnswers(value)
                }
            )
            HorizontalDivider(Modifier.padding(horizontal = 36.dp), color = Color.DarkGray)

            val allowFollowupAnswers by viewModel.allowFollowupAnswers.collectAsState()
            SettingToggleCell(
                settingName = "Allow Followup Answers",
                value = allowFollowupAnswers,
                onToggleChange = { value: Boolean ->
                    viewModel.setAllowFollowupAnswers(value)
                }
            )
            HorizontalDivider(Modifier.padding(horizontal = 36.dp), color = Color.DarkGray)

            val allowMultipleAnswersFromSameUser by viewModel.allowMultipleAnswersFromSameUser.collectAsState()
            SettingToggleCell(
                settingName = "Allow Multiple Answers From Same User",
                value = allowMultipleAnswersFromSameUser,
                onToggleChange = { value: Boolean ->
                    viewModel.setAllowMultipleAnswersFromSameUser(value)
                }
            )
        }
    }
}

@Preview
@Composable
fun MockBuzzerModeSettingsPage() {
    BuzzerModeSettingsPage(
        viewModel = MockBuzzerModeViewModel()
    )
}