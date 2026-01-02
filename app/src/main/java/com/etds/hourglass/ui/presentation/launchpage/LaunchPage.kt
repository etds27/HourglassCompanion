package com.etds.hourglass.ui.presentation.launchpage

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BluetoothDisabled
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EditOff
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.ImageShader
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.layout.ModifierLocalBeyondBoundsLayout
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.room.ColumnInfo
import androidx.room.util.TableInfo
import com.etds.hourglass.R
import com.etds.hourglass.model.Device.DeviceConnectionState
import com.etds.hourglass.model.Device.GameDevice
import com.etds.hourglass.model.Device.LocalDevice
import com.etds.hourglass.model.config.ColorConfig
import com.etds.hourglass.ui.viewmodel.GameDeviceViewModel
import com.etds.hourglass.ui.viewmodel.GameDeviceViewModelProtocol
import com.etds.hourglass.ui.viewmodel.MockGameDeviceViewModel
import org.intellij.lang.annotations.JdkConstants

@Composable
fun LaunchPage(
    onNavigateToGameSelection: () -> Unit,
    onNavigateToEditDevice: (GameDevice) -> Unit,
    gameDeviceViewModel: GameDeviceViewModelProtocol = hiltViewModel<GameDeviceViewModel>(),
) {
    val deviceList by gameDeviceViewModel.currentDevices.collectAsState()
    val isSearching by gameDeviceViewModel.isSearching.collectAsState()

    val searchingView = isSearching
    val readyToStart by gameDeviceViewModel.readyToStart.collectAsState()
    val searchingPagePercent by animateFloatAsState(
        targetValue = if (searchingView) 1F else 0F,
        label = "Searching Page Contents",
        animationSpec = tween(1000)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    )
    {
        TitleView(
            modifier = Modifier
                .fillMaxSize()
                .weight(1F)
        )

        BluetoothDeviceView(
            viewModel = gameDeviceViewModel,
            onNavigateToEditDevice,
            modifier = Modifier
                .fillMaxSize()
                .weight(3F)
        )
        LocalDeviceView(
            viewModel = gameDeviceViewModel,
            modifier = Modifier
        )

        Column(
            modifier = Modifier
                .fillMaxWidth(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                modifier = Modifier
                    .height(128.dp)
                    .fillMaxWidth()
                    .padding(20.dp),
                enabled = readyToStart,
                shape = RoundedCornerShape(16.dp),
                onClick = {
                    gameDeviceViewModel.addLocalPlayers()
                    onNavigateToGameSelection()
                }
            ) {
                Text("Select Game Mode")
            }
        }
    }
}

@Composable
fun LocalDeviceView(
    viewModel: GameDeviceViewModelProtocol,
    modifier: Modifier
) {
    Surface(
        modifier = Modifier
            .padding(8.dp)
            .clip(RoundedCornerShape(8.dp))
            .then(modifier)
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            verticalArrangement = Arrangement.Top
        ) {
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                HeaderTitle("Local Devices")
            }

            LocalDeviceList(
                gameDeviceViewModel = viewModel,
            )
        }
    }
}

@Composable
fun BluetoothDeviceView(
    viewModel: GameDeviceViewModelProtocol,
    onNavigateToEditDevice: (GameDevice) -> Unit,
    modifier: Modifier
) {
    val deviceList by viewModel.currentDevices.collectAsState()
    Surface(
        modifier = Modifier
            .padding(8.dp)
            .clip(RoundedCornerShape(8.dp))
            .then(modifier)
    ) {
        Column(
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(8.dp),
        ) {
            DeviceListHeader(
                gameDeviceViewModel = viewModel,
                headerText = "Remote Devices",
                autoConnectButton = true
            )
            Spacer(Modifier.padding(4.dp))
            DeviceList(
                gameDeviceViewModel = viewModel,
                onNavigateToEditDevice = onNavigateToEditDevice,
                deviceList = deviceList
            )
            Spacer(
                Modifier
                    .fillMaxSize()
                    .weight(1F))
            val isSearching by viewModel.isSearching.collectAsState()

            Button(
                onClick = {
                    if (isSearching) {
                        viewModel.stopSearching()
                    } else {
                        viewModel.startBLESearch()
                    }
                },
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = if (isSearching) Icons.Default.BluetoothDisabled else Icons.Default.Bluetooth,
                    contentDescription = if (isSearching) "Stop Search" else "Start Search"
                )
                Spacer(Modifier.padding(horizontal = 8.dp, vertical = 8.dp))
                Text(if (isSearching) "Stop Search" else "Start Search")
            }
        }
    }
}

