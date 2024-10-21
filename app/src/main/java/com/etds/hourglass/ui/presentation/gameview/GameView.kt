package com.etds.hourglass.ui.presentation.gameview

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DoNotDisturbOff
import androidx.compose.material.icons.filled.DoNotDisturbOn
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.etds.hourglass.R
import com.etds.hourglass.model.Player.Player
import com.etds.hourglass.ui.viewmodel.GameViewModel
import java.lang.reflect.Field
import java.text.NumberFormat
import kotlin.time.DurationUnit
import kotlin.time.toDuration

class GameActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GameView()
        }
    }
}

@Composable
fun GameView(
    modifier: Modifier = Modifier,
) {
    val gameViewModel: GameViewModel = viewModel()
    val players by gameViewModel.players.collectAsState()
    val skippedPlayers by gameViewModel.skippedPlayers.collectAsState()
    val activePlayer by gameViewModel.activePlayer.collectAsState()
    // val isGamePaused by gameViewModel.isGamePaused.collectAsState()
    val isGamePaused = true
    val turnTime by gameViewModel.turnTime.collectAsState()
    val totalTurnTime by gameViewModel.totalTurnTime.collectAsState()

    LaunchedEffect(Unit) {
        gameViewModel.pauseGame()
    }
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(color = colorResource(R.color.purple_200))
                .blur(radius = if (isGamePaused) 6.dp else 0.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ActivePlayerView(
                gameViewModel = gameViewModel,
                activePlayer = activePlayer,
                turnTime = turnTime,
                totalTurnTime = totalTurnTime,
                isGamePaused = isGamePaused
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
                    onClick = { gameViewModel.toggleGamePause() }
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
                Button(
                    onClick = { gameViewModel.previousPlayer() }
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipPrevious,
                        contentDescription = "Previous Player"
                    )
                }
                Button(
                    onClick = { gameViewModel.nextPlayer() }
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
            gameViewModel = gameViewModel
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
                Text(
                    text = "Turn Time",
                    textDecoration = TextDecoration.Underline,
                    fontSize = 16.sp
                )
                val turnTimeString = timeToString(turnTime)
                Text(
                    text = if (isGamePaused) "-:--" else turnTimeString
                )
            }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                Text(
                    text = "Total Game Time",
                    textDecoration = TextDecoration.Underline,
                    fontSize = 16.sp
                )
                val totalTurnTimeString = timeToString(totalTurnTime)
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
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = player.name)
        Spacer(modifier = Modifier.weight(1f))

        CircularProgressIndicator(
            modifier = Modifier.alpha(if (active) 1f else 0f)
        )
        Button(
            onClick = { gameViewModel.toggleSkipped(player = player) }
        ) {
            if (skipped) {
                Icon(
                    imageVector = Icons.Default.DoNotDisturbOn,
                    contentDescription = "Skipped"
                )
            } else {
                Icon(
                    imageVector = Icons.Default.DoNotDisturbOff,
                    contentDescription = "Not Skipped"
                )
            }
        }
    }
}

@Preview
@Composable
fun PagePreview() {
    GameView(modifier = Modifier.fillMaxSize())
}

@Preview
@Composable
fun ActivePlayerPreview() {
    ActivePlayerView(
        gameViewModel = getGameModel(),
        activePlayer = Player(name = "Ethan"),
        isGamePaused = false
    )
}

@Composable
fun PauseView(
    gameViewModel: GameViewModel
) {
    val turnTimerEnforced by gameViewModel.enforceTimer.collectAsState()
    val totalTurnTimerEnforced by gameViewModel.enforceTotalTimer.collectAsState()
    val turnTime by gameViewModel.timerDuration.collectAsState()
    var localTurnTime by remember { mutableStateOf((turnTime / 1000).toString()) }
    val totalTurnTime by gameViewModel.totalTimerDuration.collectAsState()
    var localTotalTurnTime by remember { mutableStateOf((totalTurnTime / 60000).toString()) }
    val focusManager = LocalFocusManager.current
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color.Black.copy(alpha = 0.2f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { gameViewModel.toggleGamePause() },
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(modifier = Modifier.weight(0.25f))
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.weight(0.25f))
                Column(
                    modifier = Modifier
                        .background(color = Color.Gray)
                        .fillMaxSize()
                        .weight(1f)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { },
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Game is Paused")
                    Button(onClick = { gameViewModel.toggleGamePause() }) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Resume"
                        )
                    }
                    Row(

                    ) {
                        Text("Settings:")
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = turnTimerEnforced,
                            onCheckedChange = { gameViewModel.toggleEnforcedTurnTimer() }
                        )
                        Text("Turn Timer:")
                        TextField(
                            enabled = turnTimerEnforced,
                            value = localTurnTime,
                            onValueChange = {
                                localTurnTime = it
                                gameViewModel.updateTurnTimer(it)
                            },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = { focusManager.clearFocus() }
                            )
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = totalTurnTimerEnforced,
                            onCheckedChange = { gameViewModel.toggleEnforcedTotalTurnTimer() }
                        )
                        Text("Total Turn Timer:")
                        TextField(
                            enabled = totalTurnTimerEnforced,
                            value = localTotalTurnTime,
                            onValueChange = {
                                localTotalTurnTime = it
                                gameViewModel.updateTotalTurnTimer(it)
                            },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = { focusManager.clearFocus() }
                            )
                        )
                    }
                }
                Spacer(modifier = Modifier.weight(0.25f))
            }
            Spacer(modifier = Modifier.weight(0.25f))
        }
    }
}


fun getGameModel(): GameViewModel {
    return GameViewModel()
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
