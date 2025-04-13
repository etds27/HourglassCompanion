package com.etds.hourglass.data.game.local.db

import android.content.Context
import androidx.room.Room
import com.etds.hourglass.data.game.local.db.daos.GameDao
import com.etds.hourglass.data.game.local.db.daos.SettingsDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppDatabaseModule {
    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext appContext: Context): AppDatabase {
        return Room.databaseBuilder(
            appContext,
            AppDatabase::class.java,
            "app_database"
        ).build()
    }

    @Provides
    fun provideGameDao(appDatabase: AppDatabase): GameDao {
        return appDatabase.gameDao()
    }

    @Provides
    fun provideSettingsDao(appDatabase: AppDatabase): SettingsDao {
        return appDatabase.settingsDao()
    }
}