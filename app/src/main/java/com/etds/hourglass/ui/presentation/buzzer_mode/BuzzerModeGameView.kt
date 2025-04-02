package com.etds.hourglass.ui.presentation.buzzer_mode

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsEndWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.TimerOff
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import com.etds.hourglass.R
import com.etds.hourglass.model.Device.LocalDevice
import com.etds.hourglass.model.DeviceState.BuzzerTurnState
import com.etds.hourglass.model.Player.Player
import com.etds.hourglass.ui.presentation.time.CountDownTimer
import com.etds.hourglass.ui.viewmodel.BuzzerModeViewModelProtocol
import com.etds.hourglass.ui.viewmodel.MockBuzzerModeViewModel
import kotlinx.coroutines.delay
import kotlin.random.Random

@Composable
fun BuzzerModeGameView(
    viewModel: BuzzerModeViewModelProtocol = hiltViewModel()
) {
    // var turnState by viewModel.turnState.collectAsState()
    var turnState = BuzzerTurnState.BuzzerAwaitingBuzz

    BuzzerBackgroundView()

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        when(turnState) {
            BuzzerTurnState.BuzzerAwaitingBuzz -> BuzzerAwaitingBuzzView(viewModel)
            BuzzerTurnState.BuzzerAwaitingBuzzerEnabled -> BuzzerAwaitingBuzzerEnabledView(viewModel)
            BuzzerTurnState.BuzzerTurnStart -> TODO()
            BuzzerTurnState.BuzzerEnterTurnLoop -> TODO()
            BuzzerTurnState.BuzzerAwaitingAnswer -> TODO()
        }
    }

}

@Composable
fun BuzzerBackgroundView() {
    var percent by remember { mutableFloatStateOf(0.5F) }

    LaunchedEffect(Unit) {
        while (true) {
            val seconds = System.currentTimeMillis() % 60000L
            percent = seconds / 60000F
            delay(1000L)
        }
    }


    val animatedWeight = animateFloatAsState(percent,
        animationSpec = tween(
            1000,
            easing = LinearEasing
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Green)
                    .weight(1 - animatedWeight.value)
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Red)
                    .weight(animatedWeight.value)
            )
        }

    }
}

@Composable
fun BuzzerAwaitingBuzzerEnabledView(
    viewModel: BuzzerModeViewModelProtocol = hiltViewModel()
) {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize()
    ) {
        Spacer(modifier = Modifier
            .fillMaxHeight()
            .weight(1F))
        Button(
            modifier = Modifier
                .height(64.dp),
            onClick = { viewModel.onEnableBuzzersPress() }
        ) {
            Text(text = "Enable Buzzers")
        }
        Spacer(modifier = Modifier
            .fillMaxHeight()
            .weight(1F))
        Button(
            modifier = Modifier
                .height(64.dp),
            onClick = { viewModel.onEnableBuzzersPress() }
        ) {
            Text(text = "Pause Game")
        }
        Spacer(modifier = Modifier
            .fillMaxHeight()
            .weight(1F))

    }
}


@Composable
fun BuzzerAwaitingBuzzView(
    viewModel: BuzzerModeViewModelProtocol = hiltViewModel()
) {
    val enableBuzzerTimerValue by viewModel.awaitingBuzzTimerEnforced.collectAsState()

    // val enableAnswerTimer by viewModel.answerTimerEnforced.collectAsState()

    var isAwaitingBuzzScreen by remember { mutableStateOf(true) }
    var expanded by remember { mutableStateOf(false) }
    var expandedIndex by remember { mutableStateOf<Int?>(null) }

    val players by remember { mutableStateOf(listOf(
        Player(name = "Ethan", device = LocalDevice()),
        Player(name = "Haley", device = LocalDevice()),
        Player(name = "Ethan2", device = LocalDevice()),
        Player(name = "Haley2", device = LocalDevice()),
        Player(name = "Ethan3", device = LocalDevice()),
        Player(name = "Haley3", device = LocalDevice()),
    )
    )
    }

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
                if (enableBuzzerTimerValue) {
                    Text("Remaining Time:",
                        textAlign = TextAlign.Center)

                    val awaitingBuzzerTime by viewModel.awaitingBuzzerRemainingTime.collectAsState()
                    CountDownTimer(
                        remainingTime = awaitingBuzzerTime,
                        includeMillis = awaitingBuzzerTime < 60000L,
                        textSize = 40.sp,
                        showArrow = true
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(0.7F)
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(vertical = 32.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,


                    ) {
                    items(players.count()) { index ->
                        val player = players[index]

                        BuzzerModePlayerItem(
                            player = player,
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
                        icon = Icons.Default.Stop
                    ) { }
                    Spacer(modifier = Modifier.weight(1F))
                    BuzzerModeVerticalButton(
                        text = "Pause",
                        icon = Icons.Default.Pause
                    ) { }
                    Spacer(modifier = Modifier.weight(1F))

                    val isTimerPaused by viewModel.awaitingBuzzerIsPaused.collectAsState()
                    if (isTimerPaused) {
                        BuzzerModeVerticalButton(
                            text = "Start Timer",
                            icon = Icons.Default.Timer
                        ) {
                            viewModel.onStartTimerPress()
                        }
                    } else {
                        BuzzerModeVerticalButton(
                            text = "Stop Timer",
                            icon = Icons.Default.TimerOff
                        ) {
                            viewModel.onPauseTimerPress()
                        }
                    }
                    Spacer(modifier = Modifier.weight(1F))

                    val turnStateData by viewModel.turnStateData.collectAsState()
                    if (turnStateData.awaitingBuzz) {
                        BuzzerModeVerticalButton(
                            text = "Enable Buzzers",
                            icon = Icons.Default.Vibration
                        ) {
                            viewModel.onEnableBuzzersPress()
                        }
                    } else {
                        BuzzerModeVerticalButton(
                            text = "Disable Buzzers",
                            icon = ImageVector.vectorResource(R.drawable.vibration_off)
                        ) {
                            viewModel.onDisableBuzzersPress()
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
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Icon(imageVector = icon, contentDescription = text, modifier = Modifier.size(48.dp))
        }
        Spacer(modifier = Modifier.padding(4.dp))
        Text(
            text,
            fontSize = 10.sp
        )
    }
}

@Composable
fun BuzzerModePlayerItem(
    player: Player
) {

    Box(
        modifier = Modifier
            .padding(10.dp)
            .background(
                Color(
                    Random.nextInt(256),
                    Random.nextInt(256),
                    Random.nextInt(256)
                )
            )
            .fillMaxWidth()
            .height(60.dp)
            // .zIndex(0F)
    ) {
        Text(
            text = player.name,
            textAlign = TextAlign.Center,

            )
    }
}

@Preview
@Composable
fun MockBuzzerModeGameView() {
    BuzzerModeGameView(
        viewModel = MockBuzzerModeViewModel()
    )
}