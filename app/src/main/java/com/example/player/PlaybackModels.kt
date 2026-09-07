package com.example.player

import com.example.data.model.VideoItem

enum class RepeatMode {
    OFF,
    ONE,
    ALL
}

enum class AppThemeMode {
    FROSTED_GLASS,
    SYSTEM,
    LIGHT,
    DARK,
    AMOLED_BLACK
}

data class PlaybackState(
    val currentVideo: VideoItem? = null,
    val playlist: List<VideoItem> = emptyList(),
    val currentIndex: Int = -1,
    val isPlaying: Boolean = false,
    val isBuffering: Boolean = false,
    val currentPositionMs: Long = 0L,
    val durationMs: Long = 0L,
    val playbackSpeed: Float = 1.0f,
    val repeatMode: RepeatMode = RepeatMode.OFF,
    val isAutoplayEnabled: Boolean = true,
    val isBackgroundPlayEnabled: Boolean = true,
    val subtitlesEnabled: Boolean = true,
    val subtitleFileName: String? = null,
    val currentSubtitleText: String? = null,
    val isControlsLocked: Boolean = false,
    val isMiniPlayer: Boolean = false,
    val isPlayerVisible: Boolean = false,
    val volumePercent: Float = 0.5f,
    val brightnessPercent: Float = 0.5f
)
