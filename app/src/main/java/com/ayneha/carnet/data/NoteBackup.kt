// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (C) 2026 MAIGUS

package com.ayneha.carnet.data

import org.json.JSONArray
import org.json.JSONObject

/**
 * Sérialisation JSON des notes pour la sauvegarde/export complète.
 * Utilise org.json (inclus dans Android), aucune nouvelle dépendance.
 */
object NoteBackup {
    private const val FORMAT = "ayneha_carnet_backup"
    private const val VERSION = 1

    fun toJson(notes: List<Note>): String {
        val array = JSONArray()
        for (n in notes) {
            val o = JSONObject()
            o.put("title", n.title)
            o.put("body", n.body)
            o.put("createdAt", n.createdAt)
            o.put("updatedAt", n.updatedAt)
            o.put("isPinned", n.isPinned)
            o.put("deletedAt", n.deletedAt ?: JSONObject.NULL)
            array.put(o)
        }
        val root = JSONObject()
        root.put("format", FORMAT)
        root.put("version", VERSION)
        root.put("exportedAt", System.currentTimeMillis())
        root.put("notes", array)
        return root.toString(2)
    }

    /** @throws Exception si le contenu n'est pas une sauvegarde AYNEHA Carnet valide. */
    fun fromJson(json: String): List<Note> {
        val root = JSONObject(json)
        require(root.optString("format") == FORMAT) { "not_a_carnet_backup" }
        val array = root.getJSONArray("notes")
        val result = ArrayList<Note>(array.length())
        for (i in 0 until array.length()) {
            val o = array.getJSONObject(i)
            result.add(
                Note(
                    id = 0, // toujours réinséré comme nouvelle note, jamais d'écrasement
                    title = o.getString("title"),
                    body = o.getString("body"),
                    createdAt = o.optLong("createdAt", System.currentTimeMillis()),
                    updatedAt = o.optLong("updatedAt", System.currentTimeMillis()),
                    isPinned = o.optBoolean("isPinned", false),
                    deletedAt = if (o.isNull("deletedAt")) null else o.optLong("deletedAt")
                )
            )
        }
        return result
    }
}