@Composable
fun TitleView(
    modifier: Modifier = Modifier
) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .then(modifier),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        val shaderBrush =
            ShaderBrush(ImageShader(ImageBitmap.imageResource(R.drawable.title_background)))

        Box(
            modifier = Modifier,
            contentAlignment = Alignment.CenterEnd
        ) {
            // Draw the text multiple times with slight offsets for stroke
            val strokeWidth = 4F
            val offsets = listOf(
                Offset(-strokeWidth, -strokeWidth),
                Offset(-strokeWidth, strokeWidth),
                Offset(strokeWidth, -strokeWidth),
                Offset(strokeWidth, strokeWidth),
            )
            val fontSize = 54.sp

            offsets.forEach { offset ->
                Text(
                    text = "Hourglass",
                    style = TextStyle(
                        color = Color.Black,
                        fontSize = fontSize,
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier
                        .offset { IntOffset(offset.x.toInt(), offset.y.toInt()) }
                )
            }


            Text(
                text = "Hourglass",
                style = TextStyle(
                    brush = shaderBrush,
                    fontSize = fontSize,
                    fontWeight = FontWeight.Bold
                ),
            )
        }
        Spacer(Modifier.padding(8.dp))
        val icon =
            if (isSystemInDarkTheme()) R.drawable.hourglass_dark_light_hg else R.drawable.hourglass_light_black_hg
        Icon(
            painter = painterResource(icon),
            contentDescription = "Hourglass Logo",
            tint = Color.Unspecified,
            modifier = Modifier
                .size(64.dp)
                .padding(0.dp)
        )
    }
}


@Composable
fun DeviceList(
    gameDeviceViewModel: GameDeviceViewModelProtocol,
    onNavigateToEditDevice: (GameDevice) -> Unit,
    deviceList: List<GameDevice>
) {
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items(deviceList) { device ->
            DeviceListItem(
                gameDeviceViewModel = gameDeviceViewModel,
                onNavigateToEditDevice = onNavigateToEditDevice,
                device = device
            )
        }
    }
}

@Composable
fun LocalDeviceList(
    gameDeviceViewModel: GameDeviceViewModelProtocol,
) {
    val deviceCount by gameDeviceViewModel.localDevicesCount.collectAsState()

    Row(
        modifier = Modifier
            .padding(horizontal = 14.dp, vertical = 8.dp)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        // DeviceNameText(localDevice)
        // DeviceAddressText(localDevice)
        //        Spacer(
        //            modifier = Modifier
        //                .padding(16.dp)
        //                .weight(1f)
        //                .fillMaxWidth()
        //        )
        val buttonWeight = 2f
        val spacerWeight = 1f
        Spacer(Modifier.weight(spacerWeight))
        Button(
            enabled = deviceCount >= 1,
            modifier = Modifier
                .padding(8.dp)
                .weight(buttonWeight),
            shape = RoundedCornerShape(8.dp),
            onClick = { gameDeviceViewModel.removeLocalPlayer() },
            contentPadding = PaddingValues(0.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Remove,
                contentDescription = "Remove Local Player",
            )
        }
        Spacer(Modifier.weight(spacerWeight))

        Text(
            text = deviceCount.toString(),
            fontSize = 24.sp
        )
        Spacer(Modifier.weight(spacerWeight))
        Button(
            enabled = deviceCount <= 7,
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth()
                .weight(buttonWeight),
            shape = RoundedCornerShape(8.dp),
            onClick = { gameDeviceViewModel.addLocalPlayer() },
            contentPadding = PaddingValues(0.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add Local Player",
            )
        }
        Spacer(Modifier.weight(spacerWeight))

    }
}

