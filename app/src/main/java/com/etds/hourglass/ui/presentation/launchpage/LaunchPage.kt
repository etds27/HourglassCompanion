package com.etds.hourglass.ui.presentation.launchpage

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.etds.hourglass.R
import com.etds.hourglass.data.BLEData.BLERepository
import com.etds.hourglass.data.BLEData.local.BLELocalDatasource
import com.etds.hourglass.data.BLEData.remote.BLERemoteDatasource
import com.etds.hourglass.data.game.GameRepository
import com.etds.hourglass.data.game.local.LocalGameDatasource
import com.etds.hourglass.model.Device.GameDevice
import com.etds.hourglass.ui.presentation.gameview.GameActivity
import com.etds.hourglass.ui.viewmodel.GameDeviceViewModel

@Composable
fun LaunchPage(modifier: Modifier = Modifier) {
    val gameDeviceViewModel: GameDeviceViewModel = viewModel()
    val deviceList by gameDeviceViewModel.currentDevices.collectAsState()
    val isSearching by gameDeviceViewModel.isSearching.collectAsState()
    val startingPlayer by gameDeviceViewModel.startingPlayerDevice.collectAsState()
    val localContext = LocalContext.current

    Column(modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally)
    {
        Button(
            modifier = Modifier.padding(48.dp),
            onClick = { gameDeviceViewModel.fetchGameDevices() },
            enabled = !isSearching) {
            val text = if(isSearching) {
                stringResource(R.string.searching)
            } else {
                stringResource(R.string.start_search)
            }
            Text(text)
        }

        Button(
            enabled = deviceList.isNotEmpty(),
            onClick = {
                // Create an Intent to open SecondActivity
                val intent = Intent(localContext, GameActivity::class.java)
                localContext.startActivity(intent)
            }
        ) {
            Text("Start Game")
        }

        DeviceList(
            modifier = modifier,
            gameDeviceViewModel = gameDeviceViewModel,
            deviceList = deviceList,
            startingPlayer = startingPlayer
        )
    }
}

@Composable
fun DeviceList(
    modifier: Modifier = Modifier,
    gameDeviceViewModel: GameDeviceViewModel,
    deviceList: List<GameDevice>,
    startingPlayer: GameDevice? = null,
) {
    LazyColumn {
        items(deviceList) { bleDevice ->
            BLEDeviceListItem(
                gameDeviceViewModel = gameDeviceViewModel,
                bleDevice = bleDevice,
                modifier = Modifier.padding(8.dp),
                selected = bleDevice == startingPlayer
            )
        }
    }
}

@Composable
fun BLEDeviceListItem(
    gameDeviceViewModel: GameDeviceViewModel,
    bleDevice: GameDevice,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
) {
    val color = if(selected) {
        colorResource(R.color.selected_dvice)
    } else {
        colorResource(R.color.base_light)
    }

    Surface(
        color = color,
        modifier = Modifier.clickable {
            gameDeviceViewModel.selectStartingDevice(bleDevice)
        }
    ) {
        Row {
            Text(bleDevice.name)
            Spacer(modifier = Modifier.padding(10.dp))
            Text(bleDevice.address)
        }
    }

}

@Preview
@Composable
fun BLEDevicePreview() {
    val gameDeviceViewModel: GameDeviceViewModel = viewModel()
    val deviceList by gameDeviceViewModel.currentDevices.collectAsState()
    DeviceList(
        deviceList = deviceList,
        gameDeviceViewModel = gameDeviceViewModel
    )
}

@Preview
@Composable
fun LaunchPreview() {
    Surface(color = Color.White) {
        LaunchPage(
            modifier = Modifier
        )
    }
}
