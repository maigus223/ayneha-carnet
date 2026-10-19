// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (C) 2026 MAIGUS

package com.ayneha.carnet.util

/**
 * Garde-fou RTL des champs de saisie du Carnet.
 *
 * Le clavier AYNEHA insère chaque glyphe sous la forme RLO + glyphe + PDF (U+202E, U+202C).
 * Si un seul de ces trois morceaux disparaît (suppression partielle, curseur posé au milieu,
 * texte collé ou ancien texte), le texte restant contient un RLO ou un PDF orphelin : tout ce
 * qui suit se mélange et "saute" visuellement de mot en mot. [normalize] répare cela
 * automatiquement après chaque modification.
 *
 * Fonction pure (aucune dépendance Android) : parcours par points de code, jamais par unités UTF-16
 * isolées, car les glyphes AYNEHA (U+10FF7C-U+10FFDD) occupent deux unités.
 */
object AynehaRtl {
    const val RLO = 0x202E
    const val PDF = 0x202C

    const val LEGACY_FIRST = 0xE000
    const val LEGACY_LAST = 0xE061
    const val NEW_FIRST = 0x10FF7C
    const val NEW_LAST = 0x10FFDD
    const val OFFSET = NEW_FIRST - LEGACY_FIRST // 0x101F7C

    fun isGlyph(cp: Int): Boolean =
        cp in LEGACY_FIRST..LEGACY_LAST || cp in NEW_FIRST..NEW_LAST

    /** Anciens gardes invisibles (ZWSP, WJ) ajoutés après un PDF par les versions précédentes. */
    private fun isGuard(cp: Int): Boolean = cp == 0x200B || cp == 0x2060

    private fun isBidiControl(cp: Int): Boolean = cp == RLO || cp == PDF

    /** Moitié de paire de substitution isolée (laissée par une suppression d'une seule unité). */
    private fun isLoneSurrogate(cp: Int): Boolean = cp in 0xD800..0xDFFF

    /** Convertit l'ancienne plage U+E000-E061 vers U+10FF7C-U+10FFDD (idempotent). */
    fun migrateLegacy(text: String): String {
        var needs = false
        var i = 0
        while (i < text.length) {
            val cp = text.codePointAt(i)
            if (cp in LEGACY_FIRST..LEGACY_LAST) { needs = true; break }
            i += Character.charCount(cp)
        }
        if (!needs) return text
        val sb = StringBuilder(text.length + 8)
        i = 0
        while (i < text.length) {
            val cp = text.codePointAt(i)
            sb.appendCodePoint(if (cp in LEGACY_FIRST..LEGACY_LAST) cp + OFFSET else cp)
            i += Character.charCount(cp)
        }
        return sb.toString()
    }

    /**
     * Un curseur posé par un appui entre le RLO et le glyphe (ou entre le glyphe et le PDF) se
     * trouve à l'intérieur d'une lettre. L'effacement y retirerait alors la lettre située de
     * l'autre côté du curseur (d'abord le caractère à gauche, puis ceux de droite).
     * Ramène l'index à la frontière de la lettre : visuellement, après le RLO = bord droit du
     * glyphe (avant la lettre) ; avant le PDF = bord gauche (après la lettre).
     */
    fun snapOutsideWrapper(text: CharSequence, index: Int): Int {
        val n = text.length
        if (index <= 0 || index > n) return index
        if (text[index - 1].code == RLO && index < n) {
            val x = Character.codePointAt(text, index)
            val k = index + Character.charCount(x)
            if (!isBidiControl(x) && k < n && text[k].code == PDF) return index - 1
        }
        if (index < n && index >= 2 && text[index].code == PDF) {
            val j = if (Character.isLowSurrogate(text[index - 1]) && index >= 3) index - 2 else index - 1
            if (j >= 1 && text[j - 1].code == RLO && !isBidiControl(Character.codePointAt(text, j))) return index + 1
        }
        return index
    }

    class Result(val text: String, private val map: IntArray) {
        /** Position (en unités UTF-16) dans [text] correspondant à [oldIndex] dans le texte d'origine. */
        fun mapIndex(oldIndex: Int): Int = map[oldIndex.coerceIn(0, map.size - 1)]
    }

