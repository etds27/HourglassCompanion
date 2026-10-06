package com.etds.hourglass.ui.presentation.device_personalization

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.etds.hourglass.model.Device.LocalDevice
import com.etds.hourglass.model.DeviceState.DeviceState
import com.etds.hourglass.ui.presentation.common.HourglassComposable
import com.etds.hourglass.ui.viewmodel.DevicePersonalizationViewModel
import com.etds.hourglass.ui.viewmodel.DevicePersonalizationViewModelProtocol
import com.etds.hourglass.ui.viewmodel.MockDevicePersonalizationViewModel

@Composable
fun DevicePersonalizationOrientationView(
    onNavigateToLaunchPage: () -> Unit,
    onNavigateToSettingsPage: () -> Unit,
    devicePersonalizationViewModel: DevicePersonalizationViewModelProtocol = hiltViewModel<DevicePersonalizationViewModel>(),
) {
    val orientationHasChanged by devicePersonalizationViewModel.orientationHasChanged.collectAsState()

    // val deviceConfigState = DeviceState.DeviceLEDOffsetMode
    val ledOffset by devicePersonalizationViewModel.ledOffset.collectAsState()
    val ledCount by devicePersonalizationViewModel.ledCount.collectAsState()

    // val isLoadingConfig by devicePersonalizationViewModel.isLoadingConfig.collectAsState()
    val isLoadingConfig = false

    val displayLEDOffset = remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        devicePersonalizationViewModel.setDeviceConfigState(DeviceState.DeviceLEDOffsetMode)
    }

    LaunchedEffect(isLoadingConfig) {
        displayLEDOffset.intValue = 0
    }

    val colors = listOf(Color.Red, Color.Black, Color.Black, Color.Blue)


    DevicePersonalizationSubView(
        onNavigateToLaunchPage = onNavigateToLaunchPage,
        onNavigateToSettingsPage = onNavigateToSettingsPage,
        onSaveAction = {
            devicePersonalizationViewModel.saveDeviceOrientationProperties()
        },
        onClearAction = {
            devicePersonalizationViewModel.resetDeviceOrientationProperties()
        },
        devicePersonalizationViewModel = devicePersonalizationViewModel,
        canClear = orientationHasChanged,
        canSave = orientationHasChanged
    ) {

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

@Preview
@Composable
fun DevicePersonalizationOrientationViewPreview() {
    Column(modifier = Modifier.fillMaxSize()) {
        DevicePersonalizationOrientationView(
            onNavigateToLaunchPage = {},
            onNavigateToSettingsPage = {},
            devicePersonalizationViewModel = MockDevicePersonalizationViewModel(LocalDevice("Mock Device"))
        )
    }
}