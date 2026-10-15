package com.byarahchar.translate

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.byarahchar.translate.data.AppDatabase
import com.byarahchar.translate.api.TranslationRepository

class ByarAhCharApp : Application() {

    lateinit var database: AppDatabase
        private set

    lateinit var repository: TranslationRepository
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this

        // Initialize local Room database
        database = AppDatabase.getDatabase(this)
        repository = TranslationRepository(database.translationDao())

        // Create notification channel for Foreground Floating Service
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Byar ah char Floating Bubble Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Keeps the floating translation bubble running over other apps"
                setShowBadge(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    companion object {
        const val CHANNEL_ID = "byar_ah_char_floating_channel"
        lateinit var instance: ByarAhCharApp
            private set
    }
}
