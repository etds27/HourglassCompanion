package com.etds.hourglass.ui.presentation.common

import android.util.Log
import android.widget.ToggleButton
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.etds.hourglass.R


val rowModifier = Modifier
    .fillMaxWidth()
    // .clip(RoundedCornerShape(8.dp))
    .background(Color.LightGray)
    .height(56.dp)
    .padding(horizontal = 8.dp)

val textModifier = Modifier
    .background(Color.Red)
    .padding(horizontal = 4.dp)

val sectionHeaderFontSize = 12.sp
val titleTextSize = 14.sp
val titleTextWeight = FontWeight.Bold

@Composable
fun SettingSection(sectionName: String,
                   content: @Composable ColumnScope.() -> Unit) {

    Column(
        modifier = Modifier.fillMaxWidth()
            .background(Color.DarkGray)
            .padding(horizontal = 8.dp)
    ) {
        Text(sectionName,
            fontSize = sectionHeaderFontSize,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(Color.Gray),
                // .padding(vertical = 8.dp, horizontal = 8.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {

            content()
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
    value: Boolean = false
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
            onCheckedChange = { value: Boolean ->
                Log.d("TEST", value.toString())
            }
        )
    }
}

@Composable
fun SettingNumericInputCell(
    settingName: String,
    value: Int = 0
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
        BasicTextField(
            value = "TEST",
            onValueChange = { value: String ->
                Log.d("TEST", value)
            },
            modifier = Modifier
                .background(Color.Yellow),
            textStyle = TextStyle(
                textAlign = TextAlign.Center
            ),
            decorationBox = { innerTextField ->
                Box(
                        modifier = Modifier
                            .background(
                                color = colorResource(R.color.settings_base_light),
                                RoundedCornerShape(8.dp))
                            .padding(vertical = 4.dp)
                        ){
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
    numericValue: Int = 0
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
            checked = toggleValue,
            onCheckedChange = { value: Boolean ->
                Log.d("TEST", value.toString())
            }
        )
        Spacer(Modifier.padding(horizontal = 8.dp))
        BasicTextField(
            value = "TEST",
            enabled = toggleValue,
            onValueChange = { value: String ->
                Log.d("TEST", value)
            },
            modifier = Modifier
                .background(Color.Yellow),
            textStyle = TextStyle(
                textAlign = TextAlign.Center
            ),
            decorationBox = { innerTextField ->
                Box(
                    modifier = Modifier
                        .background(
                            color = colorResource(R.color.settings_base_light),
                            RoundedCornerShape(8.dp))
                        .padding(vertical = 4.dp)
                ){
                    innerTextField()
                }
            }
        )
    }
}

@Preview
@Composable
fun TestSettingCell() {
    SettingSection (
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