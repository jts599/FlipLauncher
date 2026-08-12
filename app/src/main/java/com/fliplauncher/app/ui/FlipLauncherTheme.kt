package com.fliplauncher.app.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/** Supplies the dark Material colors used around the custom handset interface. */
@Composable
fun FlipLauncherTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = FlipColors.KeyText,
            background = FlipColors.HousingBottom,
            surface = FlipColors.KeyBottom,
        ),
        content = content,
    )
}

/** Centralizes visual tokens translated from the HTML mockup. */
internal object FlipColors {
    val BackdropStart = Color(0xFFC8C4B6)
    val BackdropEnd = Color(0xFFA7A18F)
    val HousingTop = Color(0xFF242821)
    val HousingBottom = Color(0xFF11130F)
    val ScreenFrameTop = Color(0xFF33382E)
    val ScreenFrameBottom = Color(0xFF20241D)
    val BezelTop = Color(0xFF0F110D)
    val BezelBottom = Color(0xFF1B1E18)
    val ScreenTop = Color(0xFFB8C49A)
    val ScreenBottom = Color(0xFF97A77B)
    val ScreenInk = Color(0xFF282E22)
    val KeyTop = Color(0xFF0A0C09)
    val KeyBottom = Color(0xFF171914)
    val KeyText = Color(0xFFD7E2BE)
    val KeySubtext = Color(0xFFA8B48F)
    val Red = Color(0xFFD84F3F)
    val Yellow = Color(0xFFD5A631)
    val Green = Color(0xFF75A84A)
}
