package com.etds.hourglass.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

object HourglassColors {
    val PlayerColor1 = Color(0xFFffadad)
    val PlayerColor2 = Color(0xFFffd6a5)
    val PlayerColor3 = Color(0xFFfdffb6)
    val PlayerColor4 = Color(0xFFcaffbf)
    val PlayerColor5 = Color(0xFF9bf6ff)
    val PlayerColor6 = Color(0xFFa0c4ff)
    val PlayerColor7 = Color(0xFFbdb2ff)
    val PlayerColor8 = Color(0xFFffc6ff)
    val PlayerColor9 = Color(0xFFFF8888)
    val PlayerColor10 = Color(0xFFFFCC88)
    val PlayerColor11 = Color(0xFFFFFF88)
    val PlayerColor12 = Color(0xFF88FF88)
    val PlayerColor13 = Color(0xFF88CCFF)
    val PlayerColor14 = Color(0xFF8888FF)
    val PlayerColor15 = Color(0xFFCC88FF)
    val PlayerColor16 = Color(0xFFFF88FF)

    val darkPrimaryColor = Color(0xFFF5F77A)
    val darkOnPrimaryColor = Color(0xFF04031C)
    val darkPrimaryContainerColor = Color(0xFF8C8E3A)
    val darkOnPrimaryContainerColor = Color(0xFFFCFFD2)

    val darkSecondaryColor = Color(0xFFB3DE81)
    val darkOnSecondaryColor = Color(0xFF1E2A0F)
    val darkSecondaryContainerColor = Color(0xFF4C5F35)
    val darkOnSecondaryContainerColor = Color(0xFFE6F8C6)

    val darkBackground = Color(0xFF04031C)
    val darkOnBackground = Color(0xFFF8F88E)

    val darkSurface = Color(0xFF1E1F31)
    val darkOnSurface = Color(0xFFEFF54C)

    val darkSurfaceVariant = Color(0xFF48485A)
    val darkOnSurfaceVariant = Color(0xFFD9D9E0)

    val darkError = Color(0xFFFFB4AB)
    val darkOnError = Color(0xFF690005)
    val darkErrorContainer = Color(0xFF93000A)
    val darkOnErrorContainer = Color(0xFFFFDAD6)

    val darkOutline = Color(0xFF9B9BA5)
    val darkOutlineVariant = Color(0xFF606071)

    val darkInverseSurface = Color(0xFFE5E1E6)
    val darkInverseOnSurface = Color(0xFF2C2C35)
    val darkInversePrimary = Color(0xFF4C4E19)

    val darkSurfaceTint = darkPrimaryColor


    val lightPrimary = Color(0xFFD1B3FF)              // Soft pastel purple
    val lightOnPrimary = Color(0xFF3A1C5D)
    val lightPrimaryContainer = Color(0xFFF3E7FF)     // Even lighter lavender
    val lightOnPrimaryContainer = Color(0xFF4C3372)

    val lightSecondary = Color(0xFFFFC1E3)            // Soft pastel pink
    val lightOnSecondary = Color(0xFF4D1F3A)
    val lightSecondaryContainer = Color(0xFFFFE6F2)
    val lightOnSecondaryContainer = Color(0xFF652647)

    val lightBackground = Color(0xFFFFFBFE)
    val lightOnBackground = Color(0xFF1C1B1F)

    val lightSurface = Color(0xFFFFFFFF)
    val lightOnSurface = Color(0xFF1C1B1F)

    val lightSurfaceVariant = Color(0xFFEDE7F6)       // Lavender gray
    val lightOnSurfaceVariant = Color(0xFF514758)

    val lightError = Color(0xFFBA1A1A)
    val lightOnError = Color.White
    val lightErrorContainer = Color(0xFFFFDAD6)
    val lightOnErrorContainer = Color(0xFF410002)

    val lightOutline = Color(0xFF948FA0)
    val lightOutlineVariant = Color(0xFFD6CFE2)

    val lightInverseSurface = Color(0xFF313033)
    val lightInverseOnSurface = Color(0xFFF4EFF4)
    val lightInversePrimary = Color(0xFFCE9DFF)

    val lightSurfaceTint = lightPrimary

    val HourglassPlayerColors = listOf(
        PlayerColor1,
        PlayerColor2,
        PlayerColor3,
        PlayerColor4,
        PlayerColor5,
        PlayerColor6,
        PlayerColor7,
        PlayerColor8,
        PlayerColor9,
        PlayerColor10,
        PlayerColor11,
        PlayerColor12,
        PlayerColor13,
        PlayerColor14,
        PlayerColor15,
        PlayerColor16,
    )

    val HourglassDarkColorPalette = darkColorScheme(
        primary = darkPrimaryColor,
        onPrimary = darkOnPrimaryColor,
        primaryContainer = darkPrimaryContainerColor,
        onPrimaryContainer = darkOnPrimaryContainerColor,

        secondary = darkSecondaryColor,
        onSecondary = darkOnSecondaryColor,
        secondaryContainer = darkSecondaryContainerColor,
        onSecondaryContainer = darkOnSecondaryContainerColor,

        background = darkBackground,
        onBackground = darkOnBackground,

        surface = darkSurface,
        onSurface = darkOnSurface,

        surfaceVariant = darkSurfaceVariant,
        onSurfaceVariant = darkOnSurfaceVariant,

        error = darkError,
        onError = darkOnError,
        errorContainer = darkErrorContainer,
        onErrorContainer = darkOnErrorContainer,

        outline = darkOutline,
        outlineVariant = darkOutlineVariant,

        inverseSurface = darkInverseSurface,
        inverseOnSurface = darkInverseOnSurface,
        inversePrimary = darkInversePrimary,

        surfaceTint = darkSurfaceTint
    )


    val HourglassLightColorPalette = lightColorScheme(
        primary = lightPrimary,
        onPrimary = lightOnPrimary,
        primaryContainer = lightPrimaryContainer,
        onPrimaryContainer = lightOnPrimaryContainer,

        secondary = lightSecondary,
        onSecondary = lightOnSecondary,
        secondaryContainer = lightSecondaryContainer,
        onSecondaryContainer = lightOnSecondaryContainer,

        background = lightBackground,
        onBackground = lightOnBackground,

        surface = lightSurface,
        onSurface = lightOnSurface,

        surfaceVariant = lightSurfaceVariant,
        onSurfaceVariant = lightOnSurfaceVariant,

        error = lightError,
        onError = lightOnError,
        errorContainer = lightErrorContainer,
        onErrorContainer = lightOnErrorContainer,

        outline = lightOutline,
        outlineVariant = lightOutlineVariant,

        inverseSurface = lightInverseSurface,
        inverseOnSurface = lightInverseOnSurface,
        inversePrimary = lightInversePrimary,

        surfaceTint = lightSurfaceTint
    )

}