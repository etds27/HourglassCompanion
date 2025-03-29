package com.etds.hourglass.ui.presentation.buzzer_mode

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.etds.hourglass.ui.presentation.common.SettingNumericInputAndToggleCell
import com.etds.hourglass.ui.presentation.common.SettingPage
import com.etds.hourglass.ui.presentation.common.SettingSection
import com.etds.hourglass.ui.presentation.common.SettingToggleCell

@Preview
@Composable
fun BuzzerModeSettingsPage() {
    SettingPage(
        pageName = "Buzzer Mode Settings"
    ) {
        SettingSection(
            sectionName = "Timer Settings"
        ) {
            SettingNumericInputAndToggleCell(
                settingName = "Enable Buzz Timer (s)",
                toggleValue = true,
                numericValue = 60
            )
            HorizontalDivider(Modifier.padding(horizontal = 36.dp), color = Color.DarkGray)
            SettingNumericInputAndToggleCell(
                settingName = "Enable Answer Timer (s)",
                toggleValue = true,
                numericValue = 60
            )
        }

        SettingSection(
            sectionName = "Answer Settings"
        ) {
            SettingToggleCell(
                settingName = "Allow Immediate Answers"
            )
            HorizontalDivider(Modifier.padding(horizontal = 36.dp), color = Color.DarkGray)
            SettingToggleCell(
                settingName = "Allow Followup Answers"
            )
            HorizontalDivider(Modifier.padding(horizontal = 36.dp), color = Color.DarkGray)
            SettingToggleCell(
                settingName = "Allow Multiple Answers From Same User"
            )
        }
    }
}