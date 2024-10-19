package com.etds.hourglass.ui.presentation.gameview

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.tooling.preview.Preview
import com.etds.hourglass.R
import com.etds.hourglass.data.BLEData.BLERepository
import com.etds.hourglass.data.BLEData.local.BLELocalDatasource
import com.etds.hourglass.data.BLEData.remote.BLERemoteDatasource
import com.etds.hourglass.data.game.GameRepository
import com.etds.hourglass.data.game.local.LocalGameDatasource
import com.etds.hourglass.ui.viewmodel.GameViewModel

class GameActivity: ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent() {
            GameView()
        }
    }
}

@Preview
@Composable
fun GameView(
    modifier: Modifier = Modifier
) {
    val gameViewModel: GameViewModel = getGameModel()

    Surface(
        color = colorResource(R.color.purple_200),
        modifier = Modifier.fillMaxSize()
    ) {

    }
}

fun getGameModel(): GameViewModel {
    return GameViewModel(
        gameRepository = GameRepository(
            localGameDatasource = LocalGameDatasource()
        ),
        bleDeviceRepository = BLERepository(
            localDatasource = BLELocalDatasource(),
            remoteDatasource = BLERemoteDatasource()
        )
    )
}