package com.etds.hourglass.ui.presentation.gameview

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
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
    val roundNumber = 3
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color.White)
    ) {
        CurrentRoundBannerRow(
            round = round,
            roundNumber = roundNumber,
            color = Color.Black
        )
        CurrentRoundBannerRow(
            round = round,
            roundNumber = roundNumber,
            color = Color.Black,
            startExpanded = true
        )
    }
}

@Composable
fun CurrentRoundBannerRow(
    roundNumber: Int,
    round: Round,
    color: Color,
    startExpanded: Boolean = false
) {
    var expanded by remember { mutableStateOf(startExpanded) }

    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp
    val screenWidth = configuration.screenWidthDp
    val bannerHeight = 48.dp
    val bannerTriangle = 48.dp
    val bannerOffset = bannerTriangle / 2 + 0.dp // How far the triangle should protrude

    val visibleOffset =
        screenWidth.dp - bannerOffset - 12.dp - (26.dp * roundNumber.toString().length) // How far the triangle should be visible
    val leftExpandedOffset = 16.dp // How far the triangle should be when expanded

    val offsetX = animateDpAsState(
        targetValue = if (expanded) leftExpandedOffset else visibleOffset,
        label = "",
        animationSpec = tween(durationMillis = 1000)
    )

    val visibleOffsetAnimateX = animateDpAsState(
        targetValue = visibleOffset - 90.dp,
        animationSpec = tween(durationMillis = 1000),
        label = "Animate Label"
    )

    val roundVisibility = animateFloatAsState(
        targetValue = if (expanded) 0F else 1F,
        animationSpec = tween(durationMillis = 1000)
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(bannerHeight),
        contentAlignment = Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .offset(
                    x = visibleOffsetAnimateX.value,
                    y = 0.dp
                )
                .padding(8.dp)
        ) {
            Text(
                text = "Round",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        }
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
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
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
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp
    val totalRoundTurns by round.totalTurns.collectAsState()
    Row(
        modifier = Modifier
            .height(height)
            .width(screenWidth.dp),
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
                .background(color = color)
                .fillMaxHeight(),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxHeight()
            ) {
                Text(
                    text = roundNumber.toString(),
                    color = Color.White,
                    modifier = Modifier,
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.padding(horizontal = 8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(.7F),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1F)
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
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1F)
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
