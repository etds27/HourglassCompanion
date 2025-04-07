package com.etds.hourglass.ui.presentation.buzzer_mode

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsEndWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.TimerOff
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.toSize
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.etds.hourglass.R
import com.etds.hourglass.model.Device.LocalDevice
import com.etds.hourglass.model.DeviceState.BuzzerTurnState
import com.etds.hourglass.model.Player.Player
import com.etds.hourglass.ui.presentation.common.HourglassComposable
import com.etds.hourglass.ui.presentation.common.blockInteraction
import com.etds.hourglass.ui.presentation.common.windowPosition
import com.etds.hourglass.ui.presentation.time.CountDownTimer
import com.etds.hourglass.ui.viewmodel.BuzzerModeViewModel
import com.etds.hourglass.ui.viewmodel.BuzzerModeViewModelProtocol
import com.etds.hourglass.ui.viewmodel.MockBuzzerModeViewModel
import kotlinx.coroutines.delay
import kotlin.random.Random

private val ButtonShapeRadius = 16.dp

@Composable
fun BuzzerModeGameView(
    viewModel: BuzzerModeViewModelProtocol = hiltViewModel<BuzzerModeViewModel>()
) {
    LaunchedEffect(Unit) {
        viewModel.startGame()
    }

    val turnState by viewModel.turnState.collectAsState()
    // var turnState = BuzzerTurnState.BuzzerAwaitingBuzz

    val isPaused by viewModel.isGamePaused.collectAsState()

    val pauseBlur by animateDpAsState(
        targetValue = if (isPaused) 8.dp else 0.dp,
        animationSpec = tween(
            durationMillis = 500,
            easing = LinearEasing
        ), label = "Pause Blur"
    )

    val pauseDarken by animateFloatAsState(
        targetValue = if (isPaused) 0.5F else 0.0F,
        animationSpec = tween(
            durationMillis = 500,
            easing = LinearEasing
        ), label = "Pause Darken"
    )



    BuzzerBackgroundView()


    Box(
        modifier = Modifier
            .fillMaxSize()
            .blur(pauseBlur)
            .background(Color.Black.copy(alpha = pauseDarken))
    ) {

        BuzzerAwaitingBuzzView(viewModel)
        AnimatedVisibility(
            visible = turnState == BuzzerTurnState.BuzzerAwaitingAnswer,
            enter = slideInHorizontally(
                initialOffsetX = { fullWidth -> fullWidth }, // Slide in from right
                animationSpec = tween(500)
            ),
            exit = slideOutHorizontally (
                targetOffsetX = { fullWidth -> fullWidth }, // Slide out to right
                animationSpec = tween(500)
            ),
            modifier = Modifier.align(Alignment.CenterEnd)
        ) {
            BuzzerAwaitingAnswerView(viewModel)
        }
    }

    if (isPaused) {
        BuzzerPauseView(viewModel = viewModel)
    }
}

@Composable
fun BuzzerPauseView(
    viewModel: BuzzerModeViewModelProtocol = hiltViewModel()
) {
    val isPaused by viewModel.isGamePaused.collectAsState()
    val pauseScreenAlpha by animateFloatAsState(
        targetValue = if (isPaused) 1.0F else 0.0F,
        animationSpec = tween(
            durationMillis = 500,
            easing = LinearEasing
        ), label = "Pause Alpha"
    )


    Box(
        modifier = Modifier
            .fillMaxSize()
            .zIndex(1.0F)
            .alpha(pauseScreenAlpha)
            .blockInteraction(enabled = true),
        contentAlignment = Alignment.Center
    ) {
        Column {
            Spacer(
                Modifier
                    .fillMaxSize()
                    .weight(0.4F))
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = "Pause",
                modifier = Modifier
                    .fillMaxSize()
                    .weight(0.2F)
                    .clip(RoundedCornerShape(ButtonShapeRadius))
                    .clickable {
                        viewModel.resumeGame()
                    },
            )
            Spacer(
                Modifier
                    .fillMaxSize()
                    .weight(0.4F))
        }

    }
}

@Composable
fun BuzzerBackgroundView() {
    Box(
        modifier = Modifier
            .background(Color.White)
            .fillMaxSize()
    )
    /*
    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
        }

    }
     */
}

