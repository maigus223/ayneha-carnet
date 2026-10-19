// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (C) 2026 MAIGUS

package com.ayneha.carnet.data

import kotlinx.coroutines.flow.Flow

private const val TRASH_RETENTION_DAYS = 30L

class NoteRepository(private val dao: NoteDao) {

    fun observeAll(): Flow<List<Note>> = dao.observeAll()

    fun search(query: String): Flow<List<Note>> =
        if (query.isBlank()) dao.observeAll() else dao.search(query.trim())

    suspend fun get(id: Long): Note? = dao.getById(id)

    suspend fun save(note: Note): Long {
        return if (note.id == 0L) {
            dao.insert(note.copy(updatedAt = System.currentTimeMillis()))
        } else {
            dao.update(note.copy(updatedAt = System.currentTimeMillis()))
            note.id
        }
    }

    suspend fun setPinned(id: Long, pinned: Boolean) = dao.setPinned(id, pinned)

    /** Met une note à la corbeille (récupérable via [restore] ou l'écran Corbeille). */
    suspend fun trashNote(id: Long) = dao.softDelete(id)

    /** Récupère une note depuis la corbeille. */
    suspend fun restoreNote(id: Long) = dao.restore(id)

    fun observeTrash(): Flow<List<Note>> = dao.observeTrash()

    /** Suppression définitive et irréversible (depuis l'écran Corbeille uniquement). */
    suspend fun permanentlyDelete(note: Note) = dao.delete(note)

    /** Nettoyage silencieux des notes à la corbeille depuis plus de 30 jours. */
    suspend fun purgeExpiredTrash() {
        val cutoff = System.currentTimeMillis() - TRASH_RETENTION_DAYS * 24 * 60 * 60 * 1000
        dao.purgeTrashOlderThan(cutoff)
    }

    /** Sauvegarde complète (notes actives + corbeille) au format JSON. */
    suspend fun exportToJson(): String = NoteBackup.toJson(dao.getAllForBackup())

    /**
     * Importe une sauvegarde JSON : chaque note est ajoutée comme nouvelle note
     * (jamais d'écrasement des notes existantes). Retourne le nombre importé.
     */
    suspend fun importFromJson(json: String): Int {
        // Sauvegardes anciennes (plage U+E000-E061) : converties à l'import.
        val notes = NoteBackup.fromJson(json).map {
            it.copy(
                title = com.ayneha.carnet.PlaneMigration.fix(it.title),
                body = com.ayneha.carnet.PlaneMigration.fix(it.body)
            )
        }
        notes.forEach { dao.insert(it) }
        return notes.size
    }
}
