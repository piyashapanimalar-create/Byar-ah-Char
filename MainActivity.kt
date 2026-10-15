package com.byarahchar.translate

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.byarahchar.translate.service.FloatingBubbleService
import com.byarahchar.translate.ui.HomeScreen
import com.byarahchar.translate.ui.theme.ByarAhCharTheme

/**
 * Main Activity (Features 1, 2, 5, 6, 7, 8, 12).
 * Material Design 3 interface with dynamic color theming.
 * Includes overlay permission management for OnePlus Nord CE 2 Lite 5G.
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ByarAhCharTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    var hasOverlayPermission by remember {
                        mutableStateOf(checkOverlayPermission())
                    }

                    HomeScreen(
                        hasOverlayPermission = hasOverlayPermission,
                        onRequestOverlayPermission = {
                            requestOverlayPermission()
                        },
                        onToggleFloatingBubble = { start ->
                            if (start) {
                                if (checkOverlayPermission()) {
                                    startFloatingService()
                                } else {
                                    requestOverlayPermission()
                                }
                            } else {
                                stopFloatingService()
                            }
                        }
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Re-check permission when returning from OnePlus OxygenOS Settings
        checkOverlayPermission()
    }

    private fun checkOverlayPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(this)
        } else {
            true
        }
    }

    private fun requestOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Toast.makeText(
                this,
                "Please enable 'Allow display over other apps' for Byar ah char Translate",
                Toast.LENGTH_LONG
            ).show()
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            startActivity(intent)
        }
    }

    private fun startFloatingService() {
        val intent = Intent(this, FloatingBubbleService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
        Toast.makeText(this, "Floating bubble activated!", Toast.LENGTH_SHORT).show()
    }

    private fun stopFloatingService() {
        val intent = Intent(this, FloatingBubbleService::class.java)
        stopService(intent)
        Toast.makeText(this, "Floating bubble stopped", Toast.LENGTH_SHORT).show()
    }
}
