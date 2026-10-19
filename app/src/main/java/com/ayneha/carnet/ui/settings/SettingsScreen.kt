// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (C) 2026 MAIGUS

package com.ayneha.carnet.ui.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ayneha.carnet.data.NoteRepository
import com.ayneha.carnet.ui.AynehaStrings
import com.ayneha.carnet.ui.components.AynehaText
import com.ayneha.carnet.ui.theme.Accent
import com.ayneha.carnet.ui.theme.Card
import com.ayneha.carnet.ui.theme.TextMuted
import com.ayneha.carnet.ui.theme.TextPrimary
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Écran Réglages du Carnet (distinct des réglages du clavier système AYNEHA Carnet/IME).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(repo: NoteRepository, onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var scale by remember { mutableStateOf(TextScaleSettings.get(context)) }
    // null = rien à signaler ; sinon nombre de notes (export ou import), ou -1 = échec
    var backupResult by remember { mutableStateOf<Int?>(null) }
    var backupWasImport by remember { mutableStateOf(false) }

    fun applyScale(new: Float) {
        scale = new.coerceIn(TextScaleSettings.MIN_SCALE, TextScaleSettings.MAX_SCALE)
        TextScaleSettings.set(context, scale)
    }

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            try {
                val json = repo.exportToJson()
                context.contentResolver.openOutputStream(uri)?.use { out ->
                    out.write(json.toByteArray(Charsets.UTF_8))
                }
                val count = org.json.JSONObject(json).getJSONArray("notes").length()
                backupWasImport = false
                backupResult = count
            } catch (_: Exception) {
                backupWasImport = false
                backupResult = -1
            }
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            try {
                val json = context.contentResolver.openInputStream(uri)
                    ?.bufferedReader()?.use { it.readText() }
                    ?: throw IllegalStateException("empty")
                val count = repo.importFromJson(json)
                backupWasImport = true
                backupResult = count
            } catch (_: Exception) {
                backupWasImport = true
                backupResult = -1
            }
        }
    }

    fun defaultBackupFileName(): String {
        val stamp = SimpleDateFormat("yyyy-MM-dd_HHmm", Locale.US).format(Date())
        return "ayneha_carnet_$stamp.json"
    }

    Scaffold(
        containerColor = androidx.compose.ui.graphics.Color.Transparent,
        topBar = {
            TopAppBar(
                title = {
                    AynehaText(
                        text = AynehaStrings.reglages,
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
        Column(
            Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .background(Card, RoundedCornerShape(16.dp))
                    .padding(20.dp)
            ) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AynehaText(
                        text = AynehaStrings.tailleDuTexte,
                        color = TextPrimary,
                        fontSize = 20.sp
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("🔤", fontSize = 18.sp)
                }
                Spacer(Modifier.height(16.dp))

                // Aperçu en direct : le texte lui-même montre l'effet, pas besoin de mot
                // "taille du texte" en AYNEHA (vocabulaire non disponible côté app).
                Box(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    AynehaText(
                        text = AynehaStrings.appTitle,
                        color = TextPrimary,
                        fontSize = (28.sp.value * scale).sp,
                        textAlign = TextAlign.Right
                    )
                }

                Spacer(Modifier.height(8.dp))

                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = {
                        applyScale(scale - 0.05f)
                    }) {
                        Icon(Icons.Default.Remove, contentDescription = null, tint = TextPrimary)
                    }
                    Slider(
                        value = scale,
                        onValueChange = { applyScale(it) },
                        valueRange = TextScaleSettings.MIN_SCALE..TextScaleSettings.MAX_SCALE,
                        modifier = Modifier.weight(1f),
                        colors = SliderDefaults.colors(
                            thumbColor = Accent,
                            activeTrackColor = Accent
                        )
                    )
                    IconButton(onClick = {
                        applyScale(scale + 0.05f)
                    }) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = TextPrimary)
                    }
                }

                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = { applyScale(TextScaleSettings.DEFAULT_SCALE) }) {
                        Text(
                            "${(scale * 100).toInt()}%",
                            color = TextMuted,
                            fontSize = 14.sp
                        )
                        Spacer(Modifier.width(8.dp))
                        AynehaText(
                            text = AynehaStrings.reinitialiser,
                            color = TextMuted,
                            fontSize = 16.sp
                        )
                        Spacer(Modifier.width(6.dp))
                        Icon(
                            Icons.Default.RestartAlt,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            Column(
                Modifier
                    .fillMaxWidth()
                    .background(Card, RoundedCornerShape(16.dp))
                    .padding(20.dp)
            ) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AynehaText(
                        text = AynehaStrings.sauvegarde,
                        color = TextPrimary,
                        fontSize = 20.sp
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("💾", fontSize = 18.sp)
                }
                Spacer(Modifier.height(16.dp))
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            backupResult = null
                            exportLauncher.launch(defaultBackupFileName())
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        AynehaText(text = AynehaStrings.exporter, color = TextPrimary, fontSize = 18.sp)
                        Text(" 📤", fontSize = 16.sp)
                    }
                    OutlinedButton(
                        onClick = {
                            backupResult = null
                            importLauncher.launch(arrayOf("*/*"))
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        AynehaText(text = AynehaStrings.importer, color = TextPrimary, fontSize = 18.sp)
                        Text(" 📥", fontSize = 16.sp)
                    }
                }

                backupResult?.let { count ->
                    Spacer(Modifier.height(12.dp))
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (count >= 0) {
                            AynehaText(
                                text = AynehaStrings.toAynehaDigits(count),
                                color = TextMuted,
                                fontSize = 18.sp
                            )
                            Spacer(Modifier.width(6.dp))
                            Text("✅", fontSize = 16.sp)
                        } else {
                            Text("❌", fontSize = 16.sp)
                        }
                    }
                }
            }
        }
    }
}
