// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (C) 2026 MAIGUS

package com.ayneha.carnet.ui.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.activity.compose.BackHandler
import com.ayneha.carnet.ui.settings.LocalAynehaTextScale
// LaunchedEffect in runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ayneha.carnet.data.Note
import com.ayneha.carnet.share.NoteImageExporter
import com.ayneha.carnet.ui.AynehaStrings
import com.ayneha.carnet.ui.components.AynehaRtlTransformation
import com.ayneha.carnet.ui.components.KeepCursorOutsideRtlWrappers
import com.ayneha.carnet.ui.components.AynehaText
import com.ayneha.carnet.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteEditorScreen(
    viewModel: NoteEditorViewModel,
    onBack: () -> Unit,
    onDeleted: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    var showDeleteDialog by remember { mutableStateOf(false) }
    var titleError by remember { mutableStateOf(false) }
    val titleFocus = remember { FocusRequester() }
    val bodyFocus = remember { FocusRequester() }
    // Les champs démarrent avec le texte déjà connu du ViewModel (rotation d'écran comprise).
    val titleFieldState = remember { TextFieldState(viewModel.state.value.title) }
    val bodyFieldState = remember { TextFieldState(viewModel.state.value.body) }
    var loaded by remember { mutableStateOf(state.isNew || state.id != 0L) }

    // Chargement initial UNIQUE d'une note existante (le ViewModel la lit en base de façon
    // asynchrone). Ensuite la synchronisation ne va plus que dans un sens : champ -> ViewModel.
    // Une synchronisation dans les deux sens réécrivait le champ avec une valeur périmée pendant
    // un effacement rapide : le curseur sautait en fin de texte et les mots se mélangeaient.
    LaunchedEffect(state.id, state.isNew) {
        if (!loaded && state.id != 0L && !state.isNew) {
            titleFieldState.setTextAndPlaceCursorAtEnd(state.title)
            bodyFieldState.setTextAndPlaceCursorAtEnd(state.body)
            loaded = true
        }
    }

    LaunchedEffect(Unit) {
        snapshotFlow { titleFieldState.text.toString() }
            .collect { newTitle ->
                if (newTitle != viewModel.state.value.title) {
                    titleError = false
                    viewModel.onTitleChange(newTitle)
                }
            }
    }
    LaunchedEffect(Unit) {
        snapshotFlow { bodyFieldState.text.toString() }
            .collect { newBody ->
                if (newBody != viewModel.state.value.body) {
                    viewModel.onBodyChange(newBody)
                }
            }
    }

    KeepCursorOutsideRtlWrappers(titleFieldState)
    KeepCursorOutsideRtlWrappers(bodyFieldState)

    // Nouvelle note : le curseur démarre dans le titre, le clavier AYNEHA s'ouvre tout seul.
    LaunchedEffect(Unit) {
        if (state.isNew) titleFocus.requestFocus()
    }

    BackHandler(enabled = true) { onBack() }

    Scaffold(
        modifier = Modifier.imePadding(),
        containerColor = Bg,
        topBar = {
            TopAppBar(
                title = {
                    Box(
                        Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        AynehaText(
                            text = if (state.isNew) AynehaStrings.nouvelleNote else AynehaStrings.note,
                            fontSize = 30.sp,
                            textAlign = TextAlign.Right,
                            maxLines = 1
                        )
                    }
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
                actions = {
                    // Enregistrer
                    TextButton(onClick = {
                        val ok = viewModel.save()
                        titleError = !ok
                        if (ok) {
                            // feedback léger possible plus tard
                        }
                    }) {
                        AynehaText(AynehaStrings.enregistrer, color = Accent, fontSize = 27.sp)
                        Spacer(Modifier.width(4.dp))
                        Icon(Icons.Outlined.Save, null, tint = Accent, modifier = Modifier.size(18.dp))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Bg)
            )
        },
        bottomBar = {
            Column(Modifier.fillMaxWidth().background(Bg)) {
                // Toolbar SANS Gras
                Row(
                    Modifier
                        .fillMaxWidth()
                        .background(Card)
                        .padding(vertical = 6.dp, horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { viewModel.togglePin() }) {
                        Icon(
                            Icons.Outlined.PushPin,
                            null,
                            tint = if (state.isPinned) Accent else TextMuted
                        )
                    }
                    IconButton(onClick = {
                        val note = Note(
                            id = state.id,
                            title = state.title.ifBlank { "-" },
                            body = state.body,
                            isPinned = state.isPinned
                        )
                        NoteImageExporter.shareAsJpg(context, note)
                    }) {
                        Icon(Icons.Default.Share, null, tint = TextMuted)
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Default.Delete, null, tint = TextMuted)
                    }
                }
            }
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Label titre
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                AynehaText(AynehaStrings.titre, color = TextMuted, fontSize = 23.sp)
                Spacer(Modifier.width(4.dp))
                Text("🏷", fontSize = 13.sp)
            }

            // Titre obligatoire
            BasicTextField(
                state = titleFieldState,
                lineLimits = TextFieldLineLimits.SingleLine,
                textStyle = TextStyle(
                    color = TextPrimary,
                    fontSize = 33.sp * LocalAynehaTextScale.current,
                    fontFamily = AynehaFont,
                    fontWeight = FontWeight.Normal,
                    textAlign = TextAlign.Right,
                    textDirection = TextDirection.Rtl
                ),
                inputTransformation = AynehaRtlTransformation,
                cursorBrush = SolidColor(Accent),
                modifier = Modifier
                    .focusRequester(titleFocus)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 8.dp),
                decorator = { innerTextField ->
                    if (titleFieldState.text.isEmpty()) {
                        AynehaText(
                            text = AynehaStrings.titre,
                            color = TextMuted.copy(alpha = 0.7f),
                            fontSize = 33.sp * LocalAynehaTextScale.current
                        )
                    }
                    innerTextField()
                }
            )

            if (titleError) {
                AynehaText(
                    text = AynehaStrings.titre, // indicateur simple
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 21.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                )
            }

            HorizontalDivider(color = Border)

            // Label corps
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.End
            ) {
                AynehaText(AynehaStrings.ecrisIci + "…", color = TextMuted, fontSize = 23.sp)
                Spacer(Modifier.width(4.dp))
                Text("✏️", fontSize = 13.sp)
            }

            BasicTextField(
                state = bodyFieldState,
                textStyle = TextStyle(
                    color = TextPrimary,
                    fontSize = 36.sp * LocalAynehaTextScale.current,
                    fontFamily = AynehaFont,
                    fontWeight = FontWeight.Normal,
                    textAlign = TextAlign.Right,
                    textDirection = TextDirection.Rtl,
                    lineHeight = 52.sp
                ),
                inputTransformation = AynehaRtlTransformation,
                cursorBrush = SolidColor(Accent),
                modifier = Modifier
                    .focusRequester(bodyFocus)
                    .fillMaxSize()
                    .padding(16.dp),
                decorator = { innerTextField ->
                    if (bodyFieldState.text.isEmpty()) {
                        AynehaText(
                            text = AynehaStrings.ecrisIci + "…",
                            color = TextMuted.copy(alpha = 0.7f),
                            fontSize = 30.sp * LocalAynehaTextScale.current
                        )
                    }
                    innerTextField()
                }
            )
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = {
                Box(
                    Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    AynehaText(
                        text = AynehaStrings.supprimerCetteNote,
                        fontSize = 27.sp,
                        textAlign = TextAlign.Right,
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
                        text = AynehaStrings.actionIrreversible,
                        color = TextMuted,
                        fontSize = 24.sp,
                        textAlign = TextAlign.Right,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        AynehaText(AynehaStrings.non, color = TextMuted, fontSize = 24.sp)
                    }
                    TextButton(onClick = {
                        showDeleteDialog = false
                        viewModel.delete()
                        onDeleted()
                    }) {
                        AynehaText(AynehaStrings.oui, color = Accent, fontSize = 24.sp)
                    }
                }
            },
            containerColor = Card
        )
    }
}
