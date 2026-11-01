// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (C) 2026 MAIGUS

package com.ayneha.carnet

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.ayneha.carnet.data.NoteDatabase
import com.ayneha.carnet.data.NoteRepository
import com.ayneha.carnet.ui.editor.NoteEditorScreen
import com.ayneha.carnet.ui.editor.NoteEditorViewModel
import com.ayneha.carnet.ui.list.NoteListScreen
import com.ayneha.carnet.ui.list.NoteListViewModel
import com.ayneha.carnet.ui.privacy.PrivacyScreen
import com.ayneha.carnet.ui.settings.LocalAynehaTextScale
import com.ayneha.carnet.ui.settings.SettingsScreen
import com.ayneha.carnet.ui.settings.TextScaleSettings
import com.ayneha.carnet.ui.theme.AynehaCarnetTheme
import com.ayneha.carnet.ui.trash.TrashScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // Migration unique des notes de l'ancienne plage U+E000-E061 vers U+10FF7C-U+10FFDD.
        PlaneMigration.runOnce(this)
        setContent {
            AynehaCarnetTheme {
                AynehaCarnetNav()
            }
        }
        // Mise à jour depuis ayneha.org : tirer la liste des notes vers le bas (voir MiseAJourUi.kt).
        maj = MiseAJourUi(
            this,
            accent = 0xFFFF9F1C.toInt(),
            fond = 0xFF161B22.toInt(),
            encre = 0xFFE6EDF3.toInt(),
            police = androidx.core.content.res.ResourcesCompat.getFont(this, R.font.ayneha_pro),
            geste = false
        )
        MiseAJourUi.courant = maj
    }

    private lateinit var maj: MiseAJourUi

    override fun onResume() {
        super.onResume()
        if (::maj.isInitialized) { MiseAJourUi.courant = maj; maj.surReprise() }
    }

    override fun onDestroy() {
        if (MiseAJourUi.courant === maj) MiseAJourUi.courant = null
        super.onDestroy()
    }
}

@Composable
fun AynehaCarnetNav() {
    val context = LocalContext.current
    val repo = remember {
        val db = NoteDatabase.get(context)
        NoteRepository(db.noteDao())
    }
    val nav = rememberNavController()
    var textScale by remember { mutableStateOf(TextScaleSettings.get(context)) }

    CompositionLocalProvider(LocalAynehaTextScale provides textScale) {
        NavHost(navController = nav, startDestination = "list") {
            composable("list") {
                val vm: NoteListViewModel = viewModel(
                    factory = NoteListViewModel.Factory(repo)
                )
                NoteListScreen(
                    viewModel = vm,
                    onOpenNote = { id -> nav.navigate("editor/$id") },
                    onNewNote = { nav.navigate("editor/0") },
                    onOpenSettings = { nav.navigate("reglages") },
                    onOpenTrash = { nav.navigate("corbeille") },
                    onOpenPrivacy = { nav.navigate("confidentialite") }
                )
            }
            composable(
                route = "editor/{noteId}",
                arguments = listOf(navArgument("noteId") { type = NavType.LongType })
            ) { entry ->
                val noteId = entry.arguments?.getLong("noteId") ?: 0L
                val vm: NoteEditorViewModel = viewModel(
                    factory = NoteEditorViewModel.Factory(repo, if (noteId == 0L) null else noteId)
                )
                NoteEditorScreen(
                    viewModel = vm,
                    onBack = { nav.popBackStack() },
                    onDeleted = {
                        // TODO: appeler delete dans VM si besoin
                        nav.popBackStack()
                    }
                )
            }
            composable("reglages") {
                SettingsScreen(
                    repo = repo,
                    onBack = {
                        // Recharge la valeur persistée pour que le reste de l'app la reflète
                        // immédiatement au retour (slider modifie l'état local de l'écran).
                        textScale = TextScaleSettings.get(context)
                        nav.popBackStack()
                    }
                )
            }
            composable("corbeille") {
                TrashScreen(
                    repo = repo,
                    onBack = { nav.popBackStack() }
                )
            }
            composable("confidentialite") {
                PrivacyScreen(onBack = { nav.popBackStack() })
            }
        }
    }
}
