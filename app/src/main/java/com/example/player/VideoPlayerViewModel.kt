package com.example.player

import android.app.Activity
import android.app.PictureInPictureParams
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import android.util.Rational
import android.view.WindowManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import com.example.data.model.PlaylistEntity
import com.example.data.model.PlaylistItemEntity
import com.example.data.model.VideoItem
import com.example.data.model.WatchHistoryEntity
import com.example.data.repository.VideoRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class VideoPlayerViewModel(
    private val context: Context,
    private val repository: VideoRepository
) : ViewModel() {

    val player: ExoPlayer = ExoPlayer.Builder(context)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setContentType(C.AUDIO_CONTENT_TYPE_MOVIE)
                .setUsage(C.USAGE_MEDIA)
                .build(),
            true
        )
        .setHandleAudioBecomingNoisy(true)
        .setWakeMode(C.WAKE_MODE_LOCAL)
        .setSeekForwardIncrementMs(10000)
        .setSeekBackIncrementMs(10000)
        .build()

    private var mediaSession: MediaSession? = try {
        MediaSession.Builder(context, player).build()
    } catch (e: Exception) {
        null
    }

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    private val _playbackState = MutableStateFlow(PlaybackState())
    val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()

    private val _allVideos = MutableStateFlow<List<VideoItem>>(emptyList())
    val allVideos: StateFlow<List<VideoItem>> = _allVideos.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _currentThemeMode = MutableStateFlow(AppThemeMode.FROSTED_GLASS)
    val currentThemeMode: StateFlow<AppThemeMode> = _currentThemeMode.asStateFlow()

    val playlists: StateFlow<List<PlaylistEntity>> = repository.allPlaylists
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val watchHistory: StateFlow<List<WatchHistoryEntity>> = repository.watchHistory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private var subtitleEntries = listOf<SubtitleEntry>()
    private var progressJob: Job? = null

    init {
        initPlayerListeners()
        loadVideos()
        initVolumeAndBrightness()
    }

    private fun initVolumeAndBrightness() {
        val maxVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC).toFloat()
        val currVol = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC).toFloat()
        val volPercent = if (maxVol > 0) currVol / maxVol else 0.5f
        _playbackState.update { it.copy(volumePercent = volPercent) }
    }

    private fun initPlayerListeners() {
        player.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _playbackState.update { it.copy(isPlaying = isPlaying) }
                val current = _playbackState.value.currentVideo
                if (current != null && _playbackState.value.isBackgroundPlayEnabled) {
                    MediaNotificationHelper.showPlaybackNotification(context, current, isPlaying)
                }
                if (isPlaying) {
                    startProgressTracker()
                } else {
                    progressJob?.cancel()
                    saveCurrentProgress()
                }
            }

            override fun onPlaybackStateChanged(state: Int) {
                when (state) {
                    Player.STATE_BUFFERING -> {
                        _playbackState.update { it.copy(isBuffering = true) }
                    }
                    Player.STATE_READY -> {
                        _playbackState.update {
                            it.copy(
                                isBuffering = false,
                                durationMs = player.duration.coerceAtLeast(0L)
                            )
                        }
                    }
                    Player.STATE_ENDED -> {
                        _playbackState.update { it.copy(isPlaying = false, isBuffering = false) }
                        handlePlaybackEnded()
                    }
                    else -> {
                        _playbackState.update { it.copy(isBuffering = false) }
                    }
                }
            }
        })
    }

    fun loadVideos() {
        viewModelScope.launch {
            val list = repository.loadDeviceVideos()
            _allVideos.value = list
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setThemeMode(mode: AppThemeMode) {
        _currentThemeMode.value = mode
    }

    fun playVideo(video: VideoItem, playlist: List<VideoItem> = listOf(video)) {
        val index = playlist.indexOfFirst { it.uri == video.uri }.takeIf { it >= 0 } ?: 0
        _playbackState.update {
            it.copy(
                currentVideo = video,
                playlist = playlist,
                currentIndex = index,
                isPlayerVisible = true,
                isMiniPlayer = false
            )
        }

        viewModelScope.launch {
            val lastPos = repository.getLastPositionForVideo(video.uri.toString())
            val mediaItem = MediaItem.fromUri(video.uri)
            player.setMediaItem(mediaItem)
            player.prepare()
            if (lastPos > 0 && lastPos < video.durationMs - 5000) {
                player.seekTo(lastPos)
            }
            player.playbackParameters = player.playbackParameters.withSpeed(_playbackState.value.playbackSpeed)
            player.play()
        }
    }

    fun togglePlayPause() {
        if (player.isPlaying) {
            player.pause()
        } else {
            if (player.playbackState == Player.STATE_ENDED) {
                player.seekTo(0)
            }
            player.play()
        }
    }

    fun seekTo(positionMs: Long) {
        val clamped = positionMs.coerceIn(0L, _playbackState.value.durationMs)
        player.seekTo(clamped)
        _playbackState.update { it.copy(currentPositionMs = clamped) }
    }

    fun seekRelative(deltaMs: Long) {
        val target = (player.currentPosition + deltaMs).coerceIn(0L, _playbackState.value.durationMs)
        player.seekTo(target)
        _playbackState.update { it.copy(currentPositionMs = target) }
    }

    fun playNext() {
        val state = _playbackState.value
        if (state.playlist.isEmpty()) return
        val nextIndex = state.currentIndex + 1
        if (nextIndex < state.playlist.size) {
            val nextVideo = state.playlist[nextIndex]
            playVideo(nextVideo, state.playlist)
        } else if (state.repeatMode == RepeatMode.ALL && state.playlist.isNotEmpty()) {
            playVideo(state.playlist[0], state.playlist)
        }
    }

    fun playPrevious() {
        val state = _playbackState.value
        if (player.currentPosition > 3000L || state.currentIndex <= 0) {
            seekTo(0)
        } else {
            val prevIndex = state.currentIndex - 1
            if (prevIndex >= 0 && prevIndex < state.playlist.size) {
                playVideo(state.playlist[prevIndex], state.playlist)
            }
        }
    }

    private fun handlePlaybackEnded() {
        val state = _playbackState.value
        when (state.repeatMode) {
            RepeatMode.ONE -> {
                seekTo(0)
                player.play()
            }
            RepeatMode.ALL -> {
                playNext()
            }
            RepeatMode.OFF -> {
                if (state.isAutoplayEnabled) {
                    playNext()
                }
            }
        }
    }

    fun setPlaybackSpeed(speed: Float) {
        player.playbackParameters = player.playbackParameters.withSpeed(speed)
        _playbackState.update { it.copy(playbackSpeed = speed) }
    }

    fun cycleRepeatMode() {
        val nextMode = when (_playbackState.value.repeatMode) {
            RepeatMode.OFF -> RepeatMode.ALL
            RepeatMode.ALL -> RepeatMode.ONE
            RepeatMode.ONE -> RepeatMode.OFF
        }
        _playbackState.update { it.copy(repeatMode = nextMode) }
    }

    fun toggleAutoplay() {
        _playbackState.update { it.copy(isAutoplayEnabled = !it.isAutoplayEnabled) }
    }

    fun toggleBackgroundPlay() {
        _playbackState.update { it.copy(isBackgroundPlayEnabled = !it.isBackgroundPlayEnabled) }
    }

    fun toggleControlsLock() {
        _playbackState.update { it.copy(isControlsLocked = !it.isControlsLocked) }
    }

    fun toggleSubtitles() {
        _playbackState.update { it.copy(subtitlesEnabled = !it.subtitlesEnabled) }
    }

    fun loadSubtitles(uri: Uri, fileName: String) {
        viewModelScope.launch {
            val entries = SubtitleParser.parseFromUri(context, uri)
            subtitleEntries = entries
            _playbackState.update {
                it.copy(
                    subtitlesEnabled = true,
                    subtitleFileName = fileName
                )
            }
        }
    }

    fun minimizeToMiniPlayer() {
        _playbackState.update { it.copy(isMiniPlayer = true) }
    }

    fun expandFromMiniPlayer() {
        _playbackState.update { it.copy(isMiniPlayer = false) }
    }

    fun closePlayer() {
        saveCurrentProgress()
        MediaNotificationHelper.clearNotification(context)
        player.stop()
        _playbackState.update {
            it.copy(
                isPlayerVisible = false,
                isMiniPlayer = false,
                currentVideo = null
            )
        }
    }

    fun enterPictureInPicture(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                val aspectRatio = Rational(16, 9)
                val paramsBuilder = PictureInPictureParams.Builder()
                    .setAspectRatio(aspectRatio)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    paramsBuilder.setAutoEnterEnabled(true)
                }
                activity.enterPictureInPictureMode(paramsBuilder.build())
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun shareVideo(activity: Activity) {
        val current = _playbackState.value.currentVideo ?: return
        try {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "video/*"
                putExtra(Intent.EXTRA_STREAM, current.uri)
                putExtra(Intent.EXTRA_TITLE, current.title)
                putExtra(Intent.EXTRA_TEXT, "Watching: ${current.title} on Video Player")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            activity.startActivity(Intent.createChooser(shareIntent, "Share Video via"))
        } catch (e: Exception) {
            // Fallback to text sharing
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, "Watching: ${current.title}")
            }
            activity.startActivity(Intent.createChooser(shareIntent, "Share Video via"))
        }
    }

    fun adjustVolume(deltaPercent: Float) {
        val currentPercent = _playbackState.value.volumePercent
        val newPercent = (currentPercent + deltaPercent).coerceIn(0f, 1f)
        val maxVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        val targetVol = (newPercent * maxVol).toInt()
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, targetVol, 0)
        _playbackState.update { it.copy(volumePercent = newPercent) }
    }

    fun adjustBrightness(activity: Activity, deltaPercent: Float) {
        val current = _playbackState.value.brightnessPercent
        val newPercent = (current + deltaPercent).coerceIn(0.01f, 1.0f)
        val lp = activity.window.attributes
        lp.screenBrightness = newPercent
        activity.window.attributes = lp
        _playbackState.update { it.copy(brightnessPercent = newPercent) }
    }

    private fun startProgressTracker() {
        progressJob?.cancel()
        progressJob = viewModelScope.launch {
            while (isActive) {
                val currentPos = player.currentPosition.coerceAtLeast(0L)
                val dur = player.duration.coerceAtLeast(0L)

                val subText = if (_playbackState.value.subtitlesEnabled && subtitleEntries.isNotEmpty()) {
                    SubtitleParser.getCurrentSubtitle(subtitleEntries, currentPos)
                } else null

                _playbackState.update {
                    it.copy(
                        currentPositionMs = currentPos,
                        durationMs = if (dur > 0) dur else it.durationMs,
                        currentSubtitleText = subText
                    )
                }
                delay(300)
            }
        }
    }

    private fun saveCurrentProgress() {
        val current = _playbackState.value.currentVideo ?: return
        val pos = player.currentPosition
        if (pos > 0) {
            viewModelScope.launch {
                repository.recordWatchProgress(current, pos)
            }
        }
    }

    fun createPlaylist(name: String) {
        viewModelScope.launch {
            repository.createPlaylist(name)
        }
    }

    fun deletePlaylist(playlistId: Long) {
        viewModelScope.launch {
            repository.deletePlaylist(playlistId)
        }
    }

    fun addVideoToPlaylist(playlistId: Long, video: VideoItem) {
        viewModelScope.launch {
            repository.addVideoToPlaylist(playlistId, video)
        }
    }

    fun removePlaylistItem(itemId: Long) {
        viewModelScope.launch {
            repository.removePlaylistItem(itemId)
        }
    }

    fun clearWatchHistory() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }

    fun deleteHistoryItem(videoUri: String) {
        viewModelScope.launch {
            repository.deleteHistoryItem(videoUri)
        }
    }

    fun onAppBackgrounded() {
        if (!_playbackState.value.isBackgroundPlayEnabled) {
            player.pause()
        }
        // If background play is enabled, player continues playback smoothly!
    }

    override fun onCleared() {
        super.onCleared()
        saveCurrentProgress()
        MediaNotificationHelper.clearNotification(context)
        try {
            mediaSession?.release()
            mediaSession = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
        player.release()
    }
}