@Composable
fun BuzzerAwaitingAnswerView(
    viewModel: BuzzerModeViewModelProtocol = hiltViewModel()
) {
    val turnStateData by viewModel.turnStateData.collectAsState()
    val answeringPlayer = turnStateData.answerPlayer
    var lastPlayer by remember { mutableStateOf(answeringPlayer) }


    if (lastPlayer == null) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("No one is answering")
            }
        }
        return
    }

    if (answeringPlayer != null) {
        lastPlayer = answeringPlayer
    }

    Column(
        modifier = Modifier
            .fillMaxSize().background(lastPlayer!!.color)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .weight(1F),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            Text(
                "Answering Player:",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                fontSize = 30.sp
            )
            Text(
                lastPlayer!!.name,
                modifier = Modifier.fillMaxWidth(),
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .weight(1F),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                contentAlignment = Alignment.Center
            ) {
                val answerTimerEnforced by viewModel.answerTimerEnforced.collectAsState()
                val fadePercentage by animateFloatAsState(
                    targetValue = if (answerTimerEnforced) 1.0F else 0.0F,
                    animationSpec = tween(
                        durationMillis = 1000,
                        easing = LinearEasing
                    ), label = "Fading"
                )
                val remainingTimer by viewModel.awaitingAnswerTimer.collectAsState()
                if (remainingTimer != null) {
                    val remainingTime by remainingTimer!!.remainingTimeFlow.collectAsState()
                    CountDownTimer(
                        remainingTime = remainingTime,
                        textSize = 84.sp,
                        modifier = Modifier
                            .alpha(fadePercentage)
                    )
                }
                val isPaused by viewModel.isGamePaused.collectAsState()

                HourglassComposable(
                    modifier = Modifier
                        .padding(4.dp)
                        .alpha(1 - fadePercentage),
                    degreesPerRotation = 90,
                    rotationDuration = 2000,
                    circleOutlineThickness = 5,
                    paused = isPaused
                )
            }
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .weight(1F)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Row(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1.0f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = { viewModel.onIncorrectAnswerPress() },
                    border = BorderStroke(
                        width = 2.dp,
                        color = colorResource(R.color.hourglass_dark_red)
                    ),
                    shape = RoundedCornerShape(ButtonShapeRadius),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .weight(1.0f)
                        .padding(12.dp),
                    colors = ButtonDefaults.buttonColors().copy(
                        containerColor = colorResource(R.color.hourglass_light_red),
                        contentColor = colorResource(R.color.hourglass_dark_red)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Incorrect"
                    )
                    Spacer(modifier = Modifier.padding(2.dp))
                    Text(
                        "Incorrect",
                        fontSize = 18.sp
                    )
                }

                Button(
                    onClick = { viewModel.onCorrectAnswerPress() },
                    border = BorderStroke(
                        width = 2.dp,
                        color = colorResource(R.color.hourglass_dark_green)
                    ),
                    shape = RoundedCornerShape(ButtonShapeRadius),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .weight(1.0f)
                        .padding(12.dp),
                    colors = ButtonDefaults.buttonColors().copy(
                        containerColor = colorResource(R.color.hourglass_light_green),
                        contentColor = colorResource(R.color.hourglass_dark_green)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Correct"
                    )
                    Spacer(modifier = Modifier.padding(2.dp))
                    Text(
                        "Correct",
                        fontSize = 18.sp
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1.0f),
                verticalArrangement = Arrangement.Center
            ) {
                val awaitingAnswerTimerEnforced by viewModel.answerTimerEnforced.collectAsState()
                Button(
                    onClick = {
                        if (awaitingAnswerTimerEnforced) {
                            viewModel.onPauseAnswerTimerPress()
                        } else {
                            viewModel.onStartAnswerTimerPress()
                        }
                    },
                    border = BorderStroke(
                        width = 2.dp,
                        color = colorResource(R.color.hourglass_dark_blue)
                    ),
                    shape = RoundedCornerShape(ButtonShapeRadius),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp),
                    colors = ButtonDefaults.buttonColors().copy(
                        containerColor = colorResource(R.color.hourglass_light_blue),
                        contentColor = colorResource(R.color.hourglass_dark_blue)
                    )
                ) {
                    if (awaitingAnswerTimerEnforced) {
                        Icon(
                            imageVector = Icons.Default.TimerOff,
                            contentDescription = "Stop Timer"
                        )
                        Spacer(modifier = Modifier.padding(2.dp))
                        Text(
                            "Stop Timer",
                            fontSize = 18.sp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = "Start Timer"
                        )
                        Spacer(modifier = Modifier.padding(2.dp))
                        Text(
                            "Start Timer",
                            fontSize = 18.sp
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun BuzzerAwaitingBuzzView(
    viewModel: BuzzerModeViewModelProtocol = hiltViewModel()
) {
    val enableBuzzerTimerValue by viewModel.awaitingBuzzTimerEnforced.collectAsState()
    val turnState by viewModel.turnState.collectAsState()
    val turnStateData by viewModel.turnStateData.collectAsState()
    val buzzerEnabled = !turnStateData.awaitingBuzzerEnabled

    val players by viewModel.players.collectAsState()


    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(0.15F),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    val alpha by animateFloatAsState(
                        targetValue = if (enableBuzzerTimerValue) 1.0F else 0.0F,
                        animationSpec = tween(
                            durationMillis = 500,
                            easing = LinearEasing
                        ), label = "Fading"
                    )
                    Column(
                        Modifier
                            .alpha(alpha)
                            .fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {

                        Text(
                            "Remaining Time:",
                            textAlign = TextAlign.Center
                        )

                        val awaitingBuzzerTimer by viewModel.awaitingBuzzerTimer.collectAsState()
                        if (awaitingBuzzerTimer != null) {
                            val awaitingBuzzerTime by awaitingBuzzerTimer!!.remainingTimeFlow.collectAsState()
                            val totalBuzzerTime by viewModel.awaitingBuzzTimerDuration.collectAsState()

                            CountDownTimer(
                                remainingTime = awaitingBuzzerTime,
                                includeMillis = awaitingBuzzerTime < 60000L,
                                textSize = 40.sp,
                                showArrow = true,
                                // showProgressBar = true,
                                totalTime = totalBuzzerTime
                            )
                        }
                    }

                    val isPaused by viewModel.isGamePaused.collectAsState()
                    Log.d("BuzzerModeGameView", "isPaused: $isPaused")
                    HourglassComposable(
                        modifier = Modifier
                            .padding(4.dp)
                            .alpha(1.0F - alpha),
                        degreesPerRotation = 90,
                        rotationDuration = 2000,
                        circleOutlineThickness = 5,
                        paused = isPaused
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(0.7F)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(vertical = 8.dp),
                    verticalArrangement = Arrangement.SpaceEvenly,
                    horizontalAlignment = Alignment.CenterHorizontally,


                    ) {

                    val skippedPlayer by viewModel.skippedPlayers.collectAsState()
                    val allowMultipleAnswers by viewModel.allowMultipleAnswersFromSameUser.collectAsState()
                    val isPaused by viewModel.isGamePaused.collectAsState()
                    players.forEach { player ->
                        val isSkipped = skippedPlayer.contains(player)

                        val playerBuzzerEnabled = !isPaused && !isSkipped && buzzerEnabled && (allowMultipleAnswers || !turnStateData.playersWhoAlreadyAnswered.contains(player))
                        BuzzerModePlayerItem(
                            player = player,
                            buzzerEnabled = playerBuzzerEnabled,
                            viewModel = viewModel,
                            skipped = isSkipped,
                            modifier = Modifier
                                .weight(1F)
                                .fillMaxSize()
                                .clickable {
                                    viewModel.onPlayerAnswer(player)
                                }
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(0.15F)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BuzzerModeVerticalButton(
                        text = "End Round",
                        icon = Icons.Default.Stop,
                        buttonColor = colorResource(R.color.hourglass_light_red),
                        iconColor = colorResource(R.color.hourglass_dark_red)
                    ) { }
                    Spacer(modifier = Modifier.weight(1F))
                    BuzzerModeVerticalButton(
                        text = "Pause",
                        icon = Icons.Default.Pause,
                        buttonColor = colorResource(R.color.hourglass_light_blue),
                        iconColor = colorResource(R.color.hourglass_dark_blue)
                    ) {
                        viewModel.pauseGame()
                    }
                    Spacer(modifier = Modifier.weight(1F))

                    val isTimerActive by viewModel.awaitingBuzzTimerEnforced.collectAsState()
                    if (!isTimerActive) {
                        BuzzerModeVerticalButton(
                            text = "Start Timer",
                            icon = Icons.Default.Timer,
                            buttonColor = colorResource(R.color.hourglass_light_green),
                            iconColor = colorResource(R.color.hourglass_dark_green)
                        ) {
                            viewModel.onStartTimerPress()
                        }
                    } else {
                        BuzzerModeVerticalButton(
                            text = "Stop Timer",
                            icon = Icons.Default.TimerOff,
                            buttonColor = colorResource(R.color.hourglass_light_green),
                            iconColor = colorResource(R.color.hourglass_dark_green)
                        ) {
                            viewModel.onPauseTimerPress()
                        }
                    }
                    Spacer(modifier = Modifier.weight(1F))

                    if (turnStateData.awaitingBuzz) {
                        BuzzerModeVerticalButton(
                            text = "Disable Buzzer",
                            icon = ImageVector.vectorResource(R.drawable.vibration_off),
                            buttonColor = colorResource(R.color.hourglass_light_yellow),
                            iconColor = colorResource(R.color.hourglass_dark_yellow)
                        ) {
                            viewModel.onDisableBuzzersPress()
                        }
                    } else {
                        BuzzerModeVerticalButton(
                            text = "Enable Buzzer",
                            icon = Icons.Default.Vibration,
                            buttonColor = colorResource(R.color.hourglass_light_yellow),
                            iconColor = colorResource(R.color.hourglass_dark_yellow)
                        ) {
                            viewModel.onEnableBuzzersPress()
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BuzzerModeVerticalButton(
    text: String,
    icon: ImageVector,
    buttonColor: Color = MaterialTheme.colorScheme.primary,
    iconColor: Color = MaterialTheme.colorScheme.onPrimary,
    onClick: () -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(90.dp)
            .height(90.dp)
    ) {
        Button(
            onClick = onClick,
            shape = RoundedCornerShape(ButtonShapeRadius),
            colors = ButtonDefaults.buttonColors(
                containerColor = buttonColor
            ),
            border = BorderStroke(
                width = 2.dp,
                color = iconColor
            ),
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                tint = iconColor,
                modifier = Modifier
                    .size(48.dp)
            )
        }
        Spacer(modifier = Modifier.padding(4.dp))
        Text(
            text,
            fontSize = 12.sp
        )
    }
}

@Composable
fun BuzzerModePlayerItem(
    player: Player,
    buzzerEnabled: Boolean = true,
    viewModel: BuzzerModeViewModelProtocol = hiltViewModel(),
    modifier: Modifier = Modifier,
    skipped: Boolean = false
) {
    val playerColor by animateColorAsState(
        targetValue = if (buzzerEnabled) player.color else Color.Gray,
        label = "player_color",
        animationSpec = tween(durationMillis = 500)
    )

    val accentColor by animateColorAsState(
        targetValue = if (buzzerEnabled) player.accentColor else Color.DarkGray,
        label = "player_color",
        animationSpec = tween(durationMillis = 500)
    )
    val radius = 24.dp
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp)
            .clip(RoundedCornerShape(radius))
            .border(3.dp, accentColor, RoundedCornerShape(radius))
            .then(modifier)
        // .zIndex(0F)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .background(playerColor)
                .padding(horizontal = 32.dp),

            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = player.name,
                fontSize = 18.sp,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Normal,
            )
            Spacer(modifier = Modifier.weight(1F).fillMaxWidth())

            Button(
                onClick = { viewModel.toggleSkipped(player = player) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = playerColor, contentColor = Color.Black
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
        }

    }
}

@Preview
@Composable
fun MockBuzzerModeGameView() {
    BuzzerModeGameView(
        viewModel = MockBuzzerModeViewModel()
    )
}