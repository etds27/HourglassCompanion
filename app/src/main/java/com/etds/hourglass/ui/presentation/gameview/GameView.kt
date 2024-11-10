package com.etds.hourglass.ui.presentation.gameview

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.BluetoothDisabled
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.StopCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.etds.hourglass.R
import com.etds.hourglass.model.Player.Player
import com.etds.hourglass.ui.viewmodel.GameViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.time.DurationUnit
import kotlin.time.toDuration

@AndroidEntryPoint
class GameActivity : ComponentActivity() {
    private val gameViewModel: GameViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GameView(
                applicationContext,
                gameViewModel = gameViewModel
            )
        }
    }
}

@Composable
fun GameView(
    context: Context,
    gameViewModel: GameViewModel,
    modifier: Modifier = Modifier,
) {
    LaunchedEffect(Unit) {
        gameViewModel.startGame()
    }


    val players by gameViewModel.players.collectAsState()
    val skippedPlayers by gameViewModel.skippedPlayers.collectAsState()
    val activePlayer by gameViewModel.activePlayer.collectAsState()
    val isGamePaused by gameViewModel.isGamePaused.collectAsState()
    // val isGamePaused = false
    val turnTime by gameViewModel.turnTime.collectAsState()
    val totalTurnTime by gameViewModel.totalTurnTime.collectAsState()
    val enforceTurnTimer by gameViewModel.enforceTimer.collectAsState()
    val enforceTotalTurnTimer by gameViewModel.enforceTotalTimer.collectAsState()

    val backgroundColor by animateColorAsState(
        targetValue = activePlayer?.color ?: colorResource(R.color.paused_base),
        label = "",
        animationSpec = tween(1000)
    )

    val accentColor by animateColorAsState(
        targetValue = activePlayer?.accentColor ?: colorResource(R.color.paused_accent),
        label = "Accent Color Animation",
        animationSpec = tween(1000)
    )

    LaunchedEffect(Unit) {
        gameViewModel.pauseGame()
    }
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(color = backgroundColor)
                .blur(radius = if (isGamePaused) 4.dp else 0.dp)
                .padding(horizontal = 10.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.padding(24.dp))
            ActivePlayerView(
                gameViewModel = gameViewModel,
                activePlayer = activePlayer,
                turnTime = turnTime,
                totalTurnTime = totalTurnTime,
                isGamePaused = isGamePaused,
                enforceTurnTimer = enforceTurnTimer,
                enforeeTotalTurnTimer = enforceTotalTurnTimer
            )
            PlayerList(
                gameViewModel = gameViewModel,
                players = players,
                activePlayer = activePlayer,
                skippedPlayers = skippedPlayers
            )
            Spacer(
                modifier = Modifier
                    .weight(1f)
            )
            Row(
                modifier = Modifier,
            ) {
                Button(
                    onClick = { gameViewModel.endRound() },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = accentColor)
                ) {
                    Icon(
                        imageVector = Icons.Default.StopCircle,
                        contentDescription = "End Round"
                    )
                    Spacer(modifier = Modifier.padding(2.dp))
                    Text("End Round")
                }
                Spacer(Modifier.weight(.25F))
                Button(
                    onClick = { gameViewModel.toggleGamePause() },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = accentColor)
                ) {
                    if (isGamePaused) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Resume"
                        )
                        Text(text = " Resume")

                    } else {
                        Icon(
                            imageVector = Icons.Default.Pause,
                            contentDescription = "Pause"
                        )
                        Text(text = " Pause")
                    }
                }
            }
            Row(
                modifier = Modifier,
            ) {
                Button(
                    onClick = { gameViewModel.previousPlayer() },
                    modifier = Modifier
                        .weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = accentColor)
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipPrevious,
                        contentDescription = "Previous Player"
                    )
                }
                Spacer(Modifier.weight(.25F))
                Button(
                    onClick = { gameViewModel.nextPlayer() },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = accentColor)
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipNext,
                        contentDescription = "Next Player"
                    )
                }
            }
        }
    }
    if (isGamePaused) {
        PauseView(
            gameViewModel = gameViewModel,
            players = players
        )
    }
}

@Composable
fun ActivePlayerView(
    gameViewModel: GameViewModel,
    activePlayer: Player?,
    isGamePaused: Boolean,
    turnTime: Long = 0,
    totalTurnTime: Long = 0,
    enforceTurnTimer: Boolean = false,
    enforeeTotalTurnTimer: Boolean = false
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row {
            Text(
                text = "Current Player",
                style = TextStyle(textDecoration = TextDecoration.Underline),
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold
            )
        }
        var currentPlayerName = "TEST"
        if (activePlayer != null) {
            currentPlayerName = activePlayer.name
        } else if (isGamePaused) {
            currentPlayerName = "PAUSED"
        }
        Row {
            Text(
                text = currentPlayerName,
                fontSize = 40.sp,
            )
        }
        Spacer(modifier = Modifier.padding(12.dp))
        Row {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                Row() {
                    Text(
                        text = "Turn Time",
                        textDecoration = TextDecoration.Underline,
                        fontSize = 16.sp
                    )
                    val imageVector: ImageVector = if (enforceTurnTimer) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward
                    Icon(imageVector = imageVector, contentDescription = "Turn Timer Icon")
                }
                val turnTimeString = timeToString(turnTime, turnTime < 60000)
                Text(
                    text = if (isGamePaused) "-:--" else turnTimeString,
                    fontSize = 20.sp
                )
            }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                Row() {
                    Text(
                        text = "Total Game Time",
                        textDecoration = TextDecoration.Underline,
                        fontSize = 16.sp
                    )
                    val imageVector: ImageVector = if (enforeeTotalTurnTimer) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward
                    Icon(imageVector = imageVector, contentDescription = "Total Turn Timer Icon")
                }
                val totalTurnTimeString = timeToString(totalTurnTime, totalTurnTime < 60000)
                Text(
                    text = if (isGamePaused) "-:--" else totalTurnTimeString,
                    fontSize = 20.sp
                )
            }
        }
        Spacer(modifier = Modifier.padding(12.dp))
    }
}

