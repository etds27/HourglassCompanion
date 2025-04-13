package com.etds.hourglass.data.game.local.db.daos

import androidx.room.Dao
import androidx.room.Query
import com.etds.hourglass.data.game.local.db.entity.GameEntity

@Dao
interface GameDao {
    @Query("SELECT * FROM GameEntity") fun getAllGames(): List<GameEntity>
}