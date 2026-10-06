package com.etds.hourglass.ui.presentation.device_personalization

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import com.etds.hourglass.model.Device.DeviceConnectionState
import com.etds.hourglass.model.Device.LocalDevice
import com.etds.hourglass.ui.viewmodel.DevicePersonalizationViewModel
import com.etds.hourglass.ui.viewmodel.DevicePersonalizationViewModelProtocol
import com.etds.hourglass.ui.viewmodel.MockDevicePersonalizationViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DevicePersonalizationSubView(
    onNavigateToLaunchPage: () -> Unit,
    onNavigateToSettingsPage: () -> Unit,
    onSaveAction: () -> Unit,
    onClearAction: () -> Unit,
    canClear: Boolean,
    canSave: Boolean,
    devicePersonalizationViewModel: DevicePersonalizationViewModelProtocol = hiltViewModel<DevicePersonalizationViewModel>(),

    content: @Composable ColumnScope.() -> Unit
) {
    val connectionState by devicePersonalizationViewModel.deviceConnectionState.collectAsState()
    val deviceName by devicePersonalizationViewModel.deviceName.collectAsState()
    // val hasChanged by devicePersonalizationViewModel.personalizationHasChanged.collectAsState()


    // val isLoadingConfig by devicePersonalizationViewModel.isLoadingConfig.collectAsState()
    val isLoadingConfig = false


    LaunchedEffect(Unit) {
        devicePersonalizationViewModel.onNavigate()
    }

    LaunchedEffect(connectionState) {
        if (connectionState == DeviceConnectionState.Disconnected) {
            onClearAction()
            devicePersonalizationViewModel.onNavigateToLaunchPage()
            onNavigateToLaunchPage()
        }
    }

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
                            onNavigateToSettingsPage
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    if (canClear) {
                        TextButton(
                            onClick = {
                                onClearAction()
                            },
                            enabled = !isLoadingConfig
                        ) {
                            Icon(imageVector = Icons.Default.ClearAll, contentDescription = "Clear Changes")
                        }
                    }

                    if (canSave) {
                        TextButton(
                            onClick = {
                                onSaveAction()
                                // devicePersonalizationViewModel.updateDeviceProperties()
                                // onNavigateToLaunchPage()
                            },
                            enabled = !isLoadingConfig
                        ) {
                            Icon(imageVector = Icons.Default.Save, contentDescription = "Save")
                        }
                    }
                },
                scrollBehavior = scrollBehavior
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier.padding(innerPadding)
        ) {
            Surface {
                Column(modifier = Modifier.fillMaxSize()) {
                    content()
                }
            }
        }
    }
}

@Preview
@Composable
fun DevicePersonalizationSubViewPreview() {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        DevicePersonalizationSubView(
            onNavigateToLaunchPage = {},
            onNavigateToSettingsPage = {},
            onSaveAction = {},
            onClearAction = {},
            canSave = true,
            canClear = true,
            devicePersonalizationViewModel = MockDevicePersonalizationViewModel(LocalDevice("Mock Device"))
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Button(onClick = {}) {
                    Text("Test")
                }
            }
        }
    }
}