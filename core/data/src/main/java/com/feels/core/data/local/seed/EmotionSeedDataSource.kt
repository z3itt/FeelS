package com.feels.core.data.local.seed

import android.content.Context
import com.feels.core.data.local.dao.EmotionDao
import com.feels.core.data.local.entity.EmotionEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import javax.inject.Inject

class EmotionSeedDataSource @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val json = Json { ignoreUnknownKeys = true }

    fun loadSeedEmotions(): List<EmotionEntity> {
        val raw = context.assets.open("emotions.json").bufferedReader().use { it.readText() }
        val payload = json.decodeFromString<EmotionSeedPayload>(raw)
        return payload.emotions.map { item ->
            EmotionEntity(
                id = item.id,
                label = item.label,
                tier = item.tier,
                parentId = item.parentId,
                colorHex = item.colorHex,
                distressLevel = item.distressLevel,
                sortOrder = item.sortOrder,
            )
        }
    }
}

@Serializable
private data class EmotionSeedPayload(
    val version: Int,
    val emotions: List<EmotionSeedItem>,
)

@Serializable
private data class EmotionSeedItem(
    val id: String,
    val label: String,
    val tier: Int,
    @SerialName("parentId") val parentId: String? = null,
    val colorHex: String,
    val distressLevel: Int,
    val sortOrder: Int,
)

class DatabaseSeeder @Inject constructor(
    private val emotionDao: EmotionDao,
    private val seedDataSource: EmotionSeedDataSource,
) {
    suspend fun seedIfEmpty() {
        val seedEmotions = seedDataSource.loadSeedEmotions()
        if (emotionDao.count() == 0) {
            emotionDao.insertAll(seedEmotions)
            return
        }
        val missing = seedEmotions.filter { emotion ->
            emotionDao.getById(emotion.id) == null
        }
        if (missing.isNotEmpty()) {
            emotionDao.insertAll(missing)
        }
        seedEmotions.find { it.id == "unsure" }?.let { unsure ->
            emotionDao.insertAll(listOf(unsure))
        }
    }
}
