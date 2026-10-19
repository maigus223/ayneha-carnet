// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (C) 2026 MAIGUS

package com.ayneha.carnet.ui.privacy

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ayneha.carnet.ui.theme.Accent
import com.ayneha.carnet.ui.theme.Bg
import com.ayneha.carnet.ui.theme.TextMuted
import com.ayneha.carnet.ui.theme.TextPrimary

/**
 * Écran Confidentialité. Contenu en français (pas en AYNEHA) : texte informatif/légal
 * dense, avec une adresse e-mail - une exception assumée à la règle "zéro latin dans
 * l'UI", validée avec l'utilisateur. Lecture naturelle gauche-à-droite (paragraphes
 * français), contrairement au reste de l'app qui est en AYNEHA/RTL.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyScreen(onBack: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val versionName = remember {
        try {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: ""
        } catch (_: Exception) {
            ""
        }
    }
    Scaffold(
        containerColor = Bg,
        topBar = {
            TopAppBar(
                title = { Text("Confidentialité", color = TextPrimary, fontSize = 20.sp) },
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
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            Text(
                "Version $versionName - Dernière mise à jour : 11 octobre 2026",
                color = TextMuted,
                fontSize = 14.sp
            )
            Spacer(Modifier.height(16.dp))
            Text(
                "AYNEHA Carnet ne collecte aucune donnée. Toutes tes notes restent uniquement sur ton téléphone, dans une base de données locale : rien n'est jamais envoyé sur un serveur.",
                color = TextPrimary,
                fontSize = 16.sp,
                lineHeight = 24.sp
            )
            Spacer(Modifier.height(16.dp))
            Text(
                "Aucun compte, aucune inscription, aucune publicité, aucun traceur, aucune bibliothèque d'analyse tierce.",
                color = TextPrimary,
                fontSize = 16.sp,
                lineHeight = 24.sp
            )
            Spacer(Modifier.height(16.dp))
            Text(
                "L'export/import de sauvegarde utilise le sélecteur de fichiers d'Android : tu choisis toi-même où enregistrer ou lire le fichier, l'app n'y a accès à aucun autre moment.",
                color = TextPrimary,
                fontSize = 16.sp,
                lineHeight = 24.sp
            )
            Spacer(Modifier.height(16.dp))
            Text(
                "Pour écrire, AYNEHA Carnet utilise le clavier AYNEHA, qui fonctionne entièrement hors-ligne. Comme tout clavier système, il peut techniquement recevoir ce que tu tapes quand tu l'actives dans les réglages Android ; il ne transmet ni n'enregistre rien en dehors de cet appareil.",
                color = TextPrimary,
                fontSize = 16.sp,
                lineHeight = 24.sp
            )
            Spacer(Modifier.height(16.dp))
            Text(
                "Logiciel libre : AYNEHA Carnet est un logiciel libre, développé par MAIGUS avec l'aide d'outils d'IA (Claude, Anthropic). Il est publié sous licence GPL v3 ou ultérieure : tu peux l'utiliser, l'étudier, le modifier et le partager. Toute version modifiée distribuée doit rester libre, sous la même licence. Police : SIL Open Font License 1.1. Code source : github.com/maigus223/ayneha-carnet",
                color = TextPrimary,
                fontSize = 16.sp,
                lineHeight = 24.sp
            )
            Spacer(Modifier.height(16.dp))
            Spacer(Modifier.height(32.dp))
            Text(
                "Développé par MAIGUS : gwokmt2q@duck.com",
                color = Accent,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
