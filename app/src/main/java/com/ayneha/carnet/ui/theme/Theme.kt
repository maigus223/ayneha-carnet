// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (C) 2026 MAIGUS

package com.ayneha.carnet.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val AynehaDarkScheme = darkColorScheme(
    primary = Accent,
    onPrimary = AccentDark,
    background = Bg,
    onBackground = TextPrimary,
    surface = Card,
    onSurface = TextPrimary,
    surfaceVariant = KeyAction,
    onSurfaceVariant = TextMuted,
    outline = Border,
    error = Color(0xFFFF6B6B)
)

@Composable
fun AynehaCarnetTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = AynehaDarkScheme,
        content = content
    )
}
