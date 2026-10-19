// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (C) 2026 MAIGUS

package com.ayneha.carnet.ui.trash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ayneha.carnet.data.Note
import com.ayneha.carnet.data.NoteRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TrashViewModel(private val repo: NoteRepository) : ViewModel() {

    val trashedNotes: StateFlow<List<Note>> =
        repo.observeTrash().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun restore(note: Note) {
        viewModelScope.launch { repo.restoreNote(note.id) }
    }

    /** Suppression définitive et irréversible. */
    fun permanentlyDelete(note: Note) {
        viewModelScope.launch { repo.permanentlyDelete(note) }
    }

    class Factory(private val repo: NoteRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            TrashViewModel(repo) as T
    }
}
