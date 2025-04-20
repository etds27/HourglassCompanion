package com.etds.hourglass.data.game.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity
data class GameEntity(
    @PrimaryKey val id: Int
)