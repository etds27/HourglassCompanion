package com.etds.hourglass.ui.presentation.pause

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.rounded.DragHandle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.etds.hourglass.R
import com.etds.hourglass.model.Player.Player
import com.etds.hourglass.ui.viewmodel.GameViewModel
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState


@Composable
fun PauseView(
    gameViewModel: GameViewModel,
) {
    val turnTimerEnforced by gameViewModel.enforceTimer.collectAsState()
    val totalTurnTimerEnforced by gameViewModel.enforceTotalTimer.collectAsState()
    val turnTime by gameViewModel.timerDuration.collectAsState()
    var localTurnTime by remember { mutableStateOf((turnTime / 1000).toString()) }
    val totalTurnTime by gameViewModel.totalTimerDuration.collectAsState()
    var localTotalTurnTime by remember { mutableStateOf((totalTurnTime / 60000).toString()) }
    val focusManager = LocalFocusManager.current
    val players by gameViewModel.players.collectAsState()
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color.Black.copy(alpha = 0.2f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { gameViewModel.toggleGamePause() },
        contentAlignment = Alignment.Center,
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(modifier = Modifier.weight(0.15f))
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.weight(0.15f))
                Box(
                    modifier = Modifier
                        .clip(shape = RoundedCornerShape(24.dp))
                        .fillMaxSize()
                        .weight(1f)
                ) {
                    Column(
                        modifier = Modifier
                            .background(color = colorResource(R.color.settings_base_light))
                            .fillMaxSize()
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { },
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(Modifier.padding(16.dp))
                        Text(
                            "Player Order:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 32.sp,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                        EditablePlayerList(
                            gameViewModel = gameViewModel,
                            players = players
                        )
                        Box(
                            modifier = Modifier
                                .height(1.dp)
                                .fillMaxWidth()
                                .background(Color.Black)
                        )
                        Spacer(Modifier.padding(8.dp))
                        Row(
                            modifier = Modifier.padding(10.dp)
                        ) {
                            Button(
                                onClick = { gameViewModel.shiftPlayerOrderBackward() },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = colorResource(
                                        R.color.paused_accent
                                    )
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SkipPrevious,
                                    contentDescription = "Shift Order Backward"
                                )
                            }
                            Spacer(Modifier.weight(.25F))
                            Button(
                                onClick = { gameViewModel.shiftPlayerOrderForward() },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = colorResource(
                                        R.color.paused_accent
                                    )
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SkipNext,
                                    contentDescription = "Shift Order Forward"
                                )
                            }
                        }
                        Spacer(Modifier.weight(1f))
                        Row {
                            Text(
                                text = "Settings:",
                                fontWeight = FontWeight.Bold,
                                fontSize = 32.sp,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                        ) {

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp),
                            ) {
                                Checkbox(
                                    checked = turnTimerEnforced,
                                    onCheckedChange = { gameViewModel.toggleEnforcedTurnTimer() }
                                )
                                Text(
                                    "Turn Timer (s):",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f)
                                )
                                SettingsTextField(
                                    value = localTurnTime,
                                    onValueChange = {
                                        localTurnTime = it
                                        gameViewModel.updateTurnTimer(it)
                                    },
                                    enabled = turnTimerEnforced,
                                    keyboardActions = KeyboardActions(
                                        onDone = { focusManager.clearFocus() }
                                    ),

                                    )
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp),

                                ) {
                                Checkbox(
                                    checked = totalTurnTimerEnforced,
                                    onCheckedChange = { gameViewModel.toggleEnforcedTotalTurnTimer() }
                                )
                                Text(
                                    "Total Turn Timer (m):",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f)
                                )
                                SettingsTextField(
                                    enabled = totalTurnTimerEnforced,
                                    value = localTotalTurnTime,
                                    onValueChange = {
                                        localTotalTurnTime = it
                                        gameViewModel.updateTotalTurnTimer(it)
                                    },
                                    keyboardActions = KeyboardActions(
                                        onDone = { focusManager.clearFocus() }
                                    ),
                                )
                            }
                            Spacer(modifier = Modifier.padding(12.dp))
                        }
                    }
                }
                Spacer(modifier = Modifier.weight(0.15f))
            }
            Spacer(modifier = Modifier.weight(0.15f))
        }
    }
}

@Composable
fun EditablePlayerList(
    gameViewModel: GameViewModel,
    players: List<Player>
) {
    val view = LocalView.current
    val lazyListState = rememberLazyListState()
    val reorderableLazyListState = rememberReorderableLazyListState(
        lazyListState,
        onMove = { to, from ->
            gameViewModel.reorderPlayers(from.index, to.index)
        },
    )

    LazyColumn(state = lazyListState) {
        items(players, key = { it.name }) { player ->
            ReorderableItem(
                reorderableLazyListState,
                key = player.name,
            ) { isDragging ->
                val elevation by animateDpAsState(if (isDragging) 8.dp else 0.dp, label = "")

                Surface(
                    shadowElevation = elevation,
                    modifier = Modifier.draggableHandle()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.Black),
                    ) {
                        Box(
                            modifier = Modifier
                                .height(1.dp)
                                .background(Color.Black)
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            EditablePlayer(
                                gameViewModel = gameViewModel,
                                player = player
                            )
                            IconButton(
                                modifier = Modifier
                                    .draggableHandle()
                                    .weight(0.2F)
                                    .height(24.dp),

                                onClick = {},
                            ) {
                                Icon(Icons.Rounded.DragHandle, contentDescription = "Reorder")
                            }
                        }
                    }
                }
            }
        }
    }

}

@Composable
fun EditablePlayer(
    gameViewModel: GameViewModel,
    player: Player
) {
    var name by remember { mutableStateOf(player.name) }
    val focusManager = LocalFocusManager.current
    BasicTextField(
        value = name,
        onValueChange = { value: String ->
            name = value
            gameViewModel.updatePlayerName(player, value)
        },
        modifier = Modifier
            .fillMaxWidth()
            .background(color = colorResource(R.color.settings_base_light))
            .padding(horizontal = 10.dp),
        singleLine = true,
        textStyle = TextStyle.Default.copy(fontSize = 20.sp),

        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Ascii,
            imeAction = ImeAction.Done
        ),
        keyboardActions = KeyboardActions(
            onDone = { focusManager.clearFocus() }
        ),
        decorationBox = { innerTextField ->
            Box(
                modifier = Modifier
                    .background(
                        color = colorResource(R.color.settings_base_light),
                        RoundedCornerShape(20.dp)
                    )
                    .padding(vertical = 15.dp)
            ) {
                innerTextField(
                )
            }
        }
    )
}


/* Removing players
Button(
    onClick = { gameViewMobdel.removePlayer(player) },
    modifier = Modifier.width(64.dp),
    ) {
    Icon(
        imageVector = Icons.Default.Delete,
        contentDescription = "Remove"
    )
}
 */


@Composable
fun SettingsTextField(
    value: String,
    onValueChange: (String) -> Unit,
    enabled: Boolean,
    keyboardActions: KeyboardActions,
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        textStyle = TextStyle(textAlign = TextAlign.Center),
        singleLine = true,
        enabled = enabled,
        modifier = Modifier.width(98.dp),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number,
            imeAction = ImeAction.Done
        ),
        keyboardActions = keyboardActions,
        decorationBox = { innerTextField ->
            Box(
                modifier = Modifier
                    .background(
                        color = if (enabled) Color.White else colorResource(R.color.settings_base_light),
                        RoundedCornerShape(20.dp)
                    )
                    .padding(vertical = 10.dp)
            ) {
                innerTextField()
            }
        }
    )
}
