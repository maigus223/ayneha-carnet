// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (C) 2026 MAIGUS

package com.ayneha.carnet.ui.editor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ayneha.carnet.data.Note
import com.ayneha.carnet.data.NoteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EditorState(
    val id: Long = 0,
    val title: String = "",
    val body: String = "",
    val isPinned: Boolean = false,
    val isNew: Boolean = true,
    val saved: Boolean = false
)

class NoteEditorViewModel(
    private val repo: NoteRepository,
    private val noteId: Long?
) : ViewModel() {

    private val _state = MutableStateFlow(EditorState(isNew = noteId == null || noteId == 0L))
    val state: StateFlow<EditorState> = _state.asStateFlow()

    init {
        if (noteId != null && noteId != 0L) {
            viewModelScope.launch {
                repo.get(noteId)?.let { n ->
                    _state.value = EditorState(
                        id = n.id,
                        title = n.title,
                        body = n.body,
                        isPinned = n.isPinned,
                        isNew = false
                    )
                }
            }
        }
    }

    fun onTitleChange(v: String) {
        _state.update { it.copy(title = v, saved = false) }
    }

    fun onBodyChange(v: String) {
        _state.update { it.copy(body = v, saved = false) }
    }

    fun togglePin() {
        _state.value = _state.value.copy(isPinned = !_state.value.isPinned, saved = false)
    }

    /** Titre obligatoire - retourne false si vide */
    fun save(): Boolean {
        val s = _state.value
        if (s.title.isBlank()) return false
        viewModelScope.launch {
            val note = Note(
                id = s.id,
                title = s.title.trim(),
                body = s.body,
                isPinned = s.isPinned
            )
            val newId = repo.save(note)
            // update (et non une copie de l'ancien état) : ne perd pas la saisie faite pendant l'enregistrement.
            _state.update { it.copy(id = newId, isNew = false, saved = true) }
        }
        return true
    }

    /** Met la note à la corbeille (aucun effet si elle n'a jamais été enregistrée). */
    fun delete() {
        val id = _state.value.id
        if (id == 0L) return
        viewModelScope.launch { repo.trashNote(id) }
    }

    class Factory(
        private val repo: NoteRepository,
        private val noteId: Long?
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            NoteEditorViewModel(repo, noteId) as T
    }
}
