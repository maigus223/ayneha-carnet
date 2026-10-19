// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (C) 2026 MAIGUS

package com.ayneha.carnet.ui.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.ayneha.carnet.ui.theme.AynehaFont
import com.ayneha.carnet.ui.theme.TextPrimary

/**
 * Texte AYNEHA standard.
 * - Jamais de Gras
 * - RTL + ancrage à droite
 * - Police Ayneha uniquement
 */
@Composable
fun AynehaText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = TextPrimary,
    fontSize: TextUnit = 30.sp,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip,
    textAlign: TextAlign = TextAlign.Right,
    softWrap: Boolean = true
) {
    Text(
        text = text,
        modifier = modifier,
        color = color,
        fontSize = fontSize,
        fontFamily = AynehaFont,
        fontWeight = FontWeight.Normal, // JAMAIS bold
        textAlign = textAlign,
        maxLines = maxLines,
        overflow = overflow,
        softWrap = softWrap,
        style = TextStyle(
            textDirection = TextDirection.Rtl
        )
    )
}
