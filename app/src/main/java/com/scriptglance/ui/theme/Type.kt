package com.scriptglance.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.scriptglance.R


private val Montserrat = FontFamily(
    Font(R.font.montserrat_regular, FontWeight.Normal),
    Font(R.font.montserrat_medium, FontWeight.Medium),
    Font(R.font.montserrat_semi_bold, FontWeight.SemiBold),
    Font(R.font.montserrat_bold, FontWeight.Bold),
    Font(R.font.montserrat_light, FontWeight.Light)
)

val AppTypography = Typography(
    displayLarge = TextStyle(fontFamily = Montserrat),
    displayMedium = TextStyle(fontFamily = Montserrat),
    displaySmall = TextStyle(fontFamily = Montserrat),
    headlineLarge = TextStyle(fontFamily = Montserrat),
    headlineMedium = TextStyle(fontFamily = Montserrat),
    headlineSmall = TextStyle(fontFamily = Montserrat),
    titleLarge = TextStyle(fontFamily = Montserrat),
    titleMedium = TextStyle(fontFamily = Montserrat),
    titleSmall = TextStyle(fontFamily = Montserrat),
    bodyLarge = TextStyle(fontFamily = Montserrat),
    bodyMedium = TextStyle(fontFamily = Montserrat),
    bodySmall = TextStyle(fontFamily = Montserrat),
    labelLarge = TextStyle(fontFamily = Montserrat),
    labelMedium = TextStyle(fontFamily = Montserrat),
    labelSmall = TextStyle(fontFamily = Montserrat)
)
