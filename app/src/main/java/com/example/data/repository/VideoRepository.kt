package com.example.data.repository

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.example.data.db.AppDatabase
import com.example.data.model.PlaylistEntity
import com.example.data.model.PlaylistItemEntity
import com.example.data.model.VideoItem
import com.example.data.model.WatchHistoryEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.File

class VideoRepository(private val context: Context) {
    private val db = AppDatabase.getInstance(context)
    private val playlistDao = db.playlistDao()
    private val watchHistoryDao = db.watchHistoryDao()

    val allPlaylists: Flow<List<PlaylistEntity>> = playlistDao.getAllPlaylists()
    val watchHistory: Flow<List<WatchHistoryEntity>> = watchHistoryDao.getAllHistory()

    fun getPlaylistItems(playlistId: Long): Flow<List<PlaylistItemEntity>> =
        playlistDao.getItemsForPlaylist(playlistId)

    suspend fun createPlaylist(name: String): Long = withContext(Dispatchers.IO) {
        playlistDao.insertPlaylist(PlaylistEntity(name = name.trim()))
    }

    suspend fun deletePlaylist(playlistId: Long) = withContext(Dispatchers.IO) {
        playlistDao.deletePlaylist(playlistId)
    }

    suspend fun addVideoToPlaylist(playlistId: Long, video: VideoItem): Long = withContext(Dispatchers.IO) {
        playlistDao.addItemToPlaylist(
            PlaylistItemEntity(
                playlistId = playlistId,
                videoUriString = video.uri.toString(),
                title = video.title,
                durationMs = video.durationMs,
                folderName = video.folderName,
                size = video.size
            )
        )
    }

    suspend fun removePlaylistItem(itemId: Long) = withContext(Dispatchers.IO) {
        playlistDao.removeItemFromPlaylist(itemId)
    }

    suspend fun recordWatchProgress(video: VideoItem, positionMs: Long) = withContext(Dispatchers.IO) {
        watchHistoryDao.insertOrUpdate(
            WatchHistoryEntity(
                videoUriString = video.uri.toString(),
                title = video.title,
                durationMs = video.durationMs,
                lastPositionMs = positionMs,
                folderName = video.folderName,
                lastWatchedTimestamp = System.currentTimeMillis()
            )
        )
    }

    suspend fun getLastPositionForVideo(videoUri: String): Long = withContext(Dispatchers.IO) {
        watchHistoryDao.getHistoryForVideo(videoUri)?.lastPositionMs ?: 0L
    }

    suspend fun clearHistory() = withContext(Dispatchers.IO) {
        watchHistoryDao.clearAllHistory()
    }

    suspend fun deleteHistoryItem(videoUri: String) = withContext(Dispatchers.IO) {
        watchHistoryDao.deleteHistoryItem(videoUri)
    }

    suspend fun loadDeviceVideos(): List<VideoItem> = withContext(Dispatchers.IO) {
        val videoList = mutableListOf<VideoItem>()
        val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        }

        val projection = arrayOf(
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.TITLE,
            MediaStore.Video.Media.DISPLAY_NAME,
            MediaStore.Video.Media.DURATION,
            MediaStore.Video.Media.SIZE,
            MediaStore.Video.Media.BUCKET_DISPLAY_NAME,
            MediaStore.Video.Media.DATE_MODIFIED,
            MediaStore.Video.Media.MIME_TYPE,
            MediaStore.Video.Media.DATA
        )

        try {
            val cursor = context.contentResolver.query(
                collection,
                projection,
                null,
                null,
                "${MediaStore.Video.Media.DATE_MODIFIED} DESC"
            )

            cursor?.use { c ->
                val idColumn = c.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
                val titleColumn = c.getColumnIndex(MediaStore.Video.Media.TITLE)
                val displayNameColumn = c.getColumnIndex(MediaStore.Video.Media.DISPLAY_NAME)
                val durationColumn = c.getColumnIndex(MediaStore.Video.Media.DURATION)
                val sizeColumn = c.getColumnIndex(MediaStore.Video.Media.SIZE)
                val bucketColumn = c.getColumnIndex(MediaStore.Video.Media.BUCKET_DISPLAY_NAME)
                val dateModifiedColumn = c.getColumnIndex(MediaStore.Video.Media.DATE_MODIFIED)
                val mimeTypeColumn = c.getColumnIndex(MediaStore.Video.Media.MIME_TYPE)
                val dataColumn = c.getColumnIndex(MediaStore.Video.Media.DATA)

                while (c.moveToNext()) {
                    val id = c.getLong(idColumn)
                    val contentUri = ContentUris.withAppendedId(collection, id)
                    val displayName = if (displayNameColumn != -1) c.getString(displayNameColumn) else null
                    val rawTitle = if (titleColumn != -1) c.getString(titleColumn) else null
                    val title = displayName ?: rawTitle ?: "Video_$id"
                    val duration = if (durationColumn != -1) c.getLong(durationColumn) else 0L
                    val size = if (sizeColumn != -1) c.getLong(sizeColumn) else 0L
                    val folderName = if (bucketColumn != -1) c.getString(bucketColumn) ?: "Internal" else "Internal"
                    val dateModified = if (dateModifiedColumn != -1) c.getLong(dateModifiedColumn) * 1000 else System.currentTimeMillis()
                    val mimeType = if (mimeTypeColumn != -1) c.getString(mimeTypeColumn) ?: "video/mp4" else "video/mp4"
                    val path = if (dataColumn != -1) c.getString(dataColumn) ?: "" else ""

                    videoList.add(
                        VideoItem(
                            id = id,
                            uri = contentUri,
                            title = title,
                            durationMs = duration,
                            size = size,
                            resolution = "HD",
                            folderName = folderName,
                            dateModified = dateModified,
                            mimeType = mimeType,
                            path = path
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // If storage has no videos yet (e.g. testing in fresh emulator), include pre-loaded demo videos
        if (videoList.isEmpty()) {
            videoList.addAll(getDemoSampleVideos())
        }

        videoList
    }

    fun getDemoSampleVideos(): List<VideoItem> {
        return listOf(
            VideoItem(
                id = 1001L,
                uri = Uri.parse("https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"),
                title = "Big Buck Bunny (4K Animation)",
                durationMs = 596000L,
                size = 158008374L,
                resolution = "1080p",
                folderName = "Movies",
                mimeType = "video/mp4"
            ),
            VideoItem(
                id = 1002L,
                uri = Uri.parse("https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4"),
                title = "Elephants Dream (Sci-Fi Short Film)",
                durationMs = 653000L,
                size = 140008374L,
                resolution = "1080p",
                folderName = "Animation",
                mimeType = "video/mp4"
            ),
            VideoItem(
                id = 1003L,
                uri = Uri.parse("https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4"),
                title = "Chromecast - For Bigger Blazes",
                durationMs = 15000L,
                size = 15200000L,
                resolution = "1080p",
                folderName = "Camera",
                mimeType = "video/mp4"
            ),
            VideoItem(
                id = 1004L,
                uri = Uri.parse("https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4"),
                title = "Tears of Steel (VFX Short Film)",
                durationMs = 734000L,
                size = 210008374L,
                resolution = "4K",
                folderName = "Downloads",
                mimeType = "video/mp4"
            ),
            VideoItem(
                id = 1005L,
                uri = Uri.parse("https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/Sintel.mp4"),
                title = "Sintel (Fantasy CGI Story)",
                durationMs = 888000L,
                size = 190008374L,
                resolution = "1080p",
                folderName = "Movies",
                mimeType = "video/mp4"
            )
        )
    }
}
