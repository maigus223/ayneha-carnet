// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (C) 2026 MAIGUS

package com.ayneha.carnet.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {

    @Query(
        """
        SELECT * FROM notes
        WHERE deletedAt IS NULL
        ORDER BY isPinned DESC, updatedAt DESC
        """
    )
    fun observeAll(): Flow<List<Note>>

    @Query("SELECT * FROM notes WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): Note?

    @Query(
        """
        SELECT * FROM notes
        WHERE deletedAt IS NULL
        AND (title LIKE '%' || :q || '%' OR body LIKE '%' || :q || '%')
        ORDER BY isPinned DESC, updatedAt DESC
        """
    )
    fun search(q: String): Flow<List<Note>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(note: Note): Long

    @Update
    suspend fun update(note: Note)

    /** Suppression définitive (depuis la corbeille uniquement). */
    @Delete
    suspend fun delete(note: Note)

    @Query("UPDATE notes SET isPinned = :pinned, updatedAt = :now WHERE id = :id")
    suspend fun setPinned(id: Long, pinned: Boolean, now: Long = System.currentTimeMillis())

    /** Corbeille : la note reste en base mais disparaît des vues normales. */
    @Query("UPDATE notes SET deletedAt = :now WHERE id = :id")
    suspend fun softDelete(id: Long, now: Long = System.currentTimeMillis())

    /** Annule la mise à la corbeille. */
    @Query("UPDATE notes SET deletedAt = NULL WHERE id = :id")
    suspend fun restore(id: Long)

    @Query(
        """
        SELECT * FROM notes
        WHERE deletedAt IS NOT NULL
        ORDER BY deletedAt DESC
        """
    )
    fun observeTrash(): Flow<List<Note>>

    /** Purge automatique des notes à la corbeille depuis plus de :cutoff (nettoyage silencieux). */
    @Query("DELETE FROM notes WHERE deletedAt IS NOT NULL AND deletedAt < :cutoff")
    suspend fun purgeTrashOlderThan(cutoff: Long)

    /** Toutes les notes, y compris la corbeille — pour l'export/sauvegarde complète. */
    @Query("SELECT * FROM notes")
    suspend fun getAllForBackup(): List<Note>
}
