package com.etds.hourglass.ui.presentation.gameview

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.etds.hourglass.model.Device.LocalDevice
import com.etds.hourglass.model.Game.Round
import com.etds.hourglass.model.Player.Player
import com.etds.hourglass.ui.presentation.time.timeToString
import java.time.Instant


@Preview
@Composable
fun CurrentRoundBannerPreview() {
    val round = Round()
    val players = listOf(
        Player(name = "", device = LocalDevice()),
        Player(name = "", device = LocalDevice()),
        Player(name = "", device = LocalDevice()),
        Player(name = "", device = LocalDevice()),
    )

    round.setPlayerOrder(players)
    round.roundStartTime = Instant.now()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color.White)
    ) {
        CurrentRoundBannerRow(
            round = round,
            roundNumber = 3,
            color = Color.Black
        )
    }
}

@Composable
fun CurrentRoundBannerRow(
    roundNumber: Int,
    round: Round,
    color: Color
) {
    var expanded by remember { mutableStateOf(false) }

    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp
    val screenWidth = configuration.screenWidthDp
    val bannerHeight = 64.dp
    val bannerTriangle = 64.dp
    val bannerOffset = bannerTriangle / 2 + 32.dp
    val visibleOffset = screenWidth.dp - bannerOffset - 16.dp
    val leftExpandedOffset = 32.dp

    val offsetX = animateDpAsState(
        targetValue = if (expanded) leftExpandedOffset else visibleOffset,
        label = "",
        animationSpec = tween(durationMillis = 1000)
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .offset(
                    x = offsetX.value,
                    y = 0.dp
                )
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                ) {
                    expanded = !expanded
                },
            contentAlignment = Alignment.TopEnd
        ) {
            CurrentRoundBanner(
                roundNumber = roundNumber,
                round = round,
                expanded = expanded,
                height = bannerHeight,
                triangleWidth = bannerTriangle,
                triangleOffset = 32.dp,
                color = color
            )
        }
    }
}


@Composable
fun CurrentRoundBanner(
    roundNumber: Int,
    round: Round,
    expanded: Boolean,
    height: Dp,
    triangleWidth: Dp,
    triangleOffset: Dp,
    color: Color = Color.Black
) {
    val totalRoundTurns by round.totalTurns.collectAsState()
    Row(
        modifier = Modifier
            .height(height)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Canvas(
            modifier = Modifier
                .aspectRatio(0.5f)
                .height(height)
        ) {
            val path = Path().apply {
                moveTo(x = triangleWidth.toPx() / 2, y = 0F)
                lineTo(x = triangleWidth.toPx() / 2, height.toPx())
                lineTo(x = 0F, y = height.toPx() / 2)
                close()
            }
            drawPath(path, color = color)
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(color = color),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,

                ) {
                Text(
                    text = roundNumber.toString(),
                    color = Color.White,
                    modifier = Modifier
                        .background(color = color),
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold
                )
                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Time: ",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = timeToString(round.totalRoundTime, includeMillis = false),
                            fontSize = 20.sp,
                            color = Color.White
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Turns: ",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = totalRoundTurns.toString(),
                            fontSize = 20.sp,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}