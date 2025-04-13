package com.etds.hourglass.ui.presentation.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.etds.hourglass.R


val pageHeaderFontSize = 32.sp
val sectionHeaderFontSize = 18.sp

val titleTextSize = 14.sp
val titleTextWeight = FontWeight.Normal

val rowModifier = Modifier
    .fillMaxWidth()
    .height(56.dp)
    .padding(horizontal = 8.dp)

val textModifier = Modifier
    .padding(horizontal = 4.dp)


@Composable
fun SettingPage(
    pageName: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp)
                .then(modifier),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                pageName,
                fontSize = pageHeaderFontSize,
                modifier = Modifier
                    .padding(vertical = 8.dp)
                    .fillMaxWidth(),
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            content()
        }
}

@Composable
fun SettingSection(
    sectionName: String,
    content: @Composable ColumnScope.() -> Unit
) {

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 8.dp, end = 8.dp, bottom = 8.dp),
            verticalArrangement = Arrangement.Top
        ) {
            Text(
                sectionName,
                fontSize = sectionHeaderFontSize,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp)),
                // .padding(vertical = 8.dp, horizontal = 8.dp),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {

                content()
            }
        }
    }
}

@Composable
fun SettingCell(settingName: String) {
    Row(
        modifier = rowModifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Text(
            text = settingName,
            modifier = textModifier,
            textAlign = TextAlign.Center,
            fontSize = titleTextSize,
            fontWeight = titleTextWeight
        )
    }
}

@Composable
fun SettingToggleCell(
    settingName: String,
    value: Boolean = false,
    onToggleChange: (Boolean) -> Unit = {}
) {
    Row(
        modifier = rowModifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = settingName,
            modifier = textModifier,
            fontSize = titleTextSize,
            fontWeight = titleTextWeight
        )
        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1.0f)
        )
        Switch(
            checked = value,
            onCheckedChange = onToggleChange
        )
    }
}

@Composable
fun SettingNumericInputCell(
    settingName: String,
    value: Number = 0,
    onNumericChange: (Number?) -> Unit = {}
) {
    val focusManager = LocalFocusManager.current
    Row(
        modifier = rowModifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = settingName,
            modifier = textModifier,
            fontSize = titleTextSize,
            fontWeight = titleTextWeight
        )
        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1.0f)
        )
        BasicTextField(
            value = value.toString(),
            onValueChange = { value: String ->
                onNumericChange(value.toDoubleOrNull())
            },
            modifier = Modifier
                .fillMaxHeight()
                .width(100.dp)
                .padding(4.dp),
            textStyle = TextStyle(
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.background,
                fontSize = 20.sp
            ),
            keyboardActions = KeyboardActions(
                onDone = { focusManager.clearFocus() }
            ),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done
            ),
            decorationBox = { innerTextField ->
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .padding(vertical = 4.dp)
                        .background(MaterialTheme.colorScheme.onBackground),
                ) {
                    innerTextField()
                }
            }
        )
    }
}

@Composable
fun SettingNumericInputAndToggleCell(
    settingName: String,
    toggleValue: Boolean = false,
    numericValue: Number = 0,
    onToggleChange: (Boolean) -> Unit = {},
    onNumericChange: (Number?) -> Unit = {}
) {
    val focusManager = LocalFocusManager.current
    Row(
        modifier = rowModifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = settingName,
            modifier = textModifier,
            fontSize = titleTextSize,
            fontWeight = titleTextWeight
        )
        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1.0f)
        )
        Switch(
            checked = toggleValue,
            onCheckedChange = onToggleChange
        )
        Spacer(Modifier.padding(horizontal = 8.dp))
        BasicTextField(
            value = numericValue.toString(),
            onValueChange = { value: String ->
                onNumericChange(value.toDoubleOrNull())
            },
            modifier = Modifier
                .fillMaxHeight()
                .width(100.dp)
                .padding(4.dp),
            textStyle = TextStyle(
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.background,
                fontSize = 20.sp
            ),
            keyboardActions = KeyboardActions(
                onDone = { focusManager.clearFocus() }
            ),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done
            ),
            decorationBox = { innerTextField ->
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .padding(vertical = 4.dp)
                        .background(MaterialTheme.colorScheme.onBackground),
                ) {
                    innerTextField()
                }
            }
        )
    }
}

@Preview
@Composable
fun TestSettingSection() {

    SettingPage(
        pageName = "Buzzer Mode"
    ) {
        SettingSection(
            sectionName = "Settings",
        ) {
            SettingCell("Game Mode")
            HorizontalDivider(Modifier.padding(horizontal = 36.dp), color = Color.DarkGray)
            SettingToggleCell("Enable Turn Timer", value = true)
            HorizontalDivider(Modifier.padding(horizontal = 36.dp), color = Color.DarkGray)
            SettingNumericInputCell("Turn Timer Duration")
            HorizontalDivider(Modifier.padding(horizontal = 36.dp), color = Color.DarkGray)
            SettingNumericInputAndToggleCell("Combined Timer Duration")
        }
    }
}