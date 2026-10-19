// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (C) 2026 MAIGUS

package com.ayneha.carnet.ui.settings

import android.content.Context
import androidx.compose.runtime.compositionLocalOf

/**
 * Réglage "taille du texte" : un multiplicateur appliqué au texte des notes
 * (titre/corps dans l'éditeur, titre/aperçu dans la liste). Persisté en
 * SharedPreferences, sans dépendance supplémentaire.
 */
object TextScaleSettings {
    private const val PREFS = "ayneha_carnet_settings"
    private const val KEY_SCALE = "text_scale"

    const val MIN_SCALE = 0.75f
    const val MAX_SCALE = 1.75f
    const val DEFAULT_SCALE = 1f

    fun get(context: Context): Float =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getFloat(KEY_SCALE, DEFAULT_SCALE)

    fun set(context: Context, scale: Float) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putFloat(KEY_SCALE, scale.coerceIn(MIN_SCALE, MAX_SCALE))
            .apply()
    }
}

/** Multiplicateur courant, à lire dans les écrans : `fontSize = 33.sp * LocalAynehaTextScale.current` */
val LocalAynehaTextScale = compositionLocalOf { TextScaleSettings.DEFAULT_SCALE }