    /**
     * @param deletion true si la modification est une suppression : les morceaux de wrapper abîmés
     *   (RLO sans PDF, glyphe sans RLO...) sont alors retirés avec leur glyphe, de sorte qu'un
     *   effacement efface toujours une lettre entière. Sinon (saisie, collage), les glyphes
     *   AYNEHA "nus" sont ré-enveloppés.
     */
    fun normalize(text: String, deletion: Boolean): Result {
        val n = text.length
        val map = IntArray(n + 1)
        val out = StringBuilder(n + 8)
        var i = 0
        while (i < n) {
            val cp = text.codePointAt(i)
            val cl = Character.charCount(cp)
            map[i] = out.length
            if (cp == RLO) {
                // Forme correcte : RLO, un point de code non-bidi, PDF.
                val j = i + cl
                if (j < n) {
                    val x = text.codePointAt(j)
                    val xl = Character.charCount(x)
                    if (isLoneSurrogate(x)) {
                        // Moitié de glyphe : on retire le RLO et la moitié restante.
                        for (p in i until j + xl) map[p] = out.length
                        i = j + xl
                        continue
                    }
                    if (!isBidiControl(x)) {
                        val k = j + xl
                        if (k < n && text.codePointAt(k) == PDF) {
                            val start = out.length
                            out.appendCodePoint(RLO)
                            out.appendCodePoint(x)
                            out.appendCodePoint(PDF)
                            val end = out.length
                            // Un curseur posé à l'intérieur d'un wrapper est ramené à sa frontière.
                            for (p in i + cl..j) map[p] = start
                            for (p in j + 1..k) map[p] = end
                            var e = k + 1
                            // Retire les anciens gardes invisibles juste après le PDF.
                            while (e < n && isGuard(text.codePointAt(e))) { map[e] = end; e++ }
                            i = e
                            continue
                        }
                        // RLO + x sans PDF : abîmé.
                        if (deletion && isGlyph(x)) {
                            for (p in i until j + xl) map[p] = out.length
                            i = j + xl
                            continue
                        }
                        // Sinon : on retire seulement le RLO orphelin ; x sera traité au tour suivant.
                        i += cl
                        continue
                    }
                    if (x == PDF) {
                        // RLO PDF vide : on retire les deux.
                        map[j] = out.length
                        i = j + xl
                        continue
                    }
                }
                i += cl // RLO orphelin (fin de texte ou suivi d'un autre RLO)
                continue
            }
            if (cp == PDF || isLoneSurrogate(cp)) { i += cl; continue } // PDF orphelin / moitié de paire
            if (isGuard(cp)) {
                // Garde isolé sans PDF devant : on le conserve tel quel (texte non-AYNEHA).
                out.appendCodePoint(cp); i += cl; continue
            }
            if (isGlyph(cp)) {
                if (deletion) {
                    // Glyphe nu issu d'un wrapper à moitié effacé : s'il est suivi d'un PDF, on retire les deux.
                    val k = i + cl
                    if (k < n && text.codePointAt(k) == PDF) {
                        map[k] = out.length
                        i = k + 1
                        continue
                    }
                    out.appendCodePoint(cp); i += cl; continue
                }
                // Saisie / collage : ré-enveloppe le glyphe nu (et mange un PDF qui le suit déjà).
                out.appendCodePoint(RLO)
                out.appendCodePoint(cp)
                out.appendCodePoint(PDF)
                var k = i + cl
                if (k < n && text.codePointAt(k) == PDF) { map[k] = out.length; k++ }
                i = k
                continue
            }
            out.appendCodePoint(cp)
            i += cl
        }
        map[n] = out.length
        // Les positions au milieu d'une paire de substitution (invalides) pointent sur le début.
        for (p in 0 until n) {
            if (p > 0 && Character.isLowSurrogate(text[p]) && Character.isHighSurrogate(text[p - 1])) {
                map[p] = map[p - 1]
            }
        }
        return Result(out.toString(), map)
    }
}
