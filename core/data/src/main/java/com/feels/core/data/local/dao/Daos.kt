package com.feels.core.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.feels.core.data.local.entity.CheckInEntity
import com.feels.core.data.local.entity.EmotionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EmotionDao {
    @Query("SELECT * FROM emotions ORDER BY tier ASC, sortOrder ASC")
    fun observeAll(): Flow<List<EmotionEntity>>

    @Query("SELECT * FROM emotions WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): EmotionEntity?

    @Query("SELECT * FROM emotions WHERE parentId = :parentId ORDER BY sortOrder ASC")
    suspend fun getChildren(parentId: String): List<EmotionEntity>

    @Query("SELECT * FROM emotions WHERE tier = 1 ORDER BY sortOrder ASC")
    suspend fun getPrimaryEmotions(): List<EmotionEntity>

    @Query("SELECT COUNT(*) FROM emotions")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(emotions: List<EmotionEntity>)
}

@Dao
interface CheckInDao {
    @Query("SELECT * FROM check_ins ORDER BY timestampMillis DESC LIMIT :limit")
    fun observeRecent(limit: Int): Flow<List<CheckInEntity>>

    @Query("SELECT * FROM check_ins WHERE timestampMillis >= :sinceMillis ORDER BY timestampMillis DESC")
    fun observeSince(sinceMillis: Long): Flow<List<CheckInEntity>>

    @Query("SELECT * FROM check_ins WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): CheckInEntity?

    @Query("SELECT * FROM check_ins ORDER BY timestampMillis DESC")
    suspend fun getAll(): List<CheckInEntity>

    @Insert
    suspend fun insert(checkIn: CheckInEntity): Long

    @Update
    suspend fun update(checkIn: CheckInEntity)

    @Query("DELETE FROM check_ins WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM check_ins")
    suspend fun deleteAll()
}
