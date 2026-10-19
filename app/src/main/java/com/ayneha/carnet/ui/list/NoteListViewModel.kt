// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (C) 2026 MAIGUS

package com.ayneha.carnet.ui.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ayneha.carnet.data.Note
import com.ayneha.carnet.data.NoteRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class NoteListViewModel(private val repo: NoteRepository) : ViewModel() {

    private val query = MutableStateFlow("")

    @OptIn(ExperimentalCoroutinesApi::class)
    val notes: StateFlow<List<Note>> = query
        .flatMapLatest { q -> repo.search(q) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        // Nettoyage silencieux des notes à la corbeille depuis plus de 30 jours.
        viewModelScope.launch { repo.purgeExpiredTrash() }
    }

    fun setQuery(q: String) {
        query.value = q
    }

    fun togglePin(note: Note) {
        viewModelScope.launch {
            repo.setPinned(note.id, !note.isPinned)
        }
    }

    /** Met la note à la corbeille (récupérable via [undoTrash]). */
    fun trashNote(note: Note) {
        viewModelScope.launch { repo.trashNote(note.id) }
    }

    /** Annule la mise à la corbeille la plus récente. */
    fun undoTrash(note: Note) {
        viewModelScope.launch { repo.restoreNote(note.id) }
    }

    class Factory(private val repo: NoteRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            NoteListViewModel(repo) as T
    }
}
