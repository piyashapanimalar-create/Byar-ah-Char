package com.byarahchar.translate.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TranslationDao {

    @Query("SELECT * FROM translation_history ORDER BY timestamp DESC")
    fun getAllHistory(): Flow<List<TranslationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(translation: TranslationEntity)

    @Delete
    suspend fun delete(translation: TranslationEntity)

    @Query("DELETE FROM translation_history")
    suspend fun clearAll()

    @Query("SELECT * FROM translation_history WHERE burmeseText LIKE '%' || :query || '%' OR englishTranslation LIKE '%' || :query || '%' OR tamilTranslation LIKE '%' || :query || '%' ORDER BY timestamp DESC")
    fun searchHistory(query: String): Flow<List<TranslationEntity>>
}
