package com.etds.hourglass.ui.presentation.settings

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.RotateLeft
import androidx.compose.material.icons.automirrored.filled.RotateRight
import androidx.compose.material.icons.filled.Rotate90DegreesCw
import androidx.compose.material.icons.filled.RotateLeft
import androidx.compose.material.icons.rounded.DragHandle
import androidx.compose.material.icons.rounded.RotateLeft
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.room.util.TableInfo
import com.etds.hourglass.model.Device.LocalDevice
import com.etds.hourglass.model.Player.Player
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

@Composable
fun SettingsPlayerList(
    modifier: Modifier = Modifier,
    players: List<Player>,
    editablePlayerName: Boolean = false,
    onPlayerNameEdited: (Player, String) -> Unit = { _, _ -> },
    reorderable: Boolean = false,
    onReorder: (Int, Int) -> Unit = { _, _ -> },
    shiftable: Boolean = false,
) {
    Surface(
        modifier = modifier
            .then(modifier)
    ) {
        val lazyListState = rememberLazyListState()
        val reorderableLazyListState = rememberReorderableLazyListState(
            lazyListState,
            onMove = { to, from ->
                onReorder(to.index, from.index)
            },
        )
        Column {
            LazyColumn(
                state = lazyListState,
                modifier = Modifier.fillMaxWidth()
            ) {
                items(players, key = { it.name }) { player ->
                    ReorderableItem(
                        reorderableLazyListState,
                        key = player.name,
                    ) { isDragging ->
                        val elevation by animateDpAsState(
                            if (isDragging) 16.dp else 0.dp,
                            label = ""
                        )

                        Surface(
                            shadowElevation = elevation,
                            modifier = Modifier
                                .then(if (reorderable) Modifier.draggableHandle() else Modifier)

                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                            ) {
                                Box(
                                    modifier = Modifier
                                        .height(1.dp)
                                        .background(Color.Black)
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    SettingsPlayerItem(
                                        player = player,
                                        reorderable = reorderable,
                                        editablePlayerName = editablePlayerName,
                                        onPlayerNameEdited = onPlayerNameEdited,
                                        reorderIconModifier = Modifier.draggableHandle()
                                    )
                                }
                            }
                        }
                    }
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = {
                        onReorder(0, players.size - 1)
                    },
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.onSurface,
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.RotateLeft,
                        contentDescription = "Reorder Left"
                    )
                }

                Button(
                    onClick = {
                        onReorder(players.size - 1, 0)
                    },
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.onSurface,
                    ),
                    shape = RoundedCornerShape(8.dp)

                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.RotateRight,
                        contentDescription = "Reorder Right"
                    )
                }
            }
        }
    }
}


@Composable
fun SettingsPlayerItem(
    modifier: Modifier = Modifier,
    reorderable: Boolean = false,
    reorderIconModifier: Modifier = Modifier,
    player: Player,
    editablePlayerName: Boolean = false,
    onPlayerNameEdited: (Player, String) -> Unit = { _, _ -> },
) {
    Row(
        modifier = Modifier
            .then(modifier),
        verticalAlignment = Alignment.CenterVertically
    ) {

        var name by remember { mutableStateOf(player.name) }
        val focusManager = LocalFocusManager.current
        BasicTextField(
            value = name,
            onValueChange = { value: String ->
                if (editablePlayerName) {
                    name = value
                    onPlayerNameEdited(player, value)
                }
            },
            textStyle = MaterialTheme.typography.bodyMedium.copy(
                color = MaterialTheme.colorScheme.onSurface
            ),
            readOnly = !editablePlayerName,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp)
                .focusable(editablePlayerName),
            singleLine = true,

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
                        .padding(vertical = 15.dp)
                ) {
                    innerTextField()
                }
            }
        )

        if (reorderable) {
            IconButton(
                modifier = Modifier
                    .weight(0.2F)
                    .height(24.dp)
                    .then(reorderIconModifier),
                onClick = {},
            ) {
                Icon(Icons.Rounded.DragHandle, contentDescription = "Reorder")
            }
        }
    }
}


@Preview
@Composable
fun MockSettingsPlayerList() {
    SettingsPlayerList(
        players = listOf(
            Player("Ethan1", LocalDevice()),
            Player("Ethan2", LocalDevice()),
            Player("Ethan3", LocalDevice()),
            Player("Ethan4", LocalDevice()),
            Player("Ethan5", LocalDevice()),
        ),
        reorderable = true
    )
}