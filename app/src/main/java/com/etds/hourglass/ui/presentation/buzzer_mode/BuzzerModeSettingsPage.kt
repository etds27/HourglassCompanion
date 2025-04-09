package com.etds.hourglass.ui.presentation.buzzer_mode

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.etds.hourglass.ui.presentation.settings.SettingNumericInputCell
import com.etds.hourglass.ui.presentation.settings.SettingPage
import com.etds.hourglass.ui.presentation.settings.SettingSection
import com.etds.hourglass.ui.presentation.settings.SettingToggleCell
import com.etds.hourglass.ui.viewmodel.BuzzerModeViewModel
import com.etds.hourglass.ui.viewmodel.BuzzerModeViewModelProtocol
import com.etds.hourglass.ui.viewmodel.MockBuzzerModeViewModel

@Composable
fun BuzzerModeSettingsPage(
    viewModel: BuzzerModeViewModelProtocol = hiltViewModel<BuzzerModeViewModel>(),
    onGameViewNavigate: () -> Unit = {}
) {

    SettingPage(
        pageName = "Buzzer Mode Settings"
    ) {
        SettingSection(
            sectionName = "Timer Settings"
        ) {
            val autoStartBuzzerTimer by viewModel.autoStartAwaitingBuzzTimer.collectAsState()
            val buzzerTimerDuration by viewModel.awaitingBuzzTimerDuration.collectAsState()
            SettingToggleCell(
                settingName = "Auto Start Buzz Timer",
                value = autoStartBuzzerTimer,
                onToggleChange = { value: Boolean ->
                    viewModel.setAutoStartAwaitingBuzzTimer(value)
                }
            )

            SettingNumericInputCell(
                settingName = "Buzz Timer Duration (s)",
                value = buzzerTimerDuration / 1000.0,
                onNumericChange = { value: Number? ->
                    viewModel.setAwaitBuzzTimerDuration(value)
                }
            )

            HorizontalDivider(Modifier.padding(horizontal = 36.dp), color = Color.DarkGray)

            val autoStartAnswerTimer by viewModel.autoStartAnswerTimer.collectAsState()
            val answerTimerDuration by viewModel.answerTimerDuration.collectAsState()

            SettingToggleCell(
                settingName = "Auto Start Answer Timer",
                value = autoStartAnswerTimer,
                onToggleChange = { value: Boolean ->
                    viewModel.setAutoStartAnswerTimer(value)
                },
            )

            SettingNumericInputCell(
                settingName = "Answer Timer Duration (s)",
                value = answerTimerDuration / 1000.0,
                onNumericChange = { value: Number? ->
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
        Spacer(modifier = Modifier.fillMaxSize().weight(1F))
        Button(
            modifier = Modifier.padding(vertical = 24.dp),
            shape = RoundedCornerShape(8.dp),
            onClick = {
                onGameViewNavigate()
            }
        ) {
            Text(text = "Return to Game View")
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