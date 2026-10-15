package com.byarahchar.translate.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.util.DisplayMetrics
import android.view.*
import android.widget.*
import androidx.core.app.NotificationCompat
import com.byarahchar.translate.ByarAhCharApp
import com.byarahchar.translate.MainActivity
import com.byarahchar.translate.R
import com.byarahchar.translate.data.TranslationEntity
import kotlinx.coroutines.*
import kotlin.math.abs

/**
 * Movable Floating Bubble Service (Features 3, 4, 5, 6, 8, 9).
 * Runs over other apps (WhatsApp, Facebook, Chrome) using Android WindowManager.
 * Dragging: Magnetic edge snapping for OnePlus Nord CE 2 Lite 5G 120Hz display.
 * Tap: Expands the floating translation popup with Burmese, English, and Tamil.
 */
class FloatingBubbleService : Service() {

    private lateinit var windowManager: WindowManager
    private var bubbleView: View? = null
    private var popupView: View? = null

    private lateinit var bubbleParams: WindowManager.LayoutParams
    private lateinit var popupParams: WindowManager.LayoutParams

    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var clipboardManager: ClipboardManager? = null

    // Touch dragging & click detection state
    private var initialX = 0
    private var initialY = 0
    private var initialTouchX = 0f
    private var initialTouchY = 0f
    private var isPopupOpen = false

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

