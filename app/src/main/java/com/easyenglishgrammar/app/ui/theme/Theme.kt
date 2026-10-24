package com.easyenglishgrammar.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val Night = Color(0xFF1A1033)
val NightDeep = Color(0xFF0E0820)
val Dusk = Color(0xFF2B1A5E)
val Sakura = Color(0xFFFF7EB6)
val Lavender = Color(0xFFB388FF)
val Sky = Color(0xFF64B5F6)
val Gold = Color(0xFFFFE066)
val Mint = Color(0xFF69F0AE)
val Coral = Color(0xFFFF6E6E)
val Paper = Color(0xFFFFF8FC)
val Ink = Color(0xFF2A1F3D)

private val colors = darkColorScheme(
    primary = Sakura,
    onPrimary = Ink,
    secondary = Lavender,
    onSecondary = Ink,
    tertiary = Gold,
    background = Night,
    onBackground = Color.White,
    surface = Dusk,
    onSurface = Color.White,
    surfaceVariant = Color(0xFF3A2770),
    onSurfaceVariant = Color(0xFFE6DDFF),
)

private val typography = Typography(
    displaySmall = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Black, fontSize = 32.sp, letterSpacing = 0.5.sp),
    headlineSmall = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.ExtraBold, fontSize = 24.sp),
    titleLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold, fontSize = 20.sp),
    titleMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold, fontSize = 16.sp),
    bodyLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontSize = 14.sp, lineHeight = 21.sp),
    labelLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold, fontSize = 14.sp, letterSpacing = 0.5.sp),
)

@Composable
fun EasyGrammarTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = colors, typography = typography, content = content)
}
