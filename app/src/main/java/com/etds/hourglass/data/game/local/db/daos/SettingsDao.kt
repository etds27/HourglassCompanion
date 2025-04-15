package com.etds.hourglass.data.game.local.db.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.etds.hourglass.data.game.local.db.entity.BuzzerSettingsEntity
import com.etds.hourglass.data.game.local.db.entity.SequentialSettingsEntity
import com.etds.hourglass.data.game.local.db.entity.SettingsEntity

interface SettingsDao<T> {
    suspend fun getAll(): List<T>
    suspend fun getAllNames(): List<String>
    suspend fun getByName(configName: String): T
    suspend fun insert(settingsEntity: T)
    suspend fun insertIfNotExists(settingsEntity: T)
    suspend fun update(settingsEntity: T)
    suspend fun delete(settingsEntity: T)
    suspend fun delete(configName: String)
}

@Dao
interface BuzzerSettingsDao: SettingsDao<BuzzerSettingsEntity> {
    @Query("SELECT * FROM BuzzerSettingsEntity")
    override suspend fun getAll(): List<BuzzerSettingsEntity>

    @Query("SELECT configName FROM BuzzerSettingsEntity")
    override suspend fun getAllNames(): List<String>

    @Query("SELECT * FROM BuzzerSettingsEntity WHERE configName = :configName")
    override suspend fun getByName(configName: String): BuzzerSettingsEntity

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    override suspend fun insert(settingsEntity: BuzzerSettingsEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    override suspend fun insertIfNotExists(settingsEntity: BuzzerSettingsEntity)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    override suspend fun update(settingsEntity: BuzzerSettingsEntity)

    @Delete
    override suspend fun delete(settingsEntity: BuzzerSettingsEntity)

    @Query("DELETE FROM BuzzerSettingsEntity WHERE configName = :configName")
    override suspend fun delete(configName: String)
}

@Dao
interface SequentialSettingsDao: SettingsDao<SequentialSettingsEntity> {
    @Query("SELECT * FROM SequentialSettingsEntity")
    override suspend fun getAll(): List<SequentialSettingsEntity>

    @Query("SELECT configName FROM SequentialSettingsEntity")
    override suspend fun getAllNames(): List<String>

    @Query("SELECT * FROM SequentialSettingsEntity WHERE configName = :configName")
    override suspend fun getByName(configName: String): SequentialSettingsEntity

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    override suspend fun insert(settingsEntity: SequentialSettingsEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    override suspend fun insertIfNotExists(settingsEntity: SequentialSettingsEntity)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    override suspend fun update(settingsEntity: SequentialSettingsEntity)

    @Delete
    override suspend fun delete(settingsEntity: SequentialSettingsEntity)

    @Query("DELETE FROM SequentialSettingsEntity WHERE configName = :configName")
    override suspend fun delete(configName: String)
}