// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (C) 2026 MAIGUS

package com.ayneha.keyboard

import com.ayneha.carnet.R

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Switch
import androidx.appcompat.app.AppCompatActivity

object AynehaPrefs {
    const val PREFS_NAME = "ayneha_prefs"
    const val KEY_HAPTIC = "haptic_enabled"
}

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val prefs = getSharedPreferences(AynehaPrefs.PREFS_NAME, MODE_PRIVATE)
        val switchHaptic = findViewById<Switch>(R.id.switchHaptic)
        switchHaptic.isChecked = prefs.getBoolean(AynehaPrefs.KEY_HAPTIC, true)
        switchHaptic.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean(AynehaPrefs.KEY_HAPTIC, isChecked).apply()
        }

        findViewById<android.widget.Button>(R.id.btnSystemSettings).setOnClickListener {
            startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS))
        }
    }
}
