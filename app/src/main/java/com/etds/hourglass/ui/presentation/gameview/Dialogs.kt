package com.etds.hourglass.ui.presentation.gameview

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.etds.hourglass.R
import com.etds.hourglass.ui.viewmodel.SequentialModeViewModelProtocol


@Composable
fun ExitGameDialog(
    gameViewModel: SequentialModeViewModelProtocol,
    getShowDialog: () -> Boolean,
    setShowDialog: (Boolean) -> Unit
) {
    BackHandler {
        setShowDialog(!getShowDialog())
        if (getShowDialog()) {
            gameViewModel.pauseGame()
        } else {
            gameViewModel.resumeGame()
        }
    }


    val backDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher
    val context = LocalContext.current

    if (getShowDialog()) {
        GameViewYesNoDialog(
            getShowDialog = getShowDialog,
            setShowDialog = setShowDialog,
            text = "Are you sure you want to quit the game",
            yesAction = {
                backDispatcher?.let {
                    backDispatcher.onBackPressed()
                    gameViewModel.quitGame()
                    (context as Activity).finish()
                }
            }
        )
    }
}

@Composable
fun EndGameDialog(
    gameViewModel: SequentialModeViewModelProtocol,
    getShowDialog: () -> Boolean,
    setShowDialog: (Boolean) -> Unit
) {
    val backDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher
    val context = LocalContext.current

    if (getShowDialog()) {
        GameViewYesNoDialog(
            getShowDialog = getShowDialog,
            setShowDialog = setShowDialog,
            text = "Are you sure you want to end the game",
            yesAction = {
                backDispatcher?.let {
                    backDispatcher.onBackPressed()
                    gameViewModel.quitGame()
                    (context as Activity).finish()
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameViewYesNoDialog(
    yesAction: () -> Unit,
    text: String,
    getShowDialog: () -> Boolean,
    setShowDialog: (Boolean) -> Unit
) {

    if (getShowDialog()) {
        BasicAlertDialog(
            onDismissRequest = {
                setShowDialog(false)
            },
            modifier = Modifier.background(
                color = colorResource(R.color.settings_base_light),
                shape = RoundedCornerShape(32.dp)
            )
        ) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = text,
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.padding(16.dp))
                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = yesAction,
                        modifier = Modifier.weight(1F),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colorResource(R.color.paused_accent)
                        )
                    ) {
                        Text(text = "Yes")
                    }
                    Spacer(Modifier.padding(16.dp))
                    Button(
                        onClick = {
                            setShowDialog(false)
                        }, modifier = Modifier.weight(1F)
                    ) {
                        Text(text = "No")
                    }
                }
            }

        }
    }

}