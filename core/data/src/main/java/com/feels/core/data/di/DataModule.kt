package com.feels.core.data.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.feels.core.data.local.FeelSDatabase
import com.feels.core.data.local.dao.CheckInDao
import com.feels.core.data.local.dao.EmotionDao
import com.feels.core.data.security.DatabaseKeyManager
import com.feels.core.data.local.seed.DatabaseSeeder
import com.feels.core.data.preferences.UserPreferencesRepositoryImpl
import com.feels.core.data.repository.CheckInRepositoryImpl
import com.feels.core.data.repository.EmotionRepositoryImpl
import com.feels.core.domain.repository.CheckInRepository
import com.feels.core.domain.repository.EmotionRepository
import com.feels.core.domain.repository.UserPreferencesRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SupportFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindEmotionRepository(impl: EmotionRepositoryImpl): EmotionRepository

    @Binds
    @Singleton
    abstract fun bindCheckInRepository(impl: CheckInRepositoryImpl): CheckInRepository

    @Binds
    @Singleton
    abstract fun bindUserPreferencesRepository(
        impl: UserPreferencesRepositoryImpl,
    ): UserPreferencesRepository

    @Binds
    @Singleton
    abstract fun bindBackupRepository(
        impl: com.feels.core.data.backup.FeelSBackupRepository,
    ): com.feels.core.domain.repository.BackupRepository
}

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    private const val DATABASE_NAME = "feels.db"

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
        keyManager: DatabaseKeyManager,
    ): FeelSDatabase {
        if (!keyManager.isEncryptedDatabaseReady()) {
            context.deleteDatabase(DATABASE_NAME)
            keyManager.markEncryptedDatabaseReady()
        }

        SQLiteDatabase.loadLibs(context)
        val factory = SupportFactory(keyManager.getDatabasePassphrase())

        return Room.databaseBuilder(context, FeelSDatabase::class.java, DATABASE_NAME)
            .openHelperFactory(factory)
            .addMigrations(MIGRATION_1_2)
            .build()
    }

    @Provides
    fun provideEmotionDao(database: FeelSDatabase): EmotionDao = database.emotionDao()

    @Provides
    fun provideCheckInDao(database: FeelSDatabase): CheckInDao = database.checkInDao()
}

private val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Version 2 kept the same emotion and check-in tables.
    }
}
