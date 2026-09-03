package com.feels.core.data.repository

import com.feels.core.data.local.dao.CheckInDao
import com.feels.core.data.local.dao.EmotionDao
import com.feels.core.data.mapper.toDomain
import com.feels.core.data.mapper.toEntity
import com.feels.core.domain.model.CheckIn
import com.feels.core.domain.model.Emotion
import com.feels.core.domain.repository.CheckInRepository
import com.feels.core.domain.repository.EmotionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EmotionRepositoryImpl @Inject constructor(
    private val emotionDao: EmotionDao,
) : EmotionRepository {

    override fun observeAllEmotions(): Flow<List<Emotion>> =
        emotionDao.observeAll().map { entities -> entities.map { it.toDomain() } }

    override suspend fun getEmotionById(id: String): Emotion? =
        emotionDao.getById(id)?.toDomain()

    override suspend fun getChildren(parentId: String): List<Emotion> =
        emotionDao.getChildren(parentId).map { it.toDomain() }

    override suspend fun getPrimaryEmotions(): List<Emotion> =
        emotionDao.getPrimaryEmotions().map { it.toDomain() }
}

@Singleton
class CheckInRepositoryImpl @Inject constructor(
    private val checkInDao: CheckInDao,
) : CheckInRepository {

    override fun observeRecentCheckIns(limit: Int): Flow<List<CheckIn>> =
        checkInDao.observeRecent(limit).map { entities -> entities.map { it.toDomain() } }

    override fun observeCheckInsSince(sinceMillis: Long): Flow<List<CheckIn>> =
        checkInDao.observeSince(sinceMillis).map { entities -> entities.map { it.toDomain() } }

    override suspend fun getCheckInById(id: Long): CheckIn? =
        checkInDao.getById(id)?.toDomain()

    override suspend fun getAllCheckIns(): List<CheckIn> =
        checkInDao.getAll().map { it.toDomain() }

    override suspend fun logCheckIn(checkIn: CheckIn): Long =
        checkInDao.insert(checkIn.toEntity())

    override suspend fun updateCheckIn(checkIn: CheckIn) {
        checkInDao.update(checkIn.toEntity())
    }

    override suspend fun deleteCheckIn(id: Long) {
        checkInDao.deleteById(id)
    }

    override suspend fun clearAllCheckIns() {
        checkInDao.deleteAll()
    }
}
