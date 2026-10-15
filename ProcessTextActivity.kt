package com.byarahchar.translate.activity

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.byarahchar.translate.ByarAhCharApp
import com.byarahchar.translate.R
import com.byarahchar.translate.data.TranslationEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Feature 10: System-wide Text Selection Translation.
 * When the user highlights Burmese text in any app (Chrome, WhatsApp, Notes, Facebook),
 * Android's context menu displays "Byar ah char Translate".
 * Tapping it launches this lightweight translucent dialog activity, instantly translating into English & Tamil.
 */
class ProcessTextActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_process_text)

        val tvOriginal = findViewById<TextView>(R.id.pt_original_text)
        val tvEnglish = findViewById<TextView>(R.id.pt_english_result)
        val tvTamil = findViewById<TextView>(R.id.pt_tamil_result)
        val btnCopyEng = findViewById<ImageButton>(R.id.pt_copy_english)
        val btnCopyTam = findViewById<ImageButton>(R.id.pt_copy_tamil)
        val btnClose = findViewById<ImageButton>(R.id.pt_close)
        val progressBar = findViewById<ProgressBar>(R.id.pt_progress)

        // Retrieve highlighted text from system intent
        val selectedText = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            intent.getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT)?.toString() ?: ""
        } else {
            intent.getStringExtra(Intent.EXTRA_TEXT) ?: ""
        }

        if (selectedText.isBlank()) {
            Toast.makeText(this, "No text selected", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        tvOriginal.text = selectedText

        btnClose.setOnClickListener { finish() }

        btnCopyEng.setOnClickListener {
            copyText("English Translation", tvEnglish.text.toString())
            Toast.makeText(this, "Copied English translation", Toast.LENGTH_SHORT).show()
        }

        btnCopyTam.setOnClickListener {
            copyText("Tamil Translation", tvTamil.text.toString())
            Toast.makeText(this, "Copied Tamil translation", Toast.LENGTH_SHORT).show()
        }

        // Execute translation
        progressBar.visibility = View.VISIBLE
        CoroutineScope(Dispatchers.IO).launch {
            val result = ByarAhCharApp.instance.repository.translateBurmese(selectedText)
            withContext(Dispatchers.Main) {
                progressBar.visibility = View.GONE
                if (result.isSuccess) {
                    val (english, tamil) = result.getOrThrow()
                    tvEnglish.text = english
                    tvTamil.text = tamil

                    // Save to local history
                    ByarAhCharApp.instance.repository.saveHistory(
                        TranslationEntity(
                            burmeseText = selectedText,
                            englishTranslation = english,
                            tamilTranslation = tamil,
                            timestamp = System.currentTimeMillis()
                        )
                    )
                } else {
                    tvEnglish.text = "Translation error"
                    tvTamil.text = "பிழை ஏற்பட்டது"
                    Toast.makeText(this@ProcessTextActivity, "Could not translate text.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun copyText(label: String, text: String) {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, text)
        clipboard.setPrimaryClip(clip)
    }
}
