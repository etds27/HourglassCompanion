package com.etds.hourglass.ui.presentation.device_personalization

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ModifierLocalBeyondBoundsLayout
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.room.util.TableInfo
import com.etds.hourglass.model.Device.DeviceConnectionState
import com.etds.hourglass.model.Device.LocalDevice
import com.etds.hourglass.model.DeviceState.DeviceState
import com.etds.hourglass.model.DeviceState.displayColorCount
import com.etds.hourglass.model.DeviceState.displayName
import com.etds.hourglass.ui.presentation.common.HourglassComposable
import com.etds.hourglass.ui.viewmodel.DevicePersonalizationViewModel
import com.etds.hourglass.ui.viewmodel.DevicePersonalizationViewModelProtocol
import com.etds.hourglass.ui.viewmodel.MockDevicePersonalizationViewModel
import com.github.skydoves.colorpicker.compose.BrightnessSlider
import com.github.skydoves.colorpicker.compose.ColorEnvelope
import com.github.skydoves.colorpicker.compose.HsvColorPicker
import com.github.skydoves.colorpicker.compose.rememberColorPickerController

@Composable
fun DevicePersonalizationView(
    onNavigateToLaunchPage: () -> Unit,
    devicePersonalizationViewModel: DevicePersonalizationViewModelProtocol = hiltViewModel<DevicePersonalizationViewModel>(),
) {
    val deviceName = devicePersonalizationViewModel.deviceName.collectAsState()
    val connectionState by devicePersonalizationViewModel.deviceConnectionState.collectAsState()
    val deviceColorConfig by devicePersonalizationViewModel.deviceColorConfig.collectAsState()
    val deviceConfigState by devicePersonalizationViewModel.deviceConfigState.collectAsState()
    // val deviceConfigState = DeviceState.DeviceLEDOffsetMode
    val ledOffset by devicePersonalizationViewModel.ledOffset.collectAsState()
    val ledCount by devicePersonalizationViewModel.ledCount.collectAsState()

    // val deviceConfigState = DeviceState.AwaitingTurn
    // dialog visibility + which color type is being edited
    val (isDialogOpen, setDialogOpen) = remember { mutableStateOf(false) }
    val (editingColorIndex, setEditingColor) = remember { mutableIntStateOf(0) }
    val isLoadingConfig by devicePersonalizationViewModel.isLoadingConfig.collectAsState()
    // val isLoadingConfig = false

    var displayLEDOffset = remember { mutableIntStateOf(0) }

    val focusManager = LocalFocusManager.current

    LaunchedEffect(Unit) {
        devicePersonalizationViewModel.onNavigate()
    }

    LaunchedEffect(connectionState) {
        if (connectionState == DeviceConnectionState.Disconnected) {
            devicePersonalizationViewModel.resetDeviceProperties()
            devicePersonalizationViewModel.onNavigateToLaunchPage()
            onNavigateToLaunchPage()
        }
    }

    LaunchedEffect(isLoadingConfig) {
        displayLEDOffset.intValue = 0
    }

    Surface {

        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize()
        ) {
            Row(
                modifier = Modifier
                    .height(48.dp)
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceDim),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = {
                        devicePersonalizationViewModel.resetDeviceProperties()
                        devicePersonalizationViewModel.onNavigateToLaunchPage()
                        onNavigateToLaunchPage()
                    }
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back"
                    )
                }

                val hasChanged by devicePersonalizationViewModel.personalizationHasChanged.collectAsState()

                if (hasChanged) {
                    TextButton(
                        onClick = {
                            devicePersonalizationViewModel.resetDeviceProperties()
                        },
                        enabled = !isLoadingConfig
                    ) {
                        Icon(imageVector = Icons.Default.ClearAll, contentDescription = "Back")
                    }
                }

                if (hasChanged) {
                    TextButton(
                        onClick = {
                            devicePersonalizationViewModel.updateDeviceProperties()
                            // onNavigateToLaunchPage()
                        },
                        enabled = !isLoadingConfig
                    ) {
                        Icon(imageVector = Icons.Default.Save, contentDescription = "Back")
                    }
                }

            }
            Spacer(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(0.25f)
            )

            val editingName by devicePersonalizationViewModel.editingDeviceName.collectAsState()
            BasicTextField(
                value = editingName,
                onValueChange = {
                    devicePersonalizationViewModel.setEditingDeviceName(it)
                },
                modifier = Modifier,
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        devicePersonalizationViewModel.setDeviceName(editingName)
                        focusManager.clearFocus()
                    }
                ),
                textStyle = TextStyle.Default.copy(
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 48.sp,
                    textAlign = TextAlign.Center
                )
            )

            val numOfColors = deviceConfigState.displayColorCount()
            val colors: List<Color>
            if (deviceConfigState == DeviceState.DeviceLEDOffsetMode) {
                colors = listOf(Color.Red, Color.Black, Color.Black, Color.Blue)
            } else {
                colors = deviceColorConfig.colors.toList().subList(0, numOfColors)
            }

            HourglassComposable(
                modifier = Modifier
                    .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp)
                    .weight(6f)
                    .aspectRatio(1f),
                circles = ledCount,
                colors = colors,
                paused = !isLoadingConfig,
                offset = displayLEDOffset.intValue,
                resetTrigger = isLoadingConfig // This will reset the rotation whenever loading starts or completes
            )


            // State Selector
            var expanded by remember { mutableStateOf(false) }
            val configs = DeviceState.entries.filter { config ->
                config.displayColorCount() > 0
            }.sortedBy { it.displayName() }


            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                Spacer(Modifier.weight(.2f))
                Button(
                    onClick = { expanded = true },
                    enabled = !isLoadingConfig,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(text = deviceConfigState.displayName())
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                    ) {
                        configs.forEach { config ->
                            DropdownMenuItem(
                                text = { Text(text = config.displayName()) },
                                onClick = {
                                    devicePersonalizationViewModel.setDeviceConfigState(config)
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(Modifier.weight(.2f))
            }


            if (deviceConfigState == DeviceState.DeviceLEDOffsetMode) {
                OffsetGrid(
                    offset = ledOffset,
                    displayOffset = displayLEDOffset,
                    count = ledCount,
                    isLoading = isLoadingConfig,
                    onOffsetIncrease = {
                        devicePersonalizationViewModel.increaseLEDOffset()
                    },
                    onOffsetDecrease = {
                        devicePersonalizationViewModel.decreaseLEDOffset()
                    },
                    onCountIncrease = {
                        devicePersonalizationViewModel.increaseLEDCount()
                    },
                    onCountDecrease = {
                        devicePersonalizationViewModel.decreaseLEDCount()
                    },

                    modifier = Modifier.weight(6f)
                )

            } else {
                ColorGrid(
                    colors = deviceColorConfig.colors.subList(0, numOfColors),
                    isLoading = isLoadingConfig,
                    setDialogOpen = setDialogOpen,
                    setEditingColor = setEditingColor,
                    modifier = Modifier.weight(6f)
                )
            }

            Spacer(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(0.2f)
            )
        }
    }

    // Dialog containing ColorPicker
    if (isDialogOpen) {
        androidx.compose.ui.window.Dialog(
            onDismissRequest = { setDialogOpen(false) }
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(16.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Pick Color #${editingColorIndex + 1}",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Spacer(Modifier.height(16.dp))

                    val colorPickerController = rememberColorPickerController()

                    var editingColor by remember { mutableStateOf("#000000") }

                    HsvColorPicker(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        controller = colorPickerController,
                        onColorChanged = { colorEnvelope ->
                            editingColor = colorEnvelopeToString(colorEnvelope)
                            devicePersonalizationViewModel.setDeviceConfigColor(
                                colorEnvelope.color,
                                editingColorIndex
                            )
                        },
                        initialColor = deviceColorConfig.colors[editingColorIndex],
                    )

                    Column(modifier = Modifier.padding(top = 16.dp)) {
                        BrightnessSlider(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(30.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            controller = colorPickerController
                        )
                    }

                    Spacer(Modifier.height(32.dp))


                    BasicTextField(
                        value = editingColor,
                        onValueChange = { newValue ->
                            editingColor = newValue.uppercase()

                            // If matches #RRGGBB, update selectedColor
                            if (newValue.matches(Regex("^#[0-9A-Fa-f]{6}$"))) {
                                val hex = newValue.removePrefix("#")
                                val colorInt = hex.toLong(16).toInt() or (0xFF shl 24) // add alpha
                                val newColor = Color(colorInt)
                                colorPickerController.selectByColor(newColor, fromUser = true)

                                devicePersonalizationViewModel.setDeviceConfigColor(
                                    newColor,
                                    editingColorIndex
                                )
                            }
                        },
                        keyboardOptions = KeyboardOptions.Default.copy(
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                focusManager.clearFocus()
                            },
                        ),
                        textStyle = TextStyle.Default.copy(
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 16.sp,
                            textAlign = TextAlign.Center
                        ),
                        decorationBox = { innerTextField ->
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                innerTextField()
                            }

                        }
                    )
                    Spacer(Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
fun OffsetGrid(
    offset: Int,
    displayOffset: MutableState<Int>,
    count: Int,
    isLoading: Boolean,
    onOffsetIncrease: () -> Unit,
    onOffsetDecrease: () -> Unit,
    onCountIncrease: () -> Unit,
    onCountDecrease: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (isLoading) {
        Spacer(modifier = modifier
            .fillMaxHeight())
        return
    }
    Row(
        modifier = modifier
            .fillMaxSize()
            .padding(8.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(Modifier.weight(3f))
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxHeight()
        ) {
            Spacer(modifier= Modifier.weight(0.8f))
            Text(text = "Offset", modifier = Modifier.padding(24.dp))
            Button(onClick = {
                onOffsetIncrease()
                displayOffset.value = (displayOffset.value + 1) % count
            }) {
                Icon(imageVector = Icons.Default.ArrowUpward, contentDescription = "Up")

            }
            Spacer(modifier= Modifier.weight(0.2f))
            Text(text = "${(offset + count) % count}")
            Spacer(modifier= Modifier.weight(0.2f))
            Button(onClick = {
                onOffsetDecrease()
                displayOffset.value = (displayOffset.value - 1) % count
            }) {
                Icon(imageVector = Icons.Default.ArrowDownward, contentDescription = "Down")
            }
            Spacer(modifier= Modifier.weight(0.8f))
        }
        Spacer(Modifier.weight(1f))
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxHeight()
        ) {
            Spacer(modifier= Modifier.weight(0.8f))
            Text(text = "Count", modifier = Modifier.padding(24.dp))
            Button(onClick = {
                onCountIncrease()
            }) {
                Icon(imageVector = Icons.Default.ArrowUpward, contentDescription = "Up")

            }
            Spacer(modifier= Modifier.weight(0.2f))
            Text(text = "$count")
            Spacer(modifier= Modifier.weight(0.2f))
            Button(onClick = {
                onCountDecrease()
            }) {
                Icon(imageVector = Icons.Default.ArrowDownward, contentDescription = "Down")
            }
            Spacer(modifier= Modifier.weight(0.8f))
        }
        Spacer(Modifier.weight(3f))
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ColorGrid(
    colors: List<Color>,
    isLoading: Boolean,
    setDialogOpen: (Boolean) -> Unit,
    setEditingColor: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    if (isLoading) {
        Spacer(modifier = modifier
            .fillMaxHeight())
        return
    }

    FlowRow(
        modifier = modifier
            .fillMaxSize()
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalArrangement = Arrangement.SpaceEvenly,
        maxItemsInEachRow = 2 // up to two buttons per row
    ) {
        colors.take(4).forEachIndexed { index, color ->
            ColorButton(
                color = color,
                text = "Color #${index + 1}",
                editingColorIndex = index,
                setDialogOpen = setDialogOpen,
                setEditingColor = setEditingColor,
                modifier = Modifier
                    .weight(1f)
            )
        }
    }
}

@Composable
fun RowScope.ColorButton(
    color: Color,
    text: String,
    editingColorIndex: Int,
    setDialogOpen: (Boolean) -> Unit,
    setEditingColor: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .weight(1f)
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(
            onClick = {
                setEditingColor(editingColorIndex) // editing accent color
                setDialogOpen(true)
            },
            colors = ButtonDefaults.buttonColors().copy(
                containerColor = color
            ),
            shape = RoundedCornerShape(size = 8.dp),
            modifier = Modifier
                .aspectRatio(1f)
                .border(color = Color.Black, width = 2.dp, shape = RoundedCornerShape(8.dp))
                .weight(1f)
        ) {

        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(text, modifier = Modifier.weight(0.5f))
    }
}

private fun colorEnvelopeToString(color: ColorEnvelope): String {
    return "#${color.hexCode.drop(2).uppercase()}"
}

@Preview
@Composable
fun DevicePersonalizationViewPreview() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .windowInsetsPadding(WindowInsets.systemBars)

    ) {
        DevicePersonalizationView(
            onNavigateToLaunchPage = {},
            devicePersonalizationViewModel = MockDevicePersonalizationViewModel(
                LocalDevice(
                    name = "Mock Device"
                )
            )
        )
    }
}