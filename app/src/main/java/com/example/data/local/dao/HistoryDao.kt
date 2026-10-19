package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entities.HistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HistoryDao {
    @Query("SELECT * FROM browsing_history ORDER BY timestamp DESC LIMIT 200")
    fun getAllHistory(): Flow<List<HistoryEntity>>

    @Query("SELECT * FROM browsing_history WHERE title LIKE '%' || :query || '%' OR url LIKE '%' || :query || '%' ORDER BY timestamp DESC LIMIT 100")
    fun searchHistory(query: String): Flow<List<HistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(item: HistoryEntity): Long

    @Delete
    suspend fun deleteHistory(item: HistoryEntity)

    @Query("DELETE FROM browsing_history WHERE id = :id")
    suspend fun deleteHistoryById(id: Long)

    @Query("DELETE FROM browsing_history")
    suspend fun clearAllHistory()
}
