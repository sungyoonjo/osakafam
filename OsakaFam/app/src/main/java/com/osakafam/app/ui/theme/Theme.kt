package com.osakafam.app.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

object C {
    // 웹앱의 CSS 변수와 1:1 대응
    val WarmBg = Color(0xFFfaf8f5)
    val CardBg = Color.White
    val Coral = Color(0xFFe8614a)
    val CoralLight = Color(0xFFfdf1ee)
    val CoralMid = Color(0xFFf5c4bb)
    val Sky = Color(0xFF4a8fe8)
    val SkyLight = Color(0xFFeef4fd)
    val Green = Color(0xFF4aaa7a)
    val GreenLight = Color(0xFFedf7f2)
    val Amber = Color(0xFFe8a83a)
    val AmberLight = Color(0xFFfdf6e8)
    val Purple = Color(0xFF8b5cf6)
    val PurpleLight = Color(0xFFf3effe)
    val TextPrimary = Color(0xFF2d2926)
    val TextSecondary = Color(0xFF6b6460)
    val TextMuted = Color(0xFFa8a39e)
    val BorderSoft = Color(0xFFede9e4)
    val BadgeNeutralBg = Color(0xFFf0ede8)
}

private val AppColorScheme = lightColorScheme(
    primary = C.Coral,
    onPrimary = Color.White,
    surface = C.CardBg,
    background = C.WarmBg,
    onBackground = C.TextPrimary,
    onSurface = C.TextPrimary,
    outline = C.BorderSoft,
    surfaceVariant = C.WarmBg,
)

@Composable
fun OsakaFamTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = AppColorScheme, content = content)
}
