package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "speed_dials")
data class SpeedDialEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val url: String,
    val iconColor: Long, // Color ARGB long value
    val sortOrder: Int = 0,
    val isCustom: Boolean = false
)
