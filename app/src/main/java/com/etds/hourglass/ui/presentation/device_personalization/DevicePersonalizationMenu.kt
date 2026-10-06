package com.etds.hourglass.ui.presentation.device_personalization

import android.R
import android.graphics.drawable.Icon
import android.view.Surface
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Brightness6
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import com.etds.hourglass.model.Device.LocalDevice
import com.etds.hourglass.ui.presentation.settings.SettingNavigableCell
import com.etds.hourglass.ui.presentation.settings.SettingSection
import com.etds.hourglass.ui.viewmodel.DevicePersonalizationViewModel
import com.etds.hourglass.ui.viewmodel.DevicePersonalizationViewModelProtocol
import com.etds.hourglass.ui.viewmodel.MockDevicePersonalizationViewModel
import kotlin.math.max

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DevicePersonalizationMenu(
    onNavigateToLaunchPage: () -> Unit,
    onNavigateToSettingsPage: (DevicePersonalizationType) -> Unit,
    devicePersonalizationViewModel: DevicePersonalizationViewModelProtocol = hiltViewModel<DevicePersonalizationViewModel>(),

    ) {

    val device = devicePersonalizationViewModel.device
    val deviceName by devicePersonalizationViewModel.deviceName.collectAsState()


    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
                title = {
                    Text(
                        deviceName,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            onNavigateToLaunchPage()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                scrollBehavior = scrollBehavior
            )
        }
    ) { innerPadding ->
        InnerDevicePersonalizationMenu(
            onNavigateToSettingsPage,
            devicePersonalizationViewModel = devicePersonalizationViewModel,
            innerPadding = innerPadding
        )
    }

    Column {

    }
}

@Composable
fun InnerDevicePersonalizationMenu(
    onNavigateToSettingsPage: (DevicePersonalizationType) -> Unit,
    devicePersonalizationViewModel: DevicePersonalizationViewModelProtocol,
    innerPadding: PaddingValues
) {
    Surface(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            SettingSection("") {
                SettingNavigableCell(
                    settingName = "Appearance",
                    icon = Icons.Filled.ColorLens,
                    onClick = {
                        onNavigateToSettingsPage(
                            DevicePersonalizationType.Appearance
                        )
                    }
                )
                SettingNavigableCell(
                    settingName = "Brightness", icon = Icons.Filled.Brightness6,
                    onClick = {
                        onNavigateToSettingsPage(
                            DevicePersonalizationType.Brightness
                        )
                    })
                SettingNavigableCell(
                    settingName = "Orientation", icon = Icons.Filled.Explore,
                    onClick = {
                        onNavigateToSettingsPage(
                            DevicePersonalizationType.Orientation
                        )
                    })
                SettingNavigableCell(
                    settingName = "Name", icon = Icons.Filled.Edit,
                    onClick = {
                        onNavigateToSettingsPage(
                            DevicePersonalizationType.Name
                        )
                    })
                SettingNavigableCell(
                    settingName = "Notification",
                    icon = Icons.Filled.Notifications,
                    onClick = {
                        onNavigateToSettingsPage(
                            DevicePersonalizationType.Notification
                        )
                    }
                )
                SettingNavigableCell(
                    settingName = "Vibration", icon = Icons.Filled.Vibration,
                    onClick = {
                        onNavigateToSettingsPage(
                            DevicePersonalizationType.Vibration
                        )
                    })
            }
        }
    }
}


@Preview
@Composable
fun DevicePersonalizationMenuPreview() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        DevicePersonalizationMenu(
            onNavigateToLaunchPage = {},
            onNavigateToSettingsPage = {},
            devicePersonalizationViewModel = MockDevicePersonalizationViewModel(LocalDevice(name = "Mock Device"))
        )
    }
}