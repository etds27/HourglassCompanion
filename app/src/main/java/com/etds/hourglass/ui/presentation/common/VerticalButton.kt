package com.etds.hourglass.ui.presentation.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val ButtonShapeRadius = 16.dp

@Composable
fun VerticalIconButton(
    text: String,
    icon: ImageVector,
    primaryColor: Color = MaterialTheme.colorScheme.primary,
    secondaryColor: Color = MaterialTheme.colorScheme.onPrimary,
    modifier: Modifier = Modifier,
    iconSize: Dp = 48.dp,
    innerButtonPadding: Dp = 10.dp,
    onClick: () -> Unit,
) {
    val baseColor: Color
    val accentColor: Color
    val textColor: Color
    if (isSystemInDarkTheme()) {
        baseColor = primaryColor
        accentColor = secondaryColor
        textColor = primaryColor
    } else {
        baseColor = primaryColor
        accentColor = secondaryColor
        textColor = secondaryColor
    }
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .then(modifier)
            .background(Color.Transparent)
    ) {
        val buttonSize = iconSize + innerButtonPadding * 2
        Button(
            modifier = Modifier
                .height(buttonSize)
                .fillMaxWidth(),
            onClick = onClick,
            shape = RoundedCornerShape(ButtonShapeRadius),
            colors = ButtonDefaults.buttonColors(
                containerColor = baseColor
            ),
            border = BorderStroke(
                width = 2.dp,
                color = accentColor
            ),

            contentPadding = PaddingValues(0.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                tint = accentColor,
                modifier = Modifier
                    .size(iconSize)
                    .aspectRatio(1F)
            )
        }
        Spacer(Modifier.padding(2.dp))
        Text(
            text,
            fontSize = 12.sp,
            color = textColor,
            fontWeight = FontWeight.Bold
        )
    }
}
