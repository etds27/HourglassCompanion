package com.etds.hourglass.ui.presentation.gameview

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.BluetoothDisabled
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.StopCircle
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.TimerOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.etds.hourglass.R
import com.etds.hourglass.model.Device.DeviceConnectionState
import com.etds.hourglass.model.Player.Player
import com.etds.hourglass.ui.presentation.common.PauseView
import com.etds.hourglass.ui.presentation.common.TopBarOverlay
import com.etds.hourglass.ui.presentation.common.VerticalIconButton
import com.etds.hourglass.ui.presentation.time.CountDownTimerDisplay
import com.etds.hourglass.ui.presentation.time.StringTimerDisplay
import com.etds.hourglass.ui.presentation.time.TimerDisplay
import com.etds.hourglass.ui.viewmodel.MockSequentialModeViewModel
import com.etds.hourglass.ui.viewmodel.SequentialModeViewModel
import com.etds.hourglass.ui.viewmodel.SequentialModeViewModelProtocol
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.Instant
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun GameView(
    gameViewModel: SequentialModeViewModelProtocol = hiltViewModel<SequentialModeViewModel>(),
    onSettingsNavigate: () -> Unit = {}
) {
    LaunchedEffect(Unit) {
        gameViewModel.startGame()
    }

    var showBackDialog by remember { mutableStateOf(false) }
    var showEndGameDialog by remember { mutableStateOf(false) }

    if (showEndGameDialog) {
        showBackDialog = false
    }

    ExitGameDialog(
        gameViewModel = gameViewModel,
        getShowDialog = { showBackDialog },
        setShowDialog = { showBackDialog = it }
    )

    EndGameDialog(
        gameViewModel = gameViewModel,
        getShowDialog = { showEndGameDialog },
        setShowDialog = { showEndGameDialog = it }
    )


    val players by gameViewModel.players.collectAsState()
    val skippedPlayers by gameViewModel.skippedPlayers.collectAsState()
    val activePlayer by gameViewModel.activePlayer.collectAsState()
    val isPaused by gameViewModel.isGamePaused.collectAsState()
    val currentRoundNumber by gameViewModel.currentRoundNumber.collectAsState()
    val currentRound by gameViewModel.currentRound.collectAsState()

    val totalTurns by gameViewModel.totalTurns.collectAsState()
    val startTime = gameViewModel.gameStartTime
    val gameDuration = Duration.between(startTime, Instant.now()).toMillis()

    val playerTurnCount by currentRound.playerTurnCount.collectAsState()
    val playerRoundTurns = playerTurnCount[activePlayer] ?: -1

    val playerColor = if (isSystemInDarkTheme()) activePlayer?.primaryColor else activePlayer?.accentColor
    val backgroundColor by animateColorAsState(
        targetValue = playerColor ?: colorResource(R.color.paused_base),
        label = "",
        animationSpec = tween(1000)
    )

    val playerAccentColor =
        if (isSystemInDarkTheme()) activePlayer?.accentColor else activePlayer?.primaryColor
    val accentColor by animateColorAsState(
        targetValue = playerAccentColor ?: colorResource(R.color.paused_accent),
        label = "Accent Color Animation",
        animationSpec = tween(1000)
    )

    val updatedColorScheme = MaterialTheme.colorScheme.copy(
        onBackground = backgroundColor,
        onSurface = backgroundColor,
    )

    LaunchedEffect(Unit) {
        // gameViewModel.pauseGame()
    }

    MaterialTheme(
        colorScheme = updatedColorScheme
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .blur(radius = if (isPaused) 4.dp else 0.dp)
                    .padding(horizontal = 10.dp, vertical = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                ActivePlayerView(
                    gameViewModel = gameViewModel
                )
                PlayerList(
                    gameViewModel = gameViewModel,
                    players = players,
                    activePlayer = activePlayer,
                    skippedPlayers = skippedPlayers
                )
                Spacer(
                    modifier = Modifier
                        .padding(8.dp)
                        .fillMaxHeight()
                        .weight(1F)
                )
                GameBannerRow(
                    gameTurns = totalTurns,
                    gameTime = gameDuration,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontColor = MaterialTheme.colorScheme.background
                )
                Spacer(Modifier.padding(0.dp))
                CurrentRoundBannerRow(
                    roundNumber = currentRoundNumber,
                    round = currentRound,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontColor = MaterialTheme.colorScheme.background
                )
                Spacer(Modifier.padding(0.dp))
                PlayerBannerRow(
                    player = activePlayer,
                    playerRoundTurns = playerRoundTurns,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontColor = MaterialTheme.colorScheme.background
                )
                Spacer(Modifier.padding(8.dp))
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                        .clip(RoundedCornerShape(16.dp))
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceEvenly,
                        modifier = Modifier
                            .padding(8.dp)
                            .fillMaxWidth()
                            .wrapContentHeight()
                    ) {
                        val iconSize = 32.dp
                        Row(
                            modifier = Modifier
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            VerticalIconButton(
                                icon = Icons.Default.StopCircle,
                                text = "End Round",
                                primaryColor = backgroundColor,
                                secondaryColor = MaterialTheme.colorScheme.surface,
                                iconSize = iconSize,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1F)
                            ) {
                                gameViewModel.endRound()
                            }
                            Spacer(Modifier.padding(4.dp))
                            val turnTimeEnforced by gameViewModel.enforceTimer.collectAsState()
                            VerticalIconButton(
                                icon = if (turnTimeEnforced) Icons.Default.TimerOff else Icons.Default.Timer,
                                text = if (turnTimeEnforced) "Stop Turn Timer" else "Start Turn Timer",
                                primaryColor = backgroundColor,
                                secondaryColor = MaterialTheme.colorScheme.surface,
                                iconSize = iconSize,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1F)
                            ) {
                                gameViewModel.setTurnTimerEnforced(!turnTimeEnforced)
                            }
                            Spacer(Modifier.padding(4.dp))
                            VerticalIconButton(
                                icon = Icons.Default.Pause,
                                text = if (isPaused) "Resume" else "Pause",
                                primaryColor = backgroundColor,
                                secondaryColor = MaterialTheme.colorScheme.surface,
                                iconSize = iconSize,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1F)
                            ) {
                                gameViewModel.pauseGame()
                            }
                        }
                        Spacer(Modifier.padding(4.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            VerticalIconButton(
                                icon = Icons.Default.SkipPrevious,
                                text = "Previous Player",
                                primaryColor = backgroundColor,
                                secondaryColor = MaterialTheme.colorScheme.surface,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1F),
                                iconSize = iconSize
                            ) {
                                gameViewModel.previousPlayer()
                            }
                            Spacer(Modifier.padding(4.dp))
                            val turnTimerEnforced by gameViewModel.enforceTotalTimer.collectAsState()
                            VerticalIconButton(
                                icon = if (turnTimerEnforced) Icons.Default.TimerOff else Icons.Default.Timer,
                                text = if (turnTimerEnforced) "Stop Total Timer" else "Start Total Timer",
                                primaryColor = backgroundColor,
                                secondaryColor = MaterialTheme.colorScheme.surface,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1F),
                                iconSize = iconSize
                            ) {
                                gameViewModel.setTotalTurnTimerEnforced(!turnTimerEnforced)
                            }
                            Spacer(Modifier.padding(4.dp))
                            VerticalIconButton(
                                icon = Icons.Default.SkipNext,
                                text = "Next Player",
                                primaryColor = backgroundColor,
                                secondaryColor = MaterialTheme.colorScheme.surface,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1F),
                                iconSize = iconSize
                            ) {
                                gameViewModel.nextPlayer()
                            }
                        }
                    }
                }
            }
        }
    }



    var targetColor = MaterialTheme.colorScheme.onBackground
    if (activePlayer != null) {
        targetColor = if (isSystemInDarkTheme()) {
            activePlayer!!.accentColor
        } else {
            activePlayer!!.accentColor
        }
    } else if (isPaused) {
        targetColor = Color.DarkGray
    }

    TopBarOverlay(
        showSettings = true,
        targetColor = targetColor,
        onSettingsNavigate = onSettingsNavigate
    )

    if (isPaused) {
        PauseView(viewModel = gameViewModel, onSettingsNavigate = onSettingsNavigate)
    }
}

