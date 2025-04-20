package com.etds.hourglass.data.game.local

import com.etds.hourglass.data.game.local.db.daos.BuzzerSettingsDao
import com.etds.hourglass.data.game.local.db.daos.GameDao
import com.etds.hourglass.data.game.local.db.daos.SequentialSettingsDao
import com.etds.hourglass.data.game.local.db.daos.SettingsDao
import com.etds.hourglass.data.game.local.db.entity.BuzzerSettingsEntity
import com.etds.hourglass.data.game.local.db.entity.SequentialSettingsEntity
import com.etds.hourglass.data.game.local.db.entity.SettingsEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalDatasource @Inject constructor(
    private val _gameDao: GameDao,
    private val _buzzerSettingsDao: BuzzerSettingsDao,
    private val _sequentialSettingsDao: SequentialSettingsDao
) {
    val gameDao = _gameDao
    val buzzerSettingsDao = _buzzerSettingsDao
    val sequentialSettingsDao = _sequentialSettingsDao


    suspend fun setDefaultBuzzerPreset(presetName: String) {
        for (setting in buzzerSettingsDao.getAll()) {
            if (setting.default) {
                setting.default = false
                buzzerSettingsDao.update(setting)
            }
            if (setting.configName == presetName) {
                setting.default = true
                buzzerSettingsDao.update(setting)
            }
        }
    }

    suspend fun setDefaultSequentialPreset(presetName: String) {
        for (setting in sequentialSettingsDao.getAll()) {
            if (setting.default) {
                setting.default = false
                sequentialSettingsDao.update(setting)
            }
            if (setting.configName == presetName) {
                setting.default = true
                sequentialSettingsDao.update(setting)
            }
        }
    }

    suspend fun getDefaultBuzzerPreset(): BuzzerSettingsEntity? {
        val settingsEntities = buzzerSettingsDao.getAll()

        if (settingsEntities.isEmpty()) { return null }

        for (setting in buzzerSettingsDao.getAll()) {
            if (setting.default) {
                return setting
            }
        }
        return settingsEntities.first()
    }

    suspend fun getDefaultSequentialPreset(): SequentialSettingsEntity? {
        val settingsEntities = sequentialSettingsDao.getAll()

        if (settingsEntities.isEmpty()) { return null }

        for (setting in sequentialSettingsDao.getAll()) {
            if (setting.default) {
                return setting
            }
        }
        return settingsEntities.first()
    }


}