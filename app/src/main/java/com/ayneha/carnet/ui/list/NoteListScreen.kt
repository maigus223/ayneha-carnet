// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (C) 2026 MAIGUS

package com.ayneha.carnet.ui.list

import android.content.Intent
import android.provider.Settings
import android.view.inputmethod.InputMethodManager

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlinx.coroutines.launch
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ayneha.carnet.data.Note
import com.ayneha.carnet.ui.AynehaStrings
import com.ayneha.carnet.ui.components.AynehaRtlTransformation
import com.ayneha.carnet.ui.components.KeepCursorOutsideRtlWrappers
import com.ayneha.carnet.ui.components.AynehaText
import com.ayneha.carnet.ui.settings.LocalAynehaTextScale
import com.ayneha.carnet.ui.theme.*
import com.ayneha.carnet.util.DateAyneha

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteListScreen(
    viewModel: NoteListViewModel,
    onOpenNote: (Long) -> Unit,
    onNewNote: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenTrash: () -> Unit,
    onOpenPrivacy: () -> Unit
) {
    val notes by viewModel.notes.collectAsState()
    val queryFieldState = remember { TextFieldState() }
    var menuExpanded by remember { mutableStateOf(false) }
    var showAbout by remember { mutableStateOf(false) }
    val searchFocus = remember { FocusRequester() }
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    KeepCursorOutsideRtlWrappers(queryFieldState)

    // Le champ ne fait plus que refléter le texte ; on relaie chaque changement au ViewModel
    // pour filtrer la liste (le clavier AYNEHA officiel écrit directement dans queryFieldState).
    LaunchedEffect(Unit) {
        snapshotFlow { queryFieldState.text.toString() }
            .collect { viewModel.setQuery(it) }
    }

    Scaffold(
        containerColor = Bg,
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                Row(
                    Modifier
                        .padding(12.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Card)
                        .border(1.dp, Border, RoundedCornerShape(12.dp))
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = { data.performAction() }) {
                        AynehaText(AynehaStrings.annuler, color = Accent, fontSize = 20.sp)
                    }
                    AynehaText(
                        text = data.visuals.message,
                        color = TextPrimary,
                        fontSize = 20.sp
                    )
                }
            }
        },
        topBar = {
            TopAppBar(
                title = {
                    Box(
                        Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        AynehaText(
                            text = AynehaStrings.appTitle,
                            color = Accent,
                            fontSize = 33.sp,
                            textAlign = TextAlign.Right,
                            maxLines = 1
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onOpenPrivacy) {
                        Icon(Icons.Outlined.Info, contentDescription = null, tint = TextPrimary)
                    }
                    Box {
                        IconButton(onClick = { menuExpanded = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = null, tint = TextPrimary)
                        }
                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false },
                            containerColor = Card
                        ) {
                            DropdownMenuItem(
                                text = {
                                    AynehaText(
                                        text = AynehaStrings.toutReste,
                                        fontSize = 18.sp,
                                        color = TextPrimary,
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                },
                                onClick = {
                                    menuExpanded = false
                                    showAbout = true
                                },
                                trailingIcon = {
                                    Text("📱", fontSize = 16.sp)
                                }
                            )
                            DropdownMenuItem(
                                text = {
                                    AynehaText(
                                        text = AynehaStrings.ok,
                                        fontSize = 20.sp,
                                        color = Accent
                                    )
                                },
                                onClick = { menuExpanded = false },
                                trailingIcon = {
                                    Text("✓", fontSize = 16.sp)
                                }
                            )
                            DropdownMenuItem(
                                text = {
                                    Text("⌨️", color = TextPrimary, fontSize = 18.sp)
                                },
                                onClick = {
                                    menuExpanded = false
                                    // Ouvre le sélecteur de clavier système
                                    try {
                                        context.startActivity(
                                            Intent(Settings.ACTION_INPUT_METHOD_SETTINGS)
                                        )
                                    } catch (_: Exception) {
                                        val imm = context.getSystemService(InputMethodManager::class.java)
                                        imm?.showInputMethodPicker()
                                    }
                                },
                                trailingIcon = {
                                    Text("⌨️", fontSize = 16.sp)
                                }
                            )
                            DropdownMenuItem(
                                text = {
                                    AynehaText(text = AynehaStrings.reglages, color = TextPrimary, fontSize = 16.sp)
                                },
                                onClick = {
                                    menuExpanded = false
                                    onOpenSettings()
                                },
                                trailingIcon = {
                                    Text("⚙️", fontSize = 16.sp)
                                }
                            )
                            DropdownMenuItem(
                                text = {
                                    AynehaText(text = AynehaStrings.corbeille, color = TextPrimary, fontSize = 16.sp)
                                },
                                onClick = {
                                    menuExpanded = false
                                    onOpenTrash()
                                },
                                trailingIcon = {
                                    Text("🗑️", fontSize = 16.sp)
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Bg)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNewNote,
                containerColor = Accent,
                contentColor = AccentDark,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
            }
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 14.dp)
        ) {
            // Recherche
            Row(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Card)
                    .border(1.dp, Border, RoundedCornerShape(12.dp))
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End
            ) {
                Icon(Icons.Default.Search, null, tint = TextMuted, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                androidx.compose.foundation.text.BasicTextField(
                    state = queryFieldState,
                    lineLimits = androidx.compose.foundation.text.input.TextFieldLineLimits.SingleLine,
                    textStyle = androidx.compose.ui.text.TextStyle(
                        color = TextPrimary,
                        fontSize = 27.sp,
                        fontFamily = AynehaFont,
                        fontWeight = FontWeight.Normal,
                        textAlign = TextAlign.Right,
                        textDirection = TextDirection.Rtl
                    ),
                    inputTransformation = AynehaRtlTransformation,
                    modifier = Modifier
                        .weight(1f)
                        .focusRequester(searchFocus),
                    decorator = { innerTextField ->
                        if (queryFieldState.text.isEmpty()) {
                            AynehaText(
                                text = AynehaStrings.rechercherNote + "…",
                                color = TextMuted,
                                fontSize = 27.sp
                            )
                        }
                        innerTextField()
                    }
                )
            }

            Spacer(Modifier.height(12.dp))

            if (notes.isEmpty()) {
                EmptyState(onNewNote)
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(11.dp),
                    contentPadding = PaddingValues(bottom = 88.dp)
                ) {
                    items(notes, key = { it.id }) { note ->
                        NoteCard(
                            note = note,
                            onClick = { onOpenNote(note.id) },
                            onDelete = {
                                viewModel.trashNote(note)
                                scope.launch {
                                    val result = snackbarHostState.showSnackbar(
                                        message = AynehaStrings.noteSupprimee,
                                        actionLabel = AynehaStrings.annuler,
                                        duration = SnackbarDuration.Short
                                    )
                                    if (result == SnackbarResult.ActionPerformed) {
                                        viewModel.undoTrash(note)
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    if (showAbout) {
        AlertDialog(
            onDismissRequest = { showAbout = false },
            confirmButton = {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = { showAbout = false }) {
                        AynehaText(AynehaStrings.ok, color = Accent, fontSize = 22.sp)
                    }
                }
            },
            title = {
                Box(
                    Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    AynehaText(
                        text = AynehaStrings.appTitle,
                        color = Accent,
                        fontSize = 24.sp,
                        textAlign = TextAlign.Right,
                        maxLines = 1,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            text = {
                Box(
                    Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    AynehaText(
                        text = AynehaStrings.toutReste,
                        color = TextMuted,
                        fontSize = 20.sp,
                        textAlign = TextAlign.Right,
                        maxLines = 2,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            containerColor = Card
        )
    }
}

@Composable
private fun NoteCard(note: Note, onClick: () -> Unit, onDelete: () -> Unit) {
    Box(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Card)
            .border(1.dp, Border, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(14.dp)
    ) {
        if (note.isPinned) {
            Icon(
                Icons.Outlined.PushPin,
                null,
                tint = Accent,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .size(16.dp)
            )
        }
        IconButton(
            onClick = onDelete,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .size(32.dp)
        ) {
            Icon(
                Icons.Outlined.Delete,
                contentDescription = null,
                tint = TextMuted,
                modifier = Modifier.size(16.dp)
            )
        }
        Column(
            Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.End
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End
            ) {
                AynehaText(
                    text = note.title.ifBlank { AynehaStrings.note },
                    fontSize = 33.sp * LocalAynehaTextScale.current,
                    maxLines = 1
                )
                Spacer(Modifier.width(6.dp))
                Icon(
                    Icons.Outlined.Description,
                    null,
                    tint = TextMuted,
                    modifier = Modifier.size(16.dp)
                )
            }
            if (note.body.isNotBlank()) {
                Spacer(Modifier.height(4.dp))
                AynehaText(
                    text = note.body,
                    color = TextMuted,
                    fontSize = 27.sp * LocalAynehaTextScale.current,
                    maxLines = 2
                )
            }
            Spacer(Modifier.height(6.dp))
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (note.isPinned) {
                    AynehaText(
                        text = AynehaStrings.epinglee,
                        color = Accent,
                        fontSize = 21.sp
                    )
                    Spacer(Modifier.width(10.dp))
                }
                AynehaText(
                    text = DateAyneha.noteCardLabel(note.updatedAt),
                    color = TextMuted,
                    fontSize = 23.sp
                )
                Spacer(Modifier.width(4.dp))
                Icon(Icons.Outlined.Schedule, null, tint = TextMuted, modifier = Modifier.size(14.dp))
            }
        }
    }
}

@Composable
private fun EmptyState(onNewNote: () -> Unit) {
    Column(
        Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("📝", fontSize = 48.sp)
        Spacer(Modifier.height(16.dp))
        AynehaText(
            text = AynehaStrings.aucuneNote,
            fontSize = 33.sp,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))
        // + en premier → à droite
        AynehaText(
            text = AynehaStrings.appuie,
            color = TextMuted,
            fontSize = 27.sp,
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onNewNote)
        )
        Spacer(Modifier.height(12.dp))
        // haya L1 + talfono ra L2
        AynehaText(
            text = AynehaStrings.toutReste,
            color = TextMuted,
            fontSize = 26.sp,
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
