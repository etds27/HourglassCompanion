package com.etds.hourglass.data.game.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.etds.hourglass.data.game.local.db.daos.GameDao
import com.etds.hourglass.data.game.local.db.daos.BuzzerSettingsDao
import com.etds.hourglass.data.game.local.db.daos.SequentialSettingsDao
import com.etds.hourglass.data.game.local.db.entity.BuzzerSettingsEntity
import com.etds.hourglass.data.game.local.db.entity.GameEntity
import com.etds.hourglass.data.game.local.db.entity.SequentialSettingsEntity

@Database(
    version = 2,
    entities = [GameEntity::class, BuzzerSettingsEntity::class, SequentialSettingsEntity::class])
abstract class AppDatabase: RoomDatabase() {
    abstract fun gameDao(): GameDao
    abstract fun buzzerSettingsDao(): BuzzerSettingsDao
    abstract fun sequentialSettingsDao(): SequentialSettingsDao
}