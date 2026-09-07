package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "watch_history")
data class WatchHistoryEntity(
    @PrimaryKey val videoUriString: String,
    val title: String,
    val durationMs: Long,
    val lastPositionMs: Long,
    val folderName: String = "Videos",
    val lastWatchedTimestamp: Long = System.currentTimeMillis()
) {
    val progressPercent: Float
        get() = if (durationMs > 0) (lastPositionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f) else 0f
}
