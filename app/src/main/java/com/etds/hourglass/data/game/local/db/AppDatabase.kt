package com.etds.hourglass.data.game.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.etds.hourglass.data.game.local.db.daos.GameDao
import com.etds.hourglass.data.game.local.db.daos.SettingsDao
import com.etds.hourglass.data.game.local.db.entity.GameEntity
import com.etds.hourglass.data.game.local.db.entity.SettingsEntity

@Database(entities = [GameEntity::class, SettingsEntity::class], version = 1)
abstract class AppDatabase: RoomDatabase() {
    abstract fun gameDao(): GameDao
    abstract fun settingsDao(): SettingsDao
}