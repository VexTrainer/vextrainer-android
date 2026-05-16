package com.vextrainer.android.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary          = VexBlue,
    onPrimary        = Color.White,
    primaryContainer = VexBlueLight,
    secondary        = VexOrange,
    onSecondary      = Color.White,
    surface          = SurfaceLight,
    onSurface        = OnSurfaceLight,
    error            = ErrorRed
)

private val DarkColors = darkColorScheme(
    primary          = VexBlueLight,
    onPrimary        = Color.Black,
    secondary        = VexOrange,
    onSecondary      = Color.Black,
    error            = ErrorRed
)

@Composable
fun VexTrainerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography  = VexTypography,
        content     = content
    )
}
