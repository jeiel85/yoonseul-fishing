package com.jeiel85.healingfishing.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "caught_fish")
data class CaughtFishEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val speciesId: String,
    val weight: Float,
    val length: Float,
    val caughtTime: Long = System.currentTimeMillis(),
    val timeOfDay: String // "day", "sunset", "night"
)