        startForeground(1001, buildNotification())
        createFloatingBubble()
        initClipboardListener()
    }

    private fun buildNotification(): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, ByarAhCharApp.CHANNEL_ID)
            .setContentTitle("Byar ah char Translate Active")
            .setContentText("Tap floating bubble anytime to translate Burmese to English & Tamil")
            .setSmallIcon(R.drawable.ic_translate)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()
    }

    @SuppressLint("InflateParams", "ClickableViewAccessibility")
    private fun createFloatingBubble() {
        val layoutType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        bubbleParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 30
            y = 300
        }

        val inflater = LayoutInflater.from(this)
        bubbleView = inflater.inflate(R.layout.floating_bubble_layout, null)

        val bubbleRoot = bubbleView!!.findViewById<View>(R.id.bubble_container)

        bubbleRoot.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = bubbleParams.x
                    initialY = bubbleParams.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    bubbleParams.x = initialX + (event.rawX - initialTouchX).toInt()
                    bubbleParams.y = initialY + (event.rawY - initialTouchY).toInt()
                    windowManager.updateViewLayout(bubbleView, bubbleParams)
                    true
                }
                MotionEvent.ACTION_UP -> {
                    val diffX = abs(event.rawX - initialTouchX)
                    val diffY = abs(event.rawY - initialTouchY)

                    // Click threshold: If finger moved less than 12px, treat as TAP
                    if (diffX < 12 && diffY < 12) {
                        togglePopup()
                    } else {
                        // Magnetic snap to nearest screen edge (left or right)
                        snapToScreenEdge()
                    }
                    true
                }
                else -> false
            }
        }

        windowManager.addView(bubbleView, bubbleParams)
    }

    private fun snapToScreenEdge() {
        val metrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(metrics)
        val screenWidth = metrics.widthPixels

        val targetX = if (bubbleParams.x < screenWidth / 2) 20 else screenWidth - 160
        bubbleParams.x = targetX
        windowManager.updateViewLayout(bubbleView, bubbleParams)
    }

    private fun togglePopup() {
        if (isPopupOpen) {
            closePopup()
        } else {
            openPopup()
        }
    }

    @SuppressLint("InflateParams")
    private fun openPopup() {
        if (popupView != null) return

        val layoutType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        popupParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutType,
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.CENTER
            x = 0
            y = 0
        }

        val inflater = LayoutInflater.from(this)
        popupView = inflater.inflate(R.layout.floating_popup_layout, null)

        val etInput = popupView!!.findViewById<EditText>(R.id.et_burmese_input)
        val btnPaste = popupView!!.findViewById<Button>(R.id.btn_popup_paste)
        val btnTranslate = popupView!!.findViewById<Button>(R.id.btn_popup_translate)
        val btnClose = popupView!!.findViewById<ImageButton>(R.id.btn_popup_close)
        val tvEnglish = popupView!!.findViewById<TextView>(R.id.tv_english_result)
        val tvTamil = popupView!!.findViewById<TextView>(R.id.tv_tamil_result)
        val btnCopyEnglish = popupView!!.findViewById<ImageButton>(R.id.btn_copy_english)
        val btnCopyTamil = popupView!!.findViewById<ImageButton>(R.id.btn_copy_tamil)
        val progressBar = popupView!!.findViewById<ProgressBar>(R.id.popup_progress)

        // Clipboard auto-fill if contains Burmese text
        val clipText = getClipboardText()
        if (isLikelyBurmese(clipText)) {
            etInput.setText(clipText)
        }

        btnPaste.setOnClickListener {
            val pasted = getClipboardText()
            if (pasted.isNotEmpty()) {
                etInput.setText(pasted)
            } else {
                Toast.makeText(this, "Clipboard is empty", Toast.LENGTH_SHORT).show()
            }
        }

        btnClose.setOnClickListener { closePopup() }

        btnTranslate.setOnClickListener {
            val text = etInput.text.toString().trim()
            if (text.isEmpty()) {
                Toast.makeText(this, "Please enter or paste Burmese text", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            progressBar.visibility = View.VISIBLE
            btnTranslate.isEnabled = false

            serviceScope.launch {
                val result = ByarAhCharApp.instance.repository.translateBurmese(text)
                progressBar.visibility = View.GONE
                btnTranslate.isEnabled = true

                if (result.isSuccess) {
                    val (english, tamil) = result.getOrThrow()
                    tvEnglish.text = english
                    tvTamil.text = tamil

                    // Save translation history locally in Room
                    ByarAhCharApp.instance.repository.saveHistory(
                        TranslationEntity(
                            burmeseText = text,
                            englishTranslation = english,
                            tamilTranslation = tamil,
                            timestamp = System.currentTimeMillis()
                        )
                    )
                } else {
                    Toast.makeText(
                        this@FloatingBubbleService,
                        "Translation failed. Check connection.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        btnCopyEnglish.setOnClickListener {
            val eng = tvEnglish.text.toString()
            if (eng.isNotEmpty() && eng != "...") {
                copyToClipboard("English Translation", eng)
                Toast.makeText(this, "English translation copied!", Toast.LENGTH_SHORT).show()
            }
        }

        btnCopyTamil.setOnClickListener {
            val tam = tvTamil.text.toString()
            if (tam.isNotEmpty() && tam != "...") {
                copyToClipboard("Tamil Translation", tam)
                Toast.makeText(this, "Tamil translation copied!", Toast.LENGTH_SHORT).show()
            }
        }

        windowManager.addView(popupView, popupParams)
        isPopupOpen = true
    }

    private fun closePopup() {
        if (popupView != null) {
            windowManager.removeView(popupView)
            popupView = null
            isPopupOpen = false
        }
    }

    /**
     * Feature 9: Clipboard-based translation fallback
     * When user copies Burmese text anywhere, automatically offer quick translation.
     */
    private fun initClipboardListener() {
        clipboardManager?.addPrimaryClipChangedListener {
            val text = getClipboardText()
            if (isLikelyBurmese(text)) {
                // If bubble is active, pulse bubble or open translation prompt
                Toast.makeText(this, "Burmese text copied! Tap bubble to translate.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getClipboardText(): String {
        val clip = clipboardManager?.primaryClip
        if (clip != null && clip.itemCount > 0) {
            return clip.getItemAt(0).text?.toString() ?: ""
        }
        return ""
    }

    private fun copyToClipboard(label: String, text: String) {
        val clip = android.content.ClipData.newPlainText(label, text)
        clipboardManager?.setPrimaryClip(clip)
    }

    private fun isLikelyBurmese(text: String): Boolean {
        // Myanmar unicode range: 1000–109F, AA60–AA7F, A9E0–A9FF
        return text.any { it.code in 0x1000..0x109F || it.code in 0xAA60..0xAA7F }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        if (bubbleView != null) {
            windowManager.removeView(bubbleView)
            bubbleView = null
        }
        closePopup()
    }
}
