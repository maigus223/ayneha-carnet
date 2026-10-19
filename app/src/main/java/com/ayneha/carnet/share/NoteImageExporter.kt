// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (C) 2026 MAIGUS

package com.ayneha.carnet.share

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.net.Uri
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.core.content.res.ResourcesCompat
import com.ayneha.carnet.R
import com.ayneha.carnet.data.Note
import java.io.File
import java.io.FileOutputStream

/**
 * Convertit une note AYNEHA en JPG puis lance le partage système.
 * (Les autres apps ne lisent pas les glyphes AYNEHA, U+10FF7C-U+10FFDD.)
 */
object NoteImageExporter {

    private const val WIDTH = 1080
    private const val PADDING = 64
    private const val BG = 0xFF0D1117.toInt()
    private const val TITLE_COLOR = 0xFFE6EDF3.toInt()
    private const val BODY_COLOR = 0xFFC9D1D9.toInt()
    private const val ACCENT = 0xFFFF9F1C.toInt()

    fun shareAsJpg(context: Context, note: Note) {
        try {
            val bmp = render(context, note)
            val dir = File(context.cacheDir, "shared_notes").apply { mkdirs() }
            val file = File(dir, "ayneha_note_${System.currentTimeMillis()}.jpg")
            FileOutputStream(file).use { out ->
                bmp.compress(Bitmap.CompressFormat.JPEG, 92, out)
            }
            bmp.recycle()

            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val send = Intent(Intent.ACTION_SEND).apply {
                type = "image/jpeg"
                putExtra(Intent.EXTRA_STREAM, uri)
                clipData = ClipData.newRawUri("", uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(send, null).apply {
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(chooser)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(
                context,
                "Share error: ${e.javaClass.simpleName}",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun render(context: Context, note: Note): Bitmap {
        val typeface = try {
            ResourcesCompat.getFont(context, R.font.ayneha_pro)
        } catch (_: Exception) {
            null
        } ?: Typeface.DEFAULT

        val titlePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = TITLE_COLOR
            textSize = 56f
            this.typeface = typeface
            textAlign = Paint.Align.RIGHT
        }
        val bodyPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = BODY_COLOR
            textSize = 48f
            this.typeface = typeface
            textAlign = Paint.Align.RIGHT
        }

        val contentWidth = (WIDTH - PADDING * 2).coerceAtLeast(200)
        val titleText = note.title.ifBlank { "-" }
        val bodyText = note.body.ifBlank { " " }

        val titleLayout = buildLayout(titleText, titlePaint, contentWidth)
        val bodyLayout = buildLayout(bodyText, bodyPaint, contentWidth)

        val height = (PADDING * 2 + titleLayout.height + 32 + bodyLayout.height + 48)
            .coerceAtLeast(400)

        val bmp = Bitmap.createBitmap(WIDTH, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        canvas.drawColor(BG)

        var y = PADDING.toFloat()

        val barPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = ACCENT }
        canvas.drawRect(0f, 0f, WIDTH.toFloat(), 8f, barPaint)

        canvas.save()
        canvas.translate(PADDING.toFloat(), y)
        titleLayout.draw(canvas)
        canvas.restore()
        y += titleLayout.height + 24

        val sep = Paint().apply {
            color = 0xFF30363D.toInt()
            strokeWidth = 2f
        }
        canvas.drawLine(PADDING.toFloat(), y, (WIDTH - PADDING).toFloat(), y, sep)
        y += 24

        canvas.save()
        canvas.translate(PADDING.toFloat(), y)
        bodyLayout.draw(canvas)
        canvas.restore()

        return bmp
    }

    private fun buildLayout(text: String, paint: TextPaint, width: Int): StaticLayout {
        return StaticLayout.Builder.obtain(text, 0, text.length, paint, width)
            .setAlignment(Layout.Alignment.ALIGN_OPPOSITE)
            .setLineSpacing(0f, 1.2f)
            .setIncludePad(false)
            .build()
    }
}
