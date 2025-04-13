package com.etds.hourglass.ui.presentation.gameview

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.etds.hourglass.R
import com.etds.hourglass.ui.presentation.buzzer_mode.BuzzerModeSettingsPage
import com.etds.hourglass.ui.presentation.settings.SettingNumericInputCell
import com.etds.hourglass.ui.presentation.settings.SettingPage
import com.etds.hourglass.ui.presentation.settings.SettingSection
import com.etds.hourglass.ui.presentation.settings.SettingToggleCell
import com.etds.hourglass.ui.presentation.settings.SettingsPlayerList
import com.etds.hourglass.ui.viewmodel.MockBuzzerModeViewModel
import com.etds.hourglass.ui.viewmodel.SequentialModeViewModel

@Composable
fun SequentialModeSettingsPage(
    viewModel: SequentialModeViewModel = hiltViewModel<SequentialModeViewModel>(),
    onGameViewNavigate: () -> Unit = {}
) {

    LaunchedEffect(Unit) {
        viewModel.onInitialize()
    }

    var presetDialogOpen by remember { mutableStateOf(false) }
    var presetNameDialogOpen by remember { mutableStateOf(false) }
    if (presetDialogOpen) {
        val settingPresetNames by viewModel.settingPresetNames.collectAsState()
        val defaultPresetName by viewModel.defaultSettingPresetName.collectAsState()
        ChoosePresetDialog(
            presetList = settingPresetNames,
            onPresetSelected = { viewModel.onSelectPreset(it) },
            onPresetDefaultSelected = { viewModel.onSetDefaultPreset(it) },
            onDeletePreset = { viewModel.onDeletePreset(it) },
            onDismiss = { presetDialogOpen = false },
            defaultPresetName = defaultPresetName
        )
    }

    if (presetNameDialogOpen) {
        EnterPresetNameDialog(
            onDismiss = {
                presetNameDialogOpen = false
            },
            onSave = { presetName, makeDefault ->
                viewModel.onSavePreset(presetName, makeDefault)
            }
        )
    }


    SettingPage(
        pageName = "Buzzer Mode Settings"
    ) {
        SettingSection(
            sectionName = "Players"
        ) {
            val players by viewModel.players.collectAsState()
            SettingsPlayerList(
                players = players,
                onPlayerNameEdited = { player, name -> viewModel.updatePlayerName(player, name) },
                editablePlayerName = true,
                reorderable = true,
                onReorder = { to, from ->
                    viewModel.reorderPlayers(to, from)
                }
            )
        }

        val scrollState = rememberScrollState()
        Column(
            modifier = Modifier
                .weight(1F)
                .fillMaxSize()
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SettingSection(
                sectionName = "Presets",
            ) {
                Surface {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            shape = RoundedCornerShape(8.dp),
                            onClick = {
                                presetDialogOpen = true
                            }
                        ) {
                            Text(
                                text = "Select Setting Preset"
                            )
                        }
                        Spacer(Modifier.padding(4.dp))
                        Button(
                            modifier = Modifier
                                .wrapContentSize(),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(0.dp),
                            onClick = {
                                presetNameDialogOpen = true
                            }
                        ) {
                            Icon(
                                modifier = Modifier.size(24.dp),
                                imageVector = Icons.Default.Save,
                                contentDescription = "Save"
                            )
                        }
                    }
                }
            }

            SettingSection(
                sectionName = "Timer Settings"
            ) {
                val autoStartTurnTimer by viewModel.autoStartTurnTimer.collectAsState()
                val turnTimerDuration by viewModel.turnTimerDuration.collectAsState()

                SettingToggleCell(
                    settingName = "Auto Start Buzz Timer",
                    value = autoStartTurnTimer,
                    onToggleChange = { value: Boolean ->
                        viewModel.setAutoEnforceTurnTimer(value)
                    }
                )

                SettingNumericInputCell(
                    settingName = "Buzz Timer Duration (s)",
                    value = turnTimerDuration / 1000.0,
                    onNumericChange = { value: Number? ->
                        viewModel.setTurnTimerDuration(value)
                    }
                )

                HorizontalDivider(Modifier.padding(horizontal = 36.dp), color = Color.DarkGray)

                val autoStartAnswerTimer by viewModel.autoStartTotalTurnTimer.collectAsState()
                val answerTimerDuration by viewModel.turnTimerDuration.collectAsState()

                SettingToggleCell(
                    settingName = "Auto Start Answer Timer",
                    value = autoStartAnswerTimer,
                    onToggleChange = { value: Boolean ->
                        viewModel.setAutoEnforceTotalTurnTimer(value)
                    },
                )

                SettingNumericInputCell(
                    settingName = "Answer Timer Duration (s)",
                    value = answerTimerDuration / 1000.0,
                    onNumericChange = { value: Number? ->
                        viewModel.setTotalTurnTimerDuration(value)
                    }
                )
            }
        }
        Button(
            modifier = Modifier
                .padding(vertical = 12.dp),
            shape = RoundedCornerShape(8.dp),
            onClick = {
                onGameViewNavigate()
            }
        ) {
            Text(text = "Return to Game View")
        }
    }
}

