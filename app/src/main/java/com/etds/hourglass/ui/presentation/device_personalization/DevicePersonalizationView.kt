package com.etds.hourglass.ui.presentation.device_personalization

import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.ColorProducer
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PlatformImeOptions
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.room.util.TableInfo
import com.etds.hourglass.model.Device.LocalDevice
import com.etds.hourglass.ui.presentation.common.HourglassComposable
import com.etds.hourglass.ui.viewmodel.DevicePersonalizationViewModel
import com.etds.hourglass.ui.viewmodel.DevicePersonalizationViewModelProtocol
import com.etds.hourglass.ui.viewmodel.GameDeviceViewModel
import com.etds.hourglass.ui.viewmodel.GameDeviceViewModelProtocol
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
    val deviceColor = devicePersonalizationViewModel.deviceColor.collectAsState()
    val deviceAccentColor = devicePersonalizationViewModel.deviceAccentColor.collectAsState()

    // dialog visibility + which color type is being edited
    val (isDialogOpen, setDialogOpen) = remember { mutableStateOf(false) }
    val (editingAccent, setEditingAccent) = remember { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current

    LaunchedEffect(Unit) {
        devicePersonalizationViewModel.onNavigate()
    }

    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize()
    ) {
        Surface {
            Row(
                modifier = Modifier
                    .height(48.dp)
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.inverseSurface),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = {
                        devicePersonalizationViewModel.resetDeviceProperties()
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
                        }
                    ) {
                        Icon(imageVector = Icons.Default.ClearAll, contentDescription = "Back")
                    }
                }

                TextButton(
                    onClick = {
                        devicePersonalizationViewModel.updateDeviceProperties()
                        onNavigateToLaunchPage()
                    }
                ) {
                    Icon(imageVector = Icons.Default.Save, contentDescription = "Back")
                }

            }
        }
        Spacer(
            modifier = Modifier
                .fillMaxHeight()
                .weight(0.5f)
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
                    // Uncomment this if we want
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

        HourglassComposable(
            modifier = Modifier
                .padding(32.dp),
            colors = listOf(
                deviceAccentColor.value,
                deviceColor.value
            ),
            paused = true,
        )

        Spacer(Modifier.weight(1f))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Spacer(modifier = Modifier.weight(.8f))
            ColorButton(
                color = deviceColor.value,
                text = "Device Color",
                editingAccent = false,
                setDialogOpen = setDialogOpen,
                setEditingAccent = setEditingAccent
            )
            Spacer(modifier = Modifier.weight(.8f))
            ColorButton(
                color = deviceAccentColor.value,
                text = "Accent Color",
                editingAccent = true,
                setDialogOpen = setDialogOpen,
                setEditingAccent = setEditingAccent
            )

            Spacer(modifier = Modifier.weight(.8f))
        }

        Spacer(
            modifier = Modifier
                .fillMaxHeight()
                .weight(0.3f)
        )
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
                        text = if (editingAccent) "Pick Accent Color" else "Pick Device Color",
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

                            if (editingAccent) {
                                devicePersonalizationViewModel.setDeviceAccentColor(colorEnvelope.color)
                            } else {
                                devicePersonalizationViewModel.setDeviceColor(colorEnvelope.color)
                            }
                        },
                        initialColor = if (editingAccent) deviceAccentColor.value else deviceColor.value,
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

                                if (editingAccent) {
                                    devicePersonalizationViewModel.setDeviceAccentColor(newColor)
                                } else {
                                    devicePersonalizationViewModel.setDeviceColor(newColor)
                                }
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
fun RowScope.ColorButton(
    color: Color,
    text: String,
    editingAccent: Boolean,
    setDialogOpen: (Boolean) -> Unit,
    setEditingAccent: (Boolean) -> Unit
) {
    Column(
        modifier = Modifier
            .weight(1f),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(
            onClick = {
                setEditingAccent(editingAccent) // editing accent color
                setDialogOpen(true)
            },
            colors = ButtonDefaults.buttonColors().copy(
                containerColor = color
            ),
            shape = RoundedCornerShape(size = 8.dp),
            modifier = Modifier
                .aspectRatio(1f)
        ) {

        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(text)
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
            devicePersonalizationViewModel = MockDevicePersonalizationViewModel(LocalDevice(
                name = "Mock Device"
            ))
        )
    }
}