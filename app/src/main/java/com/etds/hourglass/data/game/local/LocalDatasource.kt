package com.etds.hourglass.data.game.local

import com.etds.hourglass.data.game.local.db.daos.GameDao
import com.etds.hourglass.data.game.local.db.daos.SettingsDao
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalDatasource @Inject constructor(
    private val _gameDao: GameDao,
    private val _settingsDao: SettingsDao
) {
    val gameDao = _gameDao
    val settingsDao = _settingsDao

    suspend fun setDefaultPreset(presetName: String) {
        for (setting in settingsDao.getAll()) {
            if (setting.default) {
                setting.default = false
                settingsDao.update(setting)
            }
            if (setting.configName == presetName) {
                setting.default = true
                settingsDao.update(setting)
            }
        }
    }
}