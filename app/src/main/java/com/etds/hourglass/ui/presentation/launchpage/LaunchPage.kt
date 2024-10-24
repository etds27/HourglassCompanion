package com.etds.hourglass.ui.presentation.launchpage

import android.content.Context
import android.content.Intent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.etds.hourglass.model.Device.GameDevice
import com.etds.hourglass.ui.presentation.gameview.GameActivity
import com.etds.hourglass.ui.viewmodel.GameDeviceViewModel
import com.etds.hourglass.ui.viewmodel.GameDeviceViewModelFactory

@Composable
fun LaunchPage(
    context: Context
) {
    val gameDeviceViewModel: GameDeviceViewModel = viewModel(
        factory = GameDeviceViewModelFactory(context)
    )
    val deviceList by gameDeviceViewModel.currentDevices.collectAsState()
    val connectedDeviceList by gameDeviceViewModel.connectedDevices.collectAsState()
    val isSearching by gameDeviceViewModel.isSearching.collectAsState()
    val autoConnectEnabled by gameDeviceViewModel.autoConnectEnabled.collectAsState()
    val localDeviceList by gameDeviceViewModel.localDevices.collectAsState()
    val localContext = LocalContext.current

    val searchingView = isSearching || deviceList.isNotEmpty() || connectedDeviceList.isNotEmpty()

    val searchingPagePercent by animateFloatAsState(
        targetValue = if (searchingView) 1F else 0F,
        label = "Searching Page Contents",
        animationSpec = tween(1000)
    )


    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    )
    {
        if (searchingView) {
            Column(
                modifier = Modifier.alpha(searchingPagePercent),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.weight(1F))
                Button(
                    enabled = connectedDeviceList.isNotEmpty(),
                    onClick = {
                        // Create an Intent to open SecondActivity
                        val intent = Intent(localContext, GameActivity::class.java)
                        localContext.startActivity(intent)
                    }
                ) {
                    Text("Start Game")
                }
                Button(
                    onClick = {
                        gameDeviceViewModel.stopSearching()
                    }
                ) {
                    Text("Stop Search")
                }
                Spacer(Modifier.padding(vertical = 24.dp))

                Row {
                    Text("BLE Devices")
                    Checkbox(
                        checked = autoConnectEnabled,
                        onCheckedChange = { gameDeviceViewModel.toggleAutoConnect() }
                    )
                }
                DeviceList(
                    gameDeviceViewModel = gameDeviceViewModel,
                    deviceList = deviceList
                )
                Spacer(Modifier.padding(vertical = 24.dp))
                Text("Local Devices")

                DeviceList(
                    gameDeviceViewModel = gameDeviceViewModel,
                    deviceList = localDeviceList
                )
                Spacer(modifier = Modifier.weight(1F))

            }
            // Spacer(Modifier.weight(1F))
        } else {
            // Spacer(Modifier.weight(2 - searchingPagePercent))
            Button(
                modifier = Modifier
                    .padding(48.dp)
                    .weight(1f)
                    .aspectRatio(1F)
                    .animateContentSize()
                    .alpha(1 - searchingPagePercent),

                onClick = {
                    gameDeviceViewModel.fetchGameDevices()
                },
                shape = RoundedCornerShape(30.dp)
            ) {
                Text("Start Search")
            }
        }
        // Spacer(Modifier.weight(2 - searchingPagePercent))
    }
}

@Composable
fun DeviceList(
    gameDeviceViewModel: GameDeviceViewModel,
    deviceList: List<GameDevice>
) {
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
    ) {
        items(deviceList) { device ->
            DeviceListItem(
                gameDeviceViewModel = gameDeviceViewModel,
                device = device
            )
        }
    }
}

@Composable
fun DeviceListItem(
    gameDeviceViewModel: GameDeviceViewModel,
    device: GameDevice
) {
    val connected by device.connected.collectAsState()
    val connecting by device.connecting.collectAsState()


    Row(
        modifier = Modifier
            .padding(horizontal = 32.dp, vertical = 8.dp)
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                gameDeviceViewModel.toggleDeviceConnection(device)
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = device.name,
            fontSize = 24.sp
        )
        Spacer(modifier = Modifier.padding(10.dp))

        Text(device.address)
        Spacer(modifier = Modifier
            .weight(1f)
            .fillMaxWidth())
        Icon(
            imageVector = Icons.Default.Radar,
            contentDescription = "Connecting",
            modifier = Modifier
                .alpha(if (connecting) 1F else 0F)
        )
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


@Composable
fun LaunchPreview(context: Context) {
    Surface(color = Color.White) {
        LaunchPage(context)
    }
}
