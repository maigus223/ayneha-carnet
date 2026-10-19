// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (C) 2026 MAIGUS

package com.ayneha.carnet.ui.trash

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ayneha.carnet.data.Note
import com.ayneha.carnet.data.NoteRepository
import com.ayneha.carnet.ui.AynehaStrings
import com.ayneha.carnet.ui.components.AynehaText
import com.ayneha.carnet.ui.theme.Accent
import com.ayneha.carnet.ui.theme.Bg
import com.ayneha.carnet.ui.theme.Border
import com.ayneha.carnet.ui.theme.Card
import com.ayneha.carnet.ui.theme.TextMuted
import com.ayneha.carnet.ui.theme.TextPrimary
import com.ayneha.carnet.util.DateAyneha

/**
 * Écran Corbeille : notes mises de côté depuis l'écran liste ou l'éditeur.
 * Purge silencieuse automatique après 30 jours (voir NoteRepository.purgeExpiredTrash).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrashScreen(repo: NoteRepository, onBack: () -> Unit) {
    val viewModel: TrashViewModel = viewModel(factory = TrashViewModel.Factory(repo))
    val notes by viewModel.trashedNotes.collectAsState()
    var pendingPermanentDelete by remember { mutableStateOf<Note?>(null) }

    Scaffold(
        containerColor = Bg,
        topBar = {
            TopAppBar(
                title = {
                    AynehaText(
                        text = AynehaStrings.corbeille,
                        color = TextPrimary,
                        fontSize = 22.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null,
                            tint = TextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = androidx.compose.ui.graphics.Color.Transparent,
                    titleContentColor = TextPrimary
                )
            )
        }
    ) { padding ->
        if (notes.isEmpty()) {
            Box(
                Modifier
                    .padding(padding)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                AynehaText(
                    text = AynehaStrings.aucuneNote,
                    color = TextMuted,
                    fontSize = 22.sp
                )
            }
        } else {
            LazyColumn(
                Modifier
                    .padding(padding)
                    .padding(horizontal = 14.dp),
                verticalArrangement = Arrangement.spacedBy(11.dp),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                items(notes, key = { it.id }) { note ->
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Card)
                            .border(1.dp, Border, RoundedCornerShape(14.dp))
                            .padding(14.dp)
                    ) {
                        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.End) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.End
                            ) {
                                AynehaText(
                                    text = note.title.ifBlank { AynehaStrings.note },
                                    fontSize = 27.sp,
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
                            note.deletedAt?.let { deletedAt ->
                                Spacer(Modifier.height(4.dp))
                                AynehaText(
                                    text = DateAyneha.noteCardLabel(deletedAt),
                                    color = TextMuted,
                                    fontSize = 18.sp
                                )
                            }
                            Spacer(Modifier.height(8.dp))
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                TextButton(onClick = { pendingPermanentDelete = note }) {
                                    Icon(
                                        Icons.Default.DeleteForever,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                TextButton(onClick = { viewModel.restore(note) }) {
                                    AynehaText(
                                        text = AynehaStrings.restaurer,
                                        color = Accent,
                                        fontSize = 16.sp
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Icon(
                                        Icons.Default.RestartAlt,
                                        contentDescription = null,
                                        tint = Accent,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    pendingPermanentDelete?.let { note ->
        AlertDialog(
            onDismissRequest = { pendingPermanentDelete = null },
            title = {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                    AynehaText(
                        text = AynehaStrings.supprimerCetteNote,
                        color = TextPrimary,
                        fontSize = 22.sp,
                        textAlign = TextAlign.Right,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            text = {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                    AynehaText(
                        text = AynehaStrings.actionIrreversible,
                        color = TextMuted,
                        fontSize = 18.sp,
                        textAlign = TextAlign.Right,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = { pendingPermanentDelete = null }) {
                        AynehaText(AynehaStrings.non, color = TextMuted, fontSize = 22.sp)
                    }
                    TextButton(onClick = {
                        viewModel.permanentlyDelete(note)
                        pendingPermanentDelete = null
                    }) {
                        AynehaText(AynehaStrings.oui, color = MaterialTheme.colorScheme.error, fontSize = 22.sp)
                    }
                }
            },
            containerColor = Card
        )
    }
}
