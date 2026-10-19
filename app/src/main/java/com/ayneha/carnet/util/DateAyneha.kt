// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (C) 2026 MAIGUS

package com.ayneha.carnet.util

import com.ayneha.carnet.ui.AynehaStrings
import java.util.Calendar

/**
 * Datation 100 % AYNEHA - source : repo AYNEHA-CALENDRIER (index.html).
 * Chiffres, jours, mois, heures identiques au calendrier.
 */
object DateAyneha {

    /** Chiffres 0-9 (U+10FF7C…U+10FF85) - HC_DIG */
    private val DIG = arrayOf(
        "\uDBFF\uDF7C", "\uDBFF\uDF7D", "\uDBFF\uDF7E", "\uDBFF\uDF7F", "\uDBFF\uDF80",
        "\uDBFF\uDF81", "\uDBFF\uDF82", "\uDBFF\uDF83", "\uDBFF\uDF84", "\uDBFF\uDF85"
    )

    /**
     * Jours complets - HC_DAYS_FULL
     * Index JS getDay() : 0=dimanche … 6=samedi
     * → Java Calendar.DAY_OF_WEEK : index = dayOfWeek - 1
     */
    private val DAYS_FULL = arrayOf(
        "\uDBFF\uDF89\uDBFF\uDF9C\uDBFF\uDF9A\uDBFF\uDFA2\uDBFF\uDF8C\uDBFF\uDF9D\uDBFF\uDF95\uDBFF\uDF9E", // dimanche
        "\uDBFF\uDF88\uDBFF\uDF9C\uDBFF\uDF9A\uDBFF\uDFA2\uDBFF\uDF8C\uDBFF\uDF9D\uDBFF\uDF95\uDBFF\uDF9E",
        "\uDBFF\uDF86\uDBFF\uDF9F\uDBFF\uDF9A\uDBFF\uDFA2\uDBFF\uDF8C\uDBFF\uDF9D\uDBFF\uDF95\uDBFF\uDF9E",
        "\uDBFF\uDF8A\uDBFF\uDF9F\uDBFF\uDFCC\uDBFF\uDF9A\uDBFF\uDFA2\uDBFF\uDF8C\uDBFF\uDF9D\uDBFF\uDF95\uDBFF\uDF9E",
        "\uDBFF\uDF8C\uDBFF\uDF9F\uDBFF\uDF9A\uDBFF\uDFA2\uDBFF\uDF8C\uDBFF\uDF9D\uDBFF\uDF95\uDBFF\uDF9E",
        "\uDBFF\uDF91\uDBFF\uDFA7\uDBFF\uDF86\uDBFF\uDFA0\uDBFF\uDF91\uDBFF\uDF9C",
        "\uDBFF\uDF8B\uDBFF\uDFA0\uDBFF\uDF9A\uDBFF\uDFA2\uDBFF\uDF8C\uDBFF\uDF9D\uDBFF\uDF95\uDBFF\uDF9E"  // samedi
    )

    /** Jours courts - HC_DAYS_SHORT */
    private val DAYS_SHORT = arrayOf(
        "\uDBFF\uDF89\uDBFF\uDF9C\uDBFF\uDF9A",
        "\uDBFF\uDF88\uDBFF\uDF9C\uDBFF\uDF9A",
        "\uDBFF\uDF86\uDBFF\uDF9F\uDBFF\uDF9A",
        "\uDBFF\uDF8A\uDBFF\uDF9F\uDBFF\uDFCC",
        "\uDBFF\uDF8C\uDBFF\uDF9F\uDBFF\uDF9A",
        "\uDBFF\uDF91\uDBFF\uDFA7\uDBFF\uDF86",
        "\uDBFF\uDF8B\uDBFF\uDFA0\uDBFF\uDF9A"
    )

    /** Mois - HC_MONTHS (0=janvier … 11=décembre) */
    private val MONTHS = arrayOf(
        "\uDBFF\uDF9E\uDBFF\uDF8F\uDBFF\uDFA3\uDBFF\uDF86\uDBFF\uDFB6\uDBFF\uDF9C\uDBFF\uDF8B\uDBFF\uDF9C\uDBFF\uDF8B", // jan
        "\uDBFF\uDF9C\uDBFF\uDF8D\uDBFF\uDFCC\uDBFF\uDF9C\uDBFF\uDF99\uDBFF\uDFB6\uDBFF\uDF9C\uDBFF\uDF8B\uDBFF\uDF9C\uDBFF\uDF8B", // fév
        "\uDBFF\uDF9E\uDBFF\uDF8D\uDBFF\uDFA1\uDBFF\uDF98", // mar
        "\uDBFF\uDF9C\uDBFF\uDF9D\uDBFF\uDF8B\uDBFF\uDFA2\uDBFF\uDF86\uDBFF\uDF9E\uDBFF\uDF8D\uDBFF\uDFA1\uDBFF\uDF98", // avr
        "\uDBFF\uDF9C\uDBFF\uDF8D\uDBFF\uDFA0\uDBFF\uDF99\uDBFF\uDFA7\uDBFF\uDF86", // mai
        "\uDBFF\uDF9C\uDBFF\uDF8D\uDBFF\uDF9E\uDBFF\uDF86\uDBFF\uDFA7\uDBFF\uDF86", // jui
        "\uDBFF\uDF9C\uDBFF\uDF8D\uDBFF\uDF9D\uDBFF\uDF91\uDBFF\uDFCC\uDBFF\uDF9F\uDBFF\uDF8A", // jui
        "\uDBFF\uDF9C\uDBFF\uDFBB\uDBFF\uDFAA\uDBFF\uDF99\uDBFF\uDFA3\uDBFF\uDF93", // aoû
        "\uDBFF\uDFB6\uDBFF\uDF9C\uDBFF\uDF9A\uDBFF\uDFA3\uDBFF\uDF8D", // sep
        "\uDBFF\uDF9E\uDBFF\uDF8F\uDBFF\uDFA3\uDBFF\uDF89\uDBFF\uDFA3\uDBFF\uDF88", // oct
        "\uDBFF\uDFB2\uDBFF\uDF9C\uDBFF\uDF91\uDBFF\uDFA2\uDBFF\uDF8D\uDBFF\uDFA0\uDBFF\uDF9A", // nov
        "\uDBFF\uDF9E\uDBFF\uDF90\uDBFF\uDFAE\uDBFF\uDF9E\uDBFF\uDF93" // déc
    )

