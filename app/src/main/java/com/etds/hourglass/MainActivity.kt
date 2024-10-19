package com.etds.hourglass

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.etds.hourglass.ui.theme.HourglassTheme
import com.etds.hourglass.ui.presentation.launchpage.LaunchPreview

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HourglassTheme {
                LaunchPreview()
            }
        }
    }
}
