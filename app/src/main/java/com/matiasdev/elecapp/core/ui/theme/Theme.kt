package com.matiasdev.elecapp.core.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = CircuitGreen,
    secondary = Copper,
    tertiary = SafetyBlue,
    background = SurfaceWarm,
    surface = Color.White,
    error = ErrorRed,
)

@Composable
fun ElecAppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColors,
        content = content,
    )
}