    /** hcToAy - chaque chiffre → glyphe AYNEHA */
    fun toAy(n: Int): String {
        if (n < 0) return n.toString()
        if (n == 0) return DIG[0]
        val sb = StringBuilder()
        var x = n
        val stack = ArrayDeque<Int>()
        while (x > 0) {
            stack.addFirst(x % 10)
            x /= 10
        }
        stack.forEach { sb.append(DIG[it]) }
        return sb.toString()
    }

    /**
     * hcToAy2Rev - 2 chiffres, ordre des chiffres inversé (comme le calendrier).
     * Ex. 14 → "41" en AYNEHA.
     */
    fun toAy2Rev(n: Int): String {
        val s = n.coerceIn(0, 99).toString().padStart(2, '0')
        val rev = "${s[1]}${s[0]}"
        return rev.map { DIG[it - '0'] }.joinToString("")
    }

    fun monthName(month0to11: Int): String =
        MONTHS[month0to11.coerceIn(0, 11)]

    fun dayFull(javaDayOfWeek: Int): String {
        // Calendar.SUNDAY=1 … SATURDAY=7 → index 0…6
        val idx = (javaDayOfWeek - 1).coerceIn(0, 6)
        return DAYS_FULL[idx]
    }

    fun dayShort(javaDayOfWeek: Int): String {
        val idx = (javaDayOfWeek - 1).coerceIn(0, 6)
        return DAYS_SHORT[idx]
    }

    /**
     * Heure comme le calendrier : secondes:minutes:heures avec toAy2Rev.
     * Pour les notes on affiche minutes:heures (2 composantes).
     */
    fun timeLabel(timestamp: Long): String {
        val cal = Calendar.getInstance().apply { timeInMillis = timestamp }
        val h = cal.get(Calendar.HOUR_OF_DAY)
        val m = cal.get(Calendar.MINUTE)
        // Format calendrier pour HH:MM → toAy2Rev(minutes):toAy2Rev(heures)
        // (le calendrier fait S:M:H ; pour notes on garde M:H cohérent)
        return "${toAy2Rev(m)}∶${toAy2Rev(h)}"
    }

    /**
     * Libellé relatif / date pour les cartes de notes.
     * Aujourd’hui / Hier (lexique) sinon jour + mois AYNEHA (calendrier).
     */
    fun relativeLabel(timestamp: Long): String {
        val now = Calendar.getInstance()
        val then = Calendar.getInstance().apply { timeInMillis = timestamp }

        val sameDay = now.get(Calendar.YEAR) == then.get(Calendar.YEAR) &&
                now.get(Calendar.DAY_OF_YEAR) == then.get(Calendar.DAY_OF_YEAR)

        val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
        val isYesterday = yesterday.get(Calendar.YEAR) == then.get(Calendar.YEAR) &&
                yesterday.get(Calendar.DAY_OF_YEAR) == then.get(Calendar.DAY_OF_YEAR)

        return when {
            sameDay -> AynehaStrings.aujourdhui
            isYesterday -> AynehaStrings.hier
            else -> {
                val day = then.get(Calendar.DAY_OF_MONTH)
                val month = then.get(Calendar.MONTH)
                "${toAy(day)} ${monthName(month)}"
            }
        }
    }

    /** Date complète : jour_semaine jour mois (tout AYNEHA) */
    fun fullDateLabel(timestamp: Long): String {
        val cal = Calendar.getInstance().apply { timeInMillis = timestamp }
        val dow = cal.get(Calendar.DAY_OF_WEEK)
        val day = cal.get(Calendar.DAY_OF_MONTH)
        val month = cal.get(Calendar.MONTH)
        return "${dayFull(dow)} ${toAy(day)} ${monthName(month)}"
    }

    /**
     * Libellé complet pour les cartes de note : heure (format calendrier,
     * minutes:heures inversé) - jour_semaine date_du_jour, mois année.
     * Tout en AYNEHA sauf les séparateurs ":", "-", ",".
     */
    fun noteCardLabel(timestamp: Long): String {
        val cal = Calendar.getInstance().apply { timeInMillis = timestamp }
        val dow = cal.get(Calendar.DAY_OF_WEEK)
        val day = cal.get(Calendar.DAY_OF_MONTH)
        val month = cal.get(Calendar.MONTH)
        val year = cal.get(Calendar.YEAR)
        return "${timeLabel(timestamp)} - ${dayFull(dow)} ${toAy(day)}, ${monthName(month)} ${toAy(year)}"
    }
}