@Composable
fun ActivePlayerView(
    gameViewModel: SequentialModeViewModelProtocol,
) {
    val activePlayer by gameViewModel.activePlayer.collectAsState()
    val isGamePaused by gameViewModel.isGamePaused.collectAsState()
    val turnTimer by gameViewModel.turnTimer.collectAsState()
    val openTurnTimer by gameViewModel.openTurnTimer.collectAsState()
    val totalTurnTimer by gameViewModel.totalTurnTimer.collectAsState()
    val openTotalTurnTimer by gameViewModel.openTotalTurnTimer.collectAsState()
    val enforceTurnTimer by gameViewModel.enforceTimer.collectAsState()
    val enforceTotalTurnTimer by gameViewModel.enforceTotalTimer.collectAsState()

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row {
            Text(
                text = "Current Player",
                style = TextStyle(textDecoration = TextDecoration.Underline),
                fontSize = 34.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
        var currentPlayerName = "TEST"
        if (activePlayer != null) {
            currentPlayerName = activePlayer!!.name
        } else if (isGamePaused) {
            currentPlayerName = "PAUSED"
        }
        Row {
            Text(
                text = currentPlayerName,
                fontSize = 40.sp,
                color = MaterialTheme.colorScheme.onBackground,
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
                Row {
                    Text(
                        text = "Turn Time",
                        textDecoration = TextDecoration.Underline,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                val turnTimeString: String
                if (turnTimer == null || isGamePaused) {
                    turnTimeString = "-:--"
                    StringTimerDisplay(
                        text = turnTimeString,
                        textSize = 20.sp,
                        fontColor = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(4.dp)
                    )
                } else {
                    if (enforceTurnTimer) {
                        val turnTime by turnTimer!!.remainingTimeFlow.collectAsState()
                        CountDownTimerDisplay(
                            fontColor = MaterialTheme.colorScheme.onSurface,
                            includeMillis = turnTime < 60000,
                            remainingTime = turnTime,
                            modifier = Modifier.padding(4.dp),
                            showArrow = true
                        )
                    } else {
                        val turnTime by openTurnTimer!!.timeFlow.collectAsState()
                        TimerDisplay(
                            fontColor = MaterialTheme.colorScheme.onSurface,
                            includeMillis = turnTime < 60000,
                            totalTime = turnTime,
                            modifier = Modifier.padding(4.dp),
                            showArrow = true
                        )
                    }
                }
            }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                Row {
                    Text(
                        text = "Total Game Time",
                        textDecoration = TextDecoration.Underline,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                val turnTimeString: String
                if (totalTurnTimer == null || isGamePaused) {
                    turnTimeString = "-:--"
                    StringTimerDisplay(
                        text = turnTimeString,
                        textSize = 20.sp,
                        fontColor = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(4.dp),
                    )
                } else {
                    if (enforceTotalTurnTimer) {
                        val turnTime by totalTurnTimer!!.remainingTimeFlow.collectAsState()
                        CountDownTimerDisplay(
                            fontColor = MaterialTheme.colorScheme.onSurface,
                            includeMillis = turnTime < 60000,
                            remainingTime = turnTime,
                            modifier = Modifier.padding(4.dp),
                            showArrow = true
                        )
                    } else {
                        val turnTime by openTotalTurnTimer!!.timeFlow.collectAsState()
                        TimerDisplay(
                            fontColor = MaterialTheme.colorScheme.onSurface,
                            includeMillis = turnTime < 60000,
                            totalTime = turnTime,
                            modifier = Modifier.padding(4.dp),
                            showArrow = true
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.padding(12.dp))
    }
}

@Composable
fun PlayerList(
    gameViewModel: SequentialModeViewModelProtocol,
    players: List<Player>,
    activePlayer: Player?,
    skippedPlayers: Set<Player>

) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
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
    gameViewModel: SequentialModeViewModelProtocol,
    player: Player,
    active: Boolean = false,
    skipped: Boolean = false,
) {
    val primaryColor = if (isSystemInDarkTheme()) player.accentColor else player.primaryColor
    val targetColor = if (skipped) Color.Gray else primaryColor

    val animatedPrimaryColor by animateColorAsState(
        targetValue = targetColor,
        label = "Primary Color Animation",
        animationSpec = tween(500)
    )

    val insets = 10.dp
    val turnIndicatorAlpha by animateFloatAsState(
        targetValue = if (active) 1F else 0F,
        label = "Turn Indicator Alpha Animation",
        animationSpec = tween(1000)
    )
    val connectionState by player.connectionState.collectAsState()
    val connected = connectionState == DeviceConnectionState.Connected


    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(color = animatedPrimaryColor)
    ) {
        Spacer(Modifier.padding(insets))
        Text(
            text = player.name,
            fontSize = 24.sp,
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
                .width(40.dp)
                .aspectRatio(1F)
        )
        Spacer(Modifier.padding(4.dp))
        Button(
            onClick = { gameViewModel.toggleSkipped(player = player) },
            colors = ButtonDefaults.buttonColors(
                containerColor = animatedPrimaryColor, contentColor = Color.Black
            )
        ) {
            if (skipped) {
                Icon(
                    imageVector = Icons.Default.Block, contentDescription = "Skipped"
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

@Composable
fun CurrentTurnIndicator(
    modifier: Modifier = Modifier
) {

    val position = remember { Animatable(0f) }
    val colors: List<Color> = listOf(
        Color.Red, Color.Green, Color.Yellow, Color.Blue
    )

    LaunchedEffect(position) {
        launch {
            while (true) {
                (1..4).forEach { _ ->
                    position.animateTo(
                        targetValue = position.value + 450, animationSpec = tween(
                            durationMillis = 3000
                        )
                    )
                }
                position.animateTo(
                    targetValue = 0F, animationSpec = tween(durationMillis = 0) // Instant reset
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

@Preview
@Composable
fun MockSequentialGameView() {
    GameView(
        gameViewModel = MockSequentialModeViewModel(),
    )
}