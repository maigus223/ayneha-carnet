// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (C) 2026 MAIGUS

package com.ayneha.carnet

import android.content.Context
import com.ayneha.carnet.data.NoteDatabase
import com.ayneha.carnet.util.AynehaRtl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Migration unique des notes enregistrées avec l'ancienne plage AYNEHA (U+E000-E061) vers la
 * nouvelle (U+10FF7C-U+10FFDD). Les enveloppes RTL sont aussi réparées au passage.
 * Idempotent : une note déjà migrée n'est pas modifiée, et le drapeau évite de relire la base.
 */
object PlaneMigration {
    private const val PREFS = "ayneha_carnet_migration"
    private const val KEY_DONE = "plane16_done"

    fun runOnce(context: Context) {
        val app = context.applicationContext
        val prefs = app.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        if (prefs.getBoolean(KEY_DONE, false)) return
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val dao = NoteDatabase.get(app).noteDao()
                for (n in dao.getAllForBackup()) {
                    val title = fix(n.title)
                    val body = fix(n.body)
                    // On conserve updatedAt : la migration ne doit pas réordonner la liste.
                    if (title != n.title || body != n.body) dao.update(n.copy(title = title, body = body))
                }
                prefs.edit().putBoolean(KEY_DONE, true).apply()
            } catch (_: Exception) {
                // Réessaiera au prochain lancement.
            }
        }
    }

    fun fix(text: String): String =
        AynehaRtl.normalize(AynehaRtl.migrateLegacy(text), deletion = false).text
}
