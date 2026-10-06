package com.etds.hourglass.ui.presentation.device_personalization

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.etds.hourglass.model.Device.LocalDevice
import com.etds.hourglass.ui.viewmodel.DevicePersonalizationViewModel
import com.etds.hourglass.ui.viewmodel.DevicePersonalizationViewModelProtocol
import com.etds.hourglass.ui.viewmodel.MockDevicePersonalizationViewModel

@Composable
fun DevicePersonalizationNameView(
    onNavigateToLaunchPage: () -> Unit,
    onNavigateToSettingsPage: () -> Unit,
    devicePersonalizationViewModel: DevicePersonalizationViewModelProtocol = hiltViewModel<DevicePersonalizationViewModel>(),
) {
    val focusManager = LocalFocusManager.current

    val nameHasChanged by devicePersonalizationViewModel.nameHasChanged.collectAsState()

    DevicePersonalizationSubView(
        onNavigateToLaunchPage = onNavigateToLaunchPage,
        onNavigateToSettingsPage = onNavigateToSettingsPage,
        onSaveAction = { devicePersonalizationViewModel.saveDeviceNameProperties() },
        onClearAction = { devicePersonalizationViewModel.resetDeviceNameProperties() },
        devicePersonalizationViewModel = devicePersonalizationViewModel,
        canSave = nameHasChanged,
        canClear = nameHasChanged
    ) {
        val editingName by devicePersonalizationViewModel.editingDeviceName.collectAsState()

        Surface {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
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
            }
        }
    }
}

@Preview
@Composable
fun DevicePersonalizationNameViewPreview() {
    DevicePersonalizationNameView(
        onNavigateToLaunchPage = {},
        onNavigateToSettingsPage = {},
        devicePersonalizationViewModel = MockDevicePersonalizationViewModel(LocalDevice("Mock Device"))
    )
}

