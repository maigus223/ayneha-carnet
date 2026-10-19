// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (C) 2026 MAIGUS

package com.ayneha.carnet.ui.components

import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.foundation.text.input.TextFieldBuffer
import androidx.compose.ui.text.TextRange
import com.ayneha.carnet.util.AynehaRtl

/**
 * Appliquée à chaque modification d'un champ (clavier AYNEHA officiel, collage, suppression) :
 * répare les enveloppes RTL (RLO-glyphe-PDF) abîmées pour que l'effacement reste toujours
 * lettre par lettre, de droite à gauche, sans mélange de mots. Voir [AynehaRtl].
 */
object AynehaRtlTransformation : InputTransformation {
    override fun TextFieldBuffer.transformInput() {
        val current = asCharSequence().toString()
        val deletion = current.length < originalText.length && selection.collapsed
        val fixed = AynehaRtl.normalize(current, deletion)
        val text = fixed.text

        val newStart = AynehaRtl.snapOutsideWrapper(text, fixed.mapIndex(selection.start))
        val newEnd = AynehaRtl.snapOutsideWrapper(text, fixed.mapIndex(selection.end))

        if (text != current) {
            // Ne remplace que la partie qui change (limite les perturbations de la composition IME),
            // sans jamais couper une paire de substitution en deux.
            val a = current
            val b = text
            var p = 0
            val maxP = minOf(a.length, b.length)
            while (p < maxP && a[p] == b[p]) p++
            if (p > 0 && p < a.length && Character.isHighSurrogate(a[p - 1])) p--
            var s = 0
            val maxS = minOf(a.length, b.length) - p
            while (s < maxS && a[a.length - 1 - s] == b[b.length - 1 - s]) s++
            if (s > 0 && Character.isLowSurrogate(a[a.length - s])) s--
            replace(p, a.length - s, b.substring(p, b.length - s))
        }
        if (newStart != selection.start || newEnd != selection.end) {
            selection = TextRange(newStart.coerceIn(0, length), newEnd.coerceIn(0, length))
        }
    }
}

/**
 * Filet de sécurité : si un appui place le curseur à l'intérieur d'une lettre (entre RLO et
 * glyphe, ou entre glyphe et PDF), le ramène à la frontière de la lettre.
 */
@Composable
fun KeepCursorOutsideRtlWrappers(state: TextFieldState) {
    LaunchedEffect(state) {
        snapshotFlow { state.selection }.collect { sel ->
            val text = state.text
            val s = AynehaRtl.snapOutsideWrapper(text, sel.start)
            val e = AynehaRtl.snapOutsideWrapper(text, sel.end)
            if (s != sel.start || e != sel.end) {
                state.edit { selection = TextRange(s, e) }
            }
        }
    }
}
