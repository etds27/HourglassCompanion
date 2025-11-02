package com.etds.hourglass.ui.presentation.endgame

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.etds.hourglass.model.Device.LocalDevice
import com.etds.hourglass.model.Game.Round
import com.etds.hourglass.model.Player.Player
import com.etds.hourglass.ui.presentation.time.timeToString

private val rowWeights: List<Float> = listOf(
    0.4F, 0.2F, 0.2F, 0.2F
)

@Composable
@Preview
fun EndGameView() {
    val players: List<Player> = listOf(
        Player(name = "Player 1", device = LocalDevice()),
        Player(name = "Player 2", device = LocalDevice()),
        Player(name = "Player 3", device = LocalDevice()),
        Player(name = "Player 4", device = LocalDevice()),
        Player(name = "Player 5", device = LocalDevice()),
    )
    players[0].incrementTurnCounter()
    players[0].incrementTurnCounter()
    players[0].incrementTurnCounter()
    players[0].totalTurnTime = 800000

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color.White)
    ) {
        EndGamePlayerList(
            players
        )
    }
}

@Composable
fun EndGamePlayerList(
    players: List<Player>
) {
    Row(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "Name", fontWeight = FontWeight.Bold, modifier = Modifier.weight(rowWeights[0])
        )
        Text(
            text = "# of Turns",
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(rowWeights[1]),
            textAlign = TextAlign.Center
        )
        Text(
            text = "Total Time",
            Modifier.weight(rowWeights[2]),
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Text(
            text = "Avg Turn Length",
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(rowWeights[3]),
            textAlign = TextAlign.Center
        )
    }
    LazyColumn(
        modifier = Modifier.fillMaxWidth()
    ) {
        items(players) {
            EndGamePlayerRow(it)
        }
    }
}

@Composable
fun EndGamePlayerRow(
    player: Player
) {
    val turnCount by player.turnCounter.collectAsState()

    val averageTurnLength = if (turnCount > 0) player.totalTurnTime / turnCount else 0
    val averageTurnLengthString = timeToString(averageTurnLength, false)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = player.primaryColor),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = player.name,
            fontSize = 24.sp,
            modifier = Modifier
                .weight(rowWeights[0])
                .padding(vertical = 12.dp),
            fontWeight = FontWeight.Bold
        )
        Text(
            text = turnCount.toString(), fontSize = 16.sp,
            modifier = Modifier.weight(rowWeights[1]),
            textAlign = TextAlign.Center
        )
        Text(
            text = timeToString(player.totalTurnTime, false),
            fontSize = 16.sp,
            modifier = Modifier.weight(rowWeights[2]),
            textAlign = TextAlign.Center,
        )
        Text(
            text = averageTurnLengthString,
            fontSize = 16.sp,
            modifier = Modifier.weight(rowWeights[3]),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun EndGameRoundList(
    rounds: List<Round>
) {
    Row(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "Name", fontWeight = FontWeight.Bold, modifier = Modifier.weight(rowWeights[0])
        )
        Text(
            text = "# of Turns",
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(rowWeights[1]),
            textAlign = TextAlign.Center
        )
        Text(
            text = "Total Time",
            Modifier.weight(rowWeights[2]),
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Text(
            text = "Avg Turn Length",
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(rowWeights[3]),
            textAlign = TextAlign.Center
        )
    }
    LazyColumn(
        modifier = Modifier.fillMaxWidth()
    ) {
        items(rounds) {
            EndGameRoundRow(it)
        }
    }
}

@Composable
fun EndGameRoundRow(
    round: Round
) {

}
