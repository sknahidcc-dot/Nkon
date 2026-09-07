package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.WatchHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WatchHistoryDao {
    @Query("SELECT * FROM watch_history ORDER BY lastWatchedTimestamp DESC")
    fun getAllHistory(): Flow<List<WatchHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(history: WatchHistoryEntity)

    @Query("SELECT * FROM watch_history WHERE videoUriString = :videoUriString LIMIT 1")
    suspend fun getHistoryForVideo(videoUriString: String): WatchHistoryEntity?

    @Query("DELETE FROM watch_history WHERE videoUriString = :videoUriString")
    suspend fun deleteHistoryItem(videoUriString: String)

    @Query("DELETE FROM watch_history")
    suspend fun clearAllHistory()
}