@Composable
fun DeviceListHeader(
    gameDeviceViewModel: GameDeviceViewModelProtocol,
    headerText: String,
    autoConnectButton: Boolean = false,
    center: Boolean = false
) {
    val autoConnectEnabled by gameDeviceViewModel.autoConnectEnabled.collectAsState()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = if (center) Arrangement.Center else Arrangement.SpaceBetween
    ) {
        HeaderTitle(headerText)
        Spacer(
            Modifier
                .fillMaxWidth()
                .weight(1f)
        )

        if (autoConnectButton) {
            Text(
                "Auto Connect",
                fontSize = 12.sp,
            )
            Spacer(Modifier.padding(2.dp))
            Checkbox(
                modifier = Modifier.size(24.dp),
                checked = autoConnectEnabled,
                onCheckedChange = { gameDeviceViewModel.toggleAutoConnect() },
            )
        }
    }
}

@Composable
fun RowScope.HeaderTitle(
    headerText: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = headerText,
        color = MaterialTheme.colorScheme.onBackground,
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold,
        style = TextStyle(
            textDecoration = TextDecoration.Underline
        ),
        modifier = modifier
    )
}


@Composable
fun DeviceListItem(
    gameDeviceViewModel: GameDeviceViewModelProtocol,
    onNavigateToEditDevice: (GameDevice) -> Unit,
    device: GameDevice
) {
    val connectionState by device.connectionState.collectAsState()
    val connected = connectionState == DeviceConnectionState.Connected
    val connecting = connectionState == DeviceConnectionState.Connecting

    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth()
                .height(IntrinsicSize.Min)
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    gameDeviceViewModel.toggleDeviceConnection(device)
                },
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxHeight()
            ) {
                DeviceNameText(device, modifier = Modifier)
            }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                .fillMaxSize()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End
                ) {
                    DeviceAddressText(device, modifier = Modifier)
                }
                Spacer(modifier = Modifier.padding(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (connected) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Device",
                            modifier = Modifier.clickable {
                                onNavigateToEditDevice(device)
                            }
                        )
                    }

                    if (connecting) {
                        Icon(
                            imageVector = Icons.Default.Radar,
                            contentDescription = "Connecting",
                            modifier = Modifier
                        )
                    }
                    if (connected) {
                        Icon(
                            imageVector = Icons.Default.CheckCircleOutline,
                            contentDescription = "Device connected",
                            modifier = Modifier
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Outlined.Circle,
                            contentDescription = "Device not connected",
                            modifier = Modifier
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ColumnScope.DeviceNameText(
    device: GameDevice,
    modifier: Modifier = Modifier
) {
    Text(
        text = device.name.collectAsState().value,
        modifier = modifier,
        fontSize = 24.sp
    )
}

@Composable
fun RowScope.DeviceNameText(
    device: GameDevice,
    modifier: Modifier = Modifier
) {
    Text(
        text = device.name.collectAsState().value,
        modifier = modifier,
        fontSize = 24.sp
    )
}

@Composable
fun DeviceAddressText(
    device: GameDevice,
    modifier: Modifier = Modifier
) {
    Text(device.address, modifier = modifier)
}

@Preview
@Composable
fun LaunchPagePreview() {
    LaunchPage(
        gameDeviceViewModel = MockGameDeviceViewModel(),
        onNavigateToGameSelection = {},
        onNavigateToEditDevice = {}
    )
}
