package com.etds.hourglass.ui.presentation.gameview

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
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


@Composable
fun PauseView(
    gameViewModel: GameViewModel,
    players: List<Player>
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
                        Button(
                            onClick = { gameViewModel.toggleGamePause() },
                            colors = ButtonDefaults.buttonColors(
                                contentColor = colorResource(R.color.teal_200),
                                containerColor = colorResource(R.color.settings_base_light)
                            ),
                            border = BorderStroke(1.dp, Color.Black)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Resume",
                            )
                        }
                        EditablePlayerList(
                            gameViewModel = gameViewModel,
                            players = players
                        )
                        Spacer(Modifier.weight(1f))
                        Row(

                        ) {
                            Text(
                                text = "Settings:",
                                fontWeight = FontWeight.Bold,
                                fontSize = 32.sp
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
                                    "Turn Timer:",
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
                                    "Total Turn Timer:",
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
    LazyColumn {
        items(players) { player ->
            EditablePlayer(
                gameViewModel = gameViewModel,
                player = player
            )
        }
    }
}

@Composable
fun EditablePlayer(
    gameViewModel: GameViewModel,
    player: Player
) {
    var name by remember { mutableStateOf(player.name) }
    Row(
        modifier = Modifier.fillMaxWidth()
    ) {
        val focusManager = LocalFocusManager.current
        TextField(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(color = colorResource(R.color.settings_base_light)),
            onValueChange = {
                name = it
                gameViewModel.updatePlayerName(player, it)
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Ascii,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = { focusManager.clearFocus() }
            ),
            value = name,
            colors = TextFieldDefaults.colors(
                unfocusedContainerColor = colorResource(R.color.settings_base_light),
                focusedContainerColor = colorResource(R.color.settings_base_light)
            )
        )
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
    }
}

@Composable
fun SettingsTextField (
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
                        RoundedCornerShape(20.dp))
                    .padding(vertical = 10.dp)
            ) {
                innerTextField()
            }
        }
    )
}
