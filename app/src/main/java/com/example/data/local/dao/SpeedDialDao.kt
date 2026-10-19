package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entities.SpeedDialEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SpeedDialDao {
    @Query("SELECT * FROM speed_dials ORDER BY sortOrder ASC")
    fun getAllSpeedDials(): Flow<List<SpeedDialEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSpeedDial(speedDial: SpeedDialEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSpeedDials(speedDials: List<SpeedDialEntity>)

    @Delete
    suspend fun deleteSpeedDial(speedDial: SpeedDialEntity)

    @Query("DELETE FROM speed_dials WHERE id = :id")
    suspend fun deleteSpeedDialById(id: Long)

    @Query("UPDATE speed_dials SET title = :title, url = :url WHERE id = :id")
    suspend fun updateSpeedDial(id: Long, title: String, url: String)

    @Query("SELECT COUNT(*) FROM speed_dials")
    suspend fun getCount(): Int
}
