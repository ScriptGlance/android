package com.scriptglance.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable


private val colorScheme = lightColorScheme(
    primary = Green5E,
    secondary = BeigeE5,
    tertiary = Gray59,
    background = WhiteEA,
)

@Composable
fun ScriptGlanceTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}