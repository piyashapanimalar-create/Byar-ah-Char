package com.byarahchar.translate.api

import com.byarahchar.translate.data.TranslationDao
import com.byarahchar.translate.data.TranslationEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

/**
 * Dual Translation Repository: Translates Burmese -> Both English and Tamil in parallel.
 */
class TranslationRepository(private val dao: TranslationDao) {

    private val api = TranslationApi()

    suspend fun translateBurmese(text: String): Result<Pair<String, String>> = withContext(Dispatchers.IO) {
        try {
            // Run English (tl=en) and Tamil (tl=ta) translations in parallel coroutines
            val englishDeferred = async { api.translate(text, "en") }
            val tamilDeferred = async { api.translate(text, "ta") }

            val english = englishDeferred.await()
            val tamil = tamilDeferred.await()

            Result.success(Pair(english, tamil))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getAllHistory(): Flow<List<TranslationEntity>> = dao.getAllHistory()

    fun searchHistory(query: String): Flow<List<TranslationEntity>> = dao.searchHistory(query)

    suspend fun saveHistory(entity: TranslationEntity) = withContext(Dispatchers.IO) {
        dao.insert(entity)
    }

    suspend fun deleteHistory(entity: TranslationEntity) = withContext(Dispatchers.IO) {
        dao.delete(entity)
    }

    suspend fun clearAllHistory() = withContext(Dispatchers.IO) {
        dao.clearAll()
    }
}