@Composable
fun PlayerList(
    gameViewModel: GameViewModel,
    players: List<Player>,
    activePlayer: Player?,
    skippedPlayers: Set<Player>

) {
    LazyColumn {
        items(players, key = { it.name }) { player ->
            PlayerRow(
                gameViewModel = gameViewModel,
                player = player,
                active = player == activePlayer,
                skipped = skippedPlayers.contains(player)
            )
        }
    }
}

@Composable
fun PlayerRow(
    gameViewModel: GameViewModel,
    player: Player,
    active: Boolean = false,
    skipped: Boolean = false,
) {
    val insets = 10.dp
    val turnIndicatorAlpha by animateFloatAsState(
        targetValue = if (active) 1F else 0F,
        label = "Turn Indicator Alpha Animation",
        animationSpec = tween(1000)
    )
    val connected by player.connected.collectAsState()

    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(Modifier.padding(insets))
        Text(
            text = player.name,
            fontSize = 32.sp,
            fontWeight = if (active) FontWeight.Bold else FontWeight.Normal
        )
        Spacer(modifier = Modifier.weight(.25f))

        Icon(
            imageVector = Icons.Default.BluetoothDisabled,
            contentDescription = "Disconnected",
            tint = Color.Red,
            modifier = Modifier.alpha(if (connected) 0.0F else 1.0F)
        )
        Spacer(Modifier.padding(4.dp))
        CurrentTurnIndicator(
            modifier = Modifier
                .alpha(turnIndicatorAlpha)
                .fillMaxWidth()
                .weight(1f)
        )
        Spacer(Modifier.padding(4.dp))
        Button(
            onClick = { gameViewModel.toggleSkipped(player = player) },
            colors = ButtonDefaults.buttonColors(
                containerColor = player.color
            )
        ) {
            if (skipped) {
                Icon(
                    imageVector = Icons.Default.Block,
                    contentDescription = "Skipped"
                )
            } else {
                Icon(
                    imageVector = Icons.Default.CheckCircleOutline,
                    contentDescription = "Not Skipped"
                )
            }
        }
        Spacer(Modifier.padding(insets))
    }
}

fun timeToString(
    time: Long,
    includeMillis: Boolean = true
): String {
    val duration = time.toDuration(DurationUnit.MILLISECONDS)
    val millis = duration.inWholeMilliseconds % 1000 / 100
    val seconds = duration.inWholeSeconds % 60
    val minutes = duration.inWholeMinutes % 60
    val hours = duration.inWholeHours % 24
    val days = duration.inWholeDays % 365
    val components: MutableList<String> = mutableListOf()
    if (days > 0) {
        components.add("%02d".format(days))
    }
    if (hours > 0 || components.isNotEmpty()) {
        components.add("%02d".format(hours))
    }
    if (minutes > 0 || components.isNotEmpty()) {
        components.add("%02d".format(minutes))
    }
    if (seconds > 0 || components.isNotEmpty()) {
        components.add("%02d".format(seconds))
    } else {
        components.add("00")
    }

    var ret: String = ""
    if (includeMillis) {
        ret = components.joinToString(":") + ".%01d".format(millis)
    } else {
        ret = components.joinToString(":")
    }
    return ret
}

@Composable
fun CurrentTurnIndicator(
    modifier: Modifier
) {

    val position = remember { Animatable(0f) }
    val colors: List<Color> = listOf(
        Color.Red,
        Color.Green,
        Color.Yellow,
        Color.Blue
    )

    LaunchedEffect(position) {
        launch {
            while (true) {
                (1..4).forEach { _ ->
                    position.animateTo(
                        targetValue = position.value + 450,
                        animationSpec = tween(
                            durationMillis = 3000
                        )
                    )
                }
                position.animateTo(
                    targetValue = 0F,
                    animationSpec = tween(durationMillis = 0) // Instant reset
                )
            }
        }
    }

    Canvas(
        modifier = modifier
            // .fillMaxSize()
            .aspectRatio(1f)
    ) {
        val canvasWidth = size.width
        val circleSize = canvasWidth / 20
        val distance = canvasWidth / 2F - circleSize * 2
        val numCircles = 16
        rotate(position.value) {
            (1..numCircles).forEach { index ->
                val angle = index.toFloat() / numCircles * 2 * PI
                val x = distance * cos(angle).toFloat()
                val y = distance * sin(angle).toFloat()

                drawCircle(
                    color = Color.Black,
                    center = Offset(x = canvasWidth / 2 + x, y = canvasWidth / 2 + y),
                    radius = circleSize + 3,
                )
                drawCircle(
                    color = colors[(index - 1) / (numCircles / 4)],
                    center = Offset(x = canvasWidth / 2 + x, y = canvasWidth / 2 + y),
                    radius = circleSize,
                )
            }
        }
    }
}