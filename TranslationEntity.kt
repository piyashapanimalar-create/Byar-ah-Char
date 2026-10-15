package com.byarahchar.translate.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Feature 7: Local translation history storage using Android Room database.
 */
@Entity(tableName = "translation_history")
data class TranslationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val burmeseText: String,
    val englishTranslation: String,
    val tamilTranslation: String,
    val timestamp: Long = System.currentTimeMillis()
)
