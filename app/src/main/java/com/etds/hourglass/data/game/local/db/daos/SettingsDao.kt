package com.etds.hourglass.data.game.local.db.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.etds.hourglass.data.game.local.db.entity.GameEntity
import com.etds.hourglass.data.game.local.db.entity.SettingsEntity


@Dao
interface SettingsDao {
    @Query("SELECT * FROM SettingsEntity") suspend fun getAll(): List<SettingsEntity>
    @Query("SELECT configName FROM SettingsEntity") suspend fun getAllNames(): List<String>
    @Query("SELECT * FROM SettingsEntity WHERE configName = :configName") suspend fun getByName(configName: String): SettingsEntity
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insert(settingsEntity: SettingsEntity)
    @Insert(onConflict = OnConflictStrategy.IGNORE) suspend fun insertIfNotExists(settingsEntity: SettingsEntity)
    @Update(onConflict = OnConflictStrategy.REPLACE) suspend fun update(settingsEntity: SettingsEntity)
    @Delete suspend fun delete(settingsEntity: SettingsEntity)
    @Query("DELETE FROM SettingsEntity WHERE configName = :configName") suspend fun delete(configName: String)
}