@Composable
fun ChoosePresetDialog(
    presetList: List<String>,
    onPresetSelected: (String) -> Unit = {},
    onPresetDefaultSelected: (String) -> Unit = {},
    onDeletePreset: (String) -> Unit = {},
    onDismiss: () -> Unit = {},
    defaultPresetName: String? = ""
) {
    Dialog(
        onDismissRequest = onDismiss
    ) {
        Surface(
            modifier = Modifier
                .width(240.dp)
                .clip(RoundedCornerShape(8.dp))
        ) {
            Column {
                Text(
                    "Select a Preset",
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.padding(vertical = 8.dp))
                LazyColumn {
                    items(presetList) { presetName ->

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onPresetSelected(presetName)
                                    onDismiss()
                                }
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = presetName
                            )
                            Spacer(modifier = Modifier.weight(1F))
                            Checkbox(
                                checked = presetName == defaultPresetName,
                                onCheckedChange = {
                                    onPresetDefaultSelected(presetName)
                                }
                            )
                            Button(
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.Transparent,
                                    contentColor = colorResource(R.color.hourglass_dark_red)
                                ),
                                contentPadding = PaddingValues(4.dp),
                                modifier = Modifier
                                    .defaultMinSize(minWidth = 1.dp, minHeight = 1.dp),
                                onClick = {
                                    onDeletePreset(presetName)
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete"
                                )
                            }
                        }
                        HorizontalDivider()
                    }
                }
            }
        }

    }
}

@Composable
fun EnterPresetNameDialog(
    onDismiss: () -> Unit = {},
    onSave: (String, Boolean) -> Unit = { _: String, _: Boolean -> }
) {
    var name by remember { mutableStateOf("") }
    var checked by remember { mutableStateOf(false) }
    Dialog(
        onDismissRequest = onDismiss
    ) {
        Surface(
            modifier = Modifier
                .width(240.dp)
                .clip(RoundedCornerShape(8.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        "Enter Preset Name",
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(
                        Modifier
                            .fillMaxWidth()
                            .weight(1F)
                    )
                    Text("Default:")
                    Checkbox(
                        modifier = Modifier.size(28.dp),
                        checked = checked,
                        onCheckedChange = {
                            checked = !checked
                        },
                    )
                }
                Spacer(Modifier.padding(8.dp))
                TextField(
                    value = name,
                    onValueChange = {
                        if (it.length > 12) {
                            return@TextField
                        }
                        name = it
                    },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .width(200.dp),
                )
                Spacer(Modifier.padding(4.dp))
                Button(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentSize(),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(0.dp),
                    onClick = {
                        onSave(name, checked)
                        onDismiss()
                    }
                ) {
                    Text(
                        "Save"
                    )
                }
            }
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