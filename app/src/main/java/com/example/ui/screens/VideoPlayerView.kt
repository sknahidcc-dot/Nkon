package com.example.ui.screens

import android.app.Activity
import android.content.pm.ActivityInfo
import android.hardware.SensorManager
import android.view.OrientationEventListener
import androidx.activity.compose.BackHandler
import androidx.annotation.OptIn
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cast
import androidx.compose.material.icons.filled.ClosedCaption
import androidx.compose.material.icons.filled.ClosedCaptionOff
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PictureInPictureAlt
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.PlayerView
import com.example.data.model.VideoItem
import com.example.player.RepeatMode
import com.example.player.VideoPlayerViewModel
import com.example.ui.components.CastDialog
import com.example.ui.components.GestureControlOverlay
import com.example.ui.components.SpeedSelectorDialog
import com.example.ui.components.SubtitleDialog
import com.example.ui.components.VideoCard
import com.example.ui.theme.FrostedDarkBackground
import com.example.ui.theme.FrostedDialogBackground
import com.example.ui.theme.FrostedGlassBlack50
import com.example.ui.theme.FrostedGlassBorder
import com.example.ui.theme.FrostedGlassBorderLight
import com.example.ui.theme.FrostedGlassBorderSubtle
import com.example.ui.theme.FrostedGlassWhite05
import com.example.ui.theme.FrostedGlassWhite10
import com.example.ui.theme.FrostedGlassWhite15
import com.example.ui.theme.FrostedGlassWhite20
import com.example.ui.theme.FrostedTextPrimary
import com.example.ui.theme.FrostedTextSecondary
import com.example.ui.theme.YouTubeRed
import kotlinx.coroutines.delay

@OptIn(UnstableApi::class)
@Composable
fun VideoPlayerView(
    viewModel: VideoPlayerViewModel,
    onAddToPlaylist: (VideoItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = context as? Activity

    val playbackState by viewModel.playbackState.collectAsState()
    val allVideos by viewModel.allVideos.collectAsState()
    val currentVideo = playbackState.currentVideo ?: return

    var areControlsVisible by remember { mutableStateOf(true) }
    var isFullScreen by remember { mutableStateOf(false) }
    var isLiked by remember { mutableStateOf(false) }

    // Dialog States
    var showSpeedDialog by remember { mutableStateOf(false) }
    var showSubtitleDialog by remember { mutableStateOf(false) }
    var showCastDialog by remember { mutableStateOf(false) }

    // Scrubber drag state
    var isDraggingSlider by remember { mutableStateOf(false) }
    var draggedPositionMs by remember { mutableFloatStateOf(0f) }
    var wasAutoRotated by remember { mutableStateOf(false) }

    // Orientation event listener: detects when user tilts phone to auto-rotate
    val orientationEventListener = remember {
        object : OrientationEventListener(context, SensorManager.SENSOR_DELAY_NORMAL) {
            override fun onOrientationChanged(orientation: Int) {
                if (orientation == ORIENTATION_UNKNOWN) return

                val isLandscapePhysical = (orientation in 60..120) || (orientation in 240..300)
                val isPortraitPhysical = (orientation in 340..360) || (orientation in 0..20)

                if (isLandscapePhysical && !isFullScreen) {
                    wasAutoRotated = true
                    isFullScreen = true
                } else if (isPortraitPhysical && isFullScreen && wasAutoRotated) {
                    wasAutoRotated = false
                    isFullScreen = false
                }
            }
        }
    }

    DisposableEffect(Unit) {
        if (orientationEventListener.canDetectOrientation()) {
            orientationEventListener.enable()
        }
        onDispose {
            orientationEventListener.disable()
            activity?.let { act ->
                act.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                val insetsController = WindowCompat.getInsetsController(act.window, act.window.decorView)
                insetsController.show(WindowInsetsCompat.Type.systemBars())
            }
        }
    }

    // Toggle orientation & immersive system bars when fullscreen toggled
    // With SCREEN_ORIENTATION_FULL_SENSOR: whichever way phone is rotated, video auto-rotates!
    LaunchedEffect(isFullScreen) {
        activity?.let { act ->
            act.requestedOrientation = if (isFullScreen) {
                ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR
            } else {
                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            }

            val insetsController = WindowCompat.getInsetsController(act.window, act.window.decorView)
            if (isFullScreen) {
                insetsController.hide(WindowInsetsCompat.Type.systemBars())
                insetsController.systemBarsBehavior =
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            } else {
                insetsController.show(WindowInsetsCompat.Type.systemBars())
            }
        }
    }

    // Handle back button: exit fullscreen first, otherwise minimize
    BackHandler {
        if (isFullScreen) {
            isFullScreen = false
        } else {
            viewModel.minimizeToMiniPlayer()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .testTag("video_player_screen")
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Video Player Container (ExoPlayer Surface + Gesture Overlay + Controls)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(
                        if (isFullScreen) Modifier.fillMaxSize()
                        else Modifier
                            .statusBarsPadding()
                            .aspectRatio(16f / 9f)
                    )
                    .background(Color.Black)
            ) {
                // ExoPlayer View
                AndroidView(
                    factory = { ctx ->
                        PlayerView(ctx).apply {
                            player = viewModel.player
                            useController = false
                            setShowBuffering(PlayerView.SHOW_BUFFERING_NEVER)
                            layoutParams = android.view.ViewGroup.LayoutParams(
                                android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                                android.view.ViewGroup.LayoutParams.MATCH_PARENT
                            )
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )

                // Subtitle Display Overlay
                if (playbackState.subtitlesEnabled && !playbackState.currentSubtitleText.isNullOrEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                            .padding(bottom = if (areControlsVisible) 56.dp else 24.dp)
                            .padding(horizontal = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color.Black.copy(alpha = 0.8f),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = playbackState.currentSubtitleText.orEmpty(),
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                // Gesture Control Overlay (Swipe Volume/Brightness, Seek scrub, Double-tap +/-10s)
                GestureControlOverlay(
                    modifier = Modifier.fillMaxSize(),
                    volumePercent = playbackState.volumePercent,
                    brightnessPercent = playbackState.brightnessPercent,
                    currentPositionMs = playbackState.currentPositionMs,
                    durationMs = playbackState.durationMs,
                    isControlsLocked = playbackState.isControlsLocked,
                    onSingleTap = { areControlsVisible = !areControlsVisible },
                    onDoubleTapSeek = { deltaMs -> viewModel.seekRelative(deltaMs) },
                    onAdjustVolume = { delta -> viewModel.adjustVolume(delta) },
                    onAdjustBrightness = { delta -> activity?.let { viewModel.adjustBrightness(it, delta) } },
                    onSeekTo = { posMs -> viewModel.seekTo(posMs) },
                    onSwipeDownMinimize = {
                        if (isFullScreen) {
                            isFullScreen = false
                        } else {
                            viewModel.minimizeToMiniPlayer()
                        }
                    }
                )

                // Player Controls Overlay
                androidx.compose.animation.AnimatedVisibility(
                    visible = areControlsVisible,
                    enter = fadeIn(),
                    exit = fadeOut(),
                    modifier = Modifier.fillMaxSize()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.45f))
                    ) {
                        // Lock / Unlock Button (Always visible on overlay)
                        IconButton(
                            onClick = { viewModel.toggleControlsLock() },
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(top = 10.dp, start = 8.dp)
                                .size(44.dp)
                                .testTag("lock_controls_button")
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = FrostedGlassWhite20,
                                modifier = Modifier
                                    .size(36.dp)
                                    .border(1.dp, FrostedGlassBorderLight, CircleShape)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = if (playbackState.isControlsLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                                        contentDescription = "Lock Controls",
                                        tint = if (playbackState.isControlsLocked) YouTubeRed else FrostedTextPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }

                        // If not locked, show full top, center, and bottom controls
                        if (!playbackState.isControlsLocked) {
                            // Top Bar Controls: Minimize, Autoplay, Cast, Subtitle, Speed, PiP
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .align(Alignment.TopCenter)
                                    .padding(start = 56.dp, end = 8.dp, top = 8.dp),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Minimize down arrow in frosted glass circle
                                Surface(
                                    shape = CircleShape,
                                    color = FrostedGlassBlack50,
                                    modifier = Modifier
                                        .size(36.dp)
                                        .border(1.dp, FrostedGlassBorderSubtle, CircleShape)
                                ) {
                                    IconButton(
                                        onClick = {
                                            if (isFullScreen) isFullScreen = false
                                            else viewModel.minimizeToMiniPlayer()
                                        },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.KeyboardArrowDown,
                                            contentDescription = "Minimize",
                                            tint = FrostedTextPrimary
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.weight(1f))

                                // Top controls pill bar
                                Row(
                                    modifier = Modifier
                                        .background(FrostedGlassBlack50, RoundedCornerShape(20.dp))
                                        .border(1.dp, FrostedGlassBorderSubtle, RoundedCornerShape(20.dp))
                                        .padding(horizontal = 4.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Autoplay chip/button
                                    IconButton(
                                        onClick = { viewModel.toggleAutoplay() },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = if (playbackState.isAutoplayEnabled) YouTubeRed else FrostedGlassWhite20,
                                            modifier = Modifier.padding(2.dp)
                                        ) {
                                            Text(
                                                text = "AUTO",
                                                color = Color.White,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                            )
                                        }
                                    }

                                    // Cast Button
                                    IconButton(
                                        onClick = { showCastDialog = true },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Cast,
                                            contentDescription = "Cast",
                                            tint = FrostedTextPrimary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }

                                    // Subtitle Button
                                    IconButton(
                                        onClick = { showSubtitleDialog = true },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (playbackState.subtitlesEnabled) Icons.Default.ClosedCaption else Icons.Default.ClosedCaptionOff,
                                            contentDescription = "Subtitles",
                                            tint = if (playbackState.subtitlesEnabled) YouTubeRed else FrostedTextPrimary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }

                                    // Speed Button
                                    IconButton(
                                        onClick = { showSpeedDialog = true },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Speed,
                                            contentDescription = "Speed",
                                            tint = FrostedTextPrimary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }

                                    // PiP Button
                                    IconButton(
                                        onClick = { activity?.let { viewModel.enterPictureInPicture(it) } },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.PictureInPictureAlt,
                                            contentDescription = "Picture-in-Picture",
                                            tint = FrostedTextPrimary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }

                            // Center Controls: Previous, Play/Pause/Buffering, Next
                            Row(
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .padding(horizontal = 24.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                // Previous Video Button
                                Surface(
                                    shape = CircleShape,
                                    color = FrostedGlassBlack50,
                                    modifier = Modifier
                                        .size(48.dp)
                                        .border(1.dp, FrostedGlassBorderSubtle, CircleShape)
                                ) {
                                    IconButton(
                                        onClick = { viewModel.playPrevious() },
                                        modifier = Modifier.fillMaxSize()
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.SkipPrevious,
                                            contentDescription = "Previous",
                                            tint = FrostedTextPrimary,
                                            modifier = Modifier.size(32.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(28.dp))

                                // Play / Pause Button with Buffering state (Frosted Glass button)
                                Surface(
                                    shape = CircleShape,
                                    color = FrostedGlassWhite20,
                                    modifier = Modifier
                                        .size(68.dp)
                                        .border(1.5.dp, FrostedGlassBorderLight, CircleShape)
                                        .clickable { viewModel.togglePlayPause() }
                                        .testTag("play_pause_button")
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        if (playbackState.isBuffering) {
                                            CircularProgressIndicator(
                                                color = YouTubeRed,
                                                modifier = Modifier.size(36.dp),
                                                strokeWidth = 3.dp
                                            )
                                        } else {
                                            Icon(
                                                imageVector = if (playbackState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                                contentDescription = if (playbackState.isPlaying) "Pause" else "Play",
                                                tint = Color.White,
                                                modifier = Modifier.size(42.dp)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.width(28.dp))

                                // Next Video Button
                                Surface(
                                    shape = CircleShape,
                                    color = FrostedGlassBlack50,
                                    modifier = Modifier
                                        .size(48.dp)
                                        .border(1.dp, FrostedGlassBorderSubtle, CircleShape)
                                ) {
                                    IconButton(
                                        onClick = { viewModel.playNext() },
                                        modifier = Modifier.fillMaxSize()
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.SkipNext,
                                            contentDescription = "Next",
                                            tint = FrostedTextPrimary,
                                            modifier = Modifier.size(32.dp)
                                        )
                                    }
                                }
                            }

                            // Bottom Controls: Timestamp, Scrubber Slider, Fullscreen Toggle
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .align(Alignment.BottomCenter)
                                    .padding(horizontal = 14.dp, vertical = 8.dp)
                                    .background(FrostedGlassBlack50, RoundedCornerShape(14.dp))
                                    .border(1.dp, FrostedGlassBorderSubtle, RoundedCornerShape(14.dp))
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    val currentMs = if (isDraggingSlider) draggedPositionMs.toLong() else playbackState.currentPositionMs
                                    Text(
                                        text = "${formatDuration(currentMs)} / ${formatDuration(playbackState.durationMs)}",
                                        color = FrostedTextPrimary,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium
                                    )

                                    // Fullscreen Toggle Button
                                    IconButton(
                                        onClick = { isFullScreen = !isFullScreen },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (isFullScreen) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                                            contentDescription = "Fullscreen",
                                            tint = FrostedTextPrimary
                                        )
                                    }
                                }

                                val sliderValue = if (isDraggingSlider) draggedPositionMs
                                else playbackState.currentPositionMs.toFloat().coerceIn(0f, playbackState.durationMs.toFloat().coerceAtLeast(1f))

                                Slider(
                                    value = sliderValue,
                                    onValueChange = {
                                        isDraggingSlider = true
                                        draggedPositionMs = it
                                    },
                                    onValueChangeFinished = {
                                        isDraggingSlider = false
                                        viewModel.seekTo(draggedPositionMs.toLong())
                                    },
                                    valueRange = 0f..playbackState.durationMs.toFloat().coerceAtLeast(1f),
                                    colors = SliderDefaults.colors(
                                        thumbColor = YouTubeRed,
                                        activeTrackColor = YouTubeRed,
                                        inactiveTrackColor = FrostedGlassWhite20
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(18.dp)
                                        .testTag("video_progress_slider")
                                )
                            }
                        }
                    }
                }
            }

            // In Portrait mode: show video details, action buttons, and playlist/queue recommendations
            if (!isFullScreen) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .background(FrostedDarkBackground)
                ) {
                    // Video Info Card
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp)
                        ) {
                            Text(
                                text = currentVideo.title,
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp,
                                color = FrostedTextPrimary,
                                lineHeight = 22.sp
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "${currentVideo.folderName} • ${currentVideo.resolution} • ${currentVideo.getFormattedSize()}",
                                fontSize = 12.sp,
                                color = FrostedTextSecondary
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            // Horizontal Action Pills (YouTube frosted glass style)
                            LazyRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                val chipBorder = BorderStroke(1.dp, FrostedGlassBorderSubtle)

                                // Like
                                item {
                                    FilterChip(
                                        selected = isLiked,
                                        onClick = { isLiked = !isLiked },
                                        label = { Text(if (isLiked) "Liked" else "Like") },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = Icons.Default.ThumbUp,
                                                contentDescription = null,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        },
                                        colors = FilterChipDefaults.filterChipColors(
                                            containerColor = FrostedGlassWhite10,
                                            labelColor = FrostedTextPrimary,
                                            iconColor = FrostedTextSecondary,
                                            selectedContainerColor = YouTubeRed.copy(alpha = 0.85f),
                                            selectedLabelColor = Color.White,
                                            selectedLeadingIconColor = Color.White
                                        ),
                                        border = chipBorder
                                    )
                                }

                                // Floating (PiP) Multitasking Option
                                item {
                                    FilterChip(
                                        selected = false,
                                        onClick = { activity?.let { viewModel.enterPictureInPicture(it) } },
                                        label = { Text("Floating") },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = Icons.Default.PictureInPictureAlt,
                                                contentDescription = "Floating Window",
                                                modifier = Modifier.size(18.dp)
                                            )
                                        },
                                        colors = FilterChipDefaults.filterChipColors(
                                            containerColor = FrostedGlassWhite10,
                                            labelColor = FrostedTextPrimary,
                                            iconColor = FrostedTextSecondary
                                        ),
                                        border = chipBorder
                                    )
                                }

                                // Share
                                item {
                                    FilterChip(
                                        selected = false,
                                        onClick = { activity?.let { viewModel.shareVideo(it) } },
                                        label = { Text("Share") },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = Icons.Default.Share,
                                                contentDescription = null,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        },
                                        colors = FilterChipDefaults.filterChipColors(
                                            containerColor = FrostedGlassWhite10,
                                            labelColor = FrostedTextPrimary,
                                            iconColor = FrostedTextSecondary
                                        ),
                                        border = chipBorder
                                    )
                                }

                                // Save to Playlist
                                item {
                                    FilterChip(
                                        selected = false,
                                        onClick = { onAddToPlaylist(currentVideo) },
                                        label = { Text("Save") },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = Icons.Default.PlaylistAdd,
                                                contentDescription = null,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        },
                                        colors = FilterChipDefaults.filterChipColors(
                                            containerColor = FrostedGlassWhite10,
                                            labelColor = FrostedTextPrimary,
                                            iconColor = FrostedTextSecondary
                                        ),
                                        border = chipBorder
                                    )
                                }

                                // Repeat Mode
                                item {
                                    val (repeatLabel, repeatIcon) = when (playbackState.repeatMode) {
                                        RepeatMode.OFF -> "Repeat Off" to Icons.Default.Repeat
                                        RepeatMode.ONE -> "Repeat 1" to Icons.Default.RepeatOne
                                        RepeatMode.ALL -> "Repeat All" to Icons.Default.Repeat
                                    }
                                    FilterChip(
                                        selected = playbackState.repeatMode != RepeatMode.OFF,
                                        onClick = { viewModel.cycleRepeatMode() },
                                        label = { Text(repeatLabel) },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = repeatIcon,
                                                contentDescription = null,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        },
                                        colors = FilterChipDefaults.filterChipColors(
                                            containerColor = FrostedGlassWhite10,
                                            labelColor = FrostedTextPrimary,
                                            iconColor = FrostedTextSecondary,
                                            selectedContainerColor = YouTubeRed.copy(alpha = 0.85f),
                                            selectedLabelColor = Color.White,
                                            selectedLeadingIconColor = Color.White
                                        ),
                                        border = chipBorder
                                    )
                                }

                                // Speed
                                item {
                                    FilterChip(
                                        selected = playbackState.playbackSpeed != 1.0f,
                                        onClick = { showSpeedDialog = true },
                                        label = { Text("${playbackState.playbackSpeed}x") },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = Icons.Default.Speed,
                                                contentDescription = null,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        },
                                        colors = FilterChipDefaults.filterChipColors(
                                            containerColor = FrostedGlassWhite10,
                                            labelColor = FrostedTextPrimary,
                                            iconColor = FrostedTextSecondary,
                                            selectedContainerColor = YouTubeRed.copy(alpha = 0.85f),
                                            selectedLabelColor = Color.White,
                                            selectedLeadingIconColor = Color.White
                                        ),
                                        border = chipBorder
                                    )
                                }

                                // Background Play
                                item {
                                    FilterChip(
                                        selected = playbackState.isBackgroundPlayEnabled,
                                        onClick = { viewModel.toggleBackgroundPlay() },
                                        label = { Text("Background") },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = Icons.Default.Headphones,
                                                contentDescription = null,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        },
                                        colors = FilterChipDefaults.filterChipColors(
                                            containerColor = FrostedGlassWhite10,
                                            labelColor = FrostedTextPrimary,
                                            iconColor = FrostedTextSecondary,
                                            selectedContainerColor = YouTubeRed.copy(alpha = 0.85f),
                                            selectedLabelColor = Color.White,
                                            selectedLeadingIconColor = Color.White
                                        ),
                                        border = chipBorder
                                    )
                                }
                            }
                        }
                    }

                    // Section Divider & Next Videos Header
                    item {
                        Surface(
                            color = FrostedGlassBorderSubtle,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                        ) {}
                        Text(
                            text = "Up Next",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = FrostedTextPrimary,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                        )
                    }

                    // Recommended / Next videos in playlist or library
                    val upNextList = allVideos.filter { it.id != currentVideo.id }
                    items(upNextList, key = { it.id }) { video ->
                        VideoCard(
                            video = video,
                            onClick = { viewModel.playVideo(video, allVideos) },
                            onAddToPlaylist = { onAddToPlaylist(video) },
                            onShare = { activity?.let { viewModel.shareVideo(it) } },
                            onPlayBackground = {
                                viewModel.playVideo(video, allVideos)
                            },
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }
            }
        }
    }

    // Dialogs
    if (showSpeedDialog) {
        SpeedSelectorDialog(
            currentSpeed = playbackState.playbackSpeed,
            onSpeedSelected = { speed -> viewModel.setPlaybackSpeed(speed) },
            onDismiss = { showSpeedDialog = false }
        )
    }

    if (showSubtitleDialog) {
        SubtitleDialog(
            subtitlesEnabled = playbackState.subtitlesEnabled,
            currentFileName = playbackState.subtitleFileName,
            onToggleSubtitles = { viewModel.toggleSubtitles() },
            onSubtitleFileSelected = { uri, name -> viewModel.loadSubtitles(uri, name) },
            onDismiss = { showSubtitleDialog = false }
        )
    }

    if (showCastDialog) {
        CastDialog(
            videoTitle = currentVideo.title,
            onDismiss = { showCastDialog = false }
        )
    }
}

private fun formatDuration(ms: Long): String {
    val totalSeconds = (ms / 1000).coerceAtLeast(0)
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return if (hours > 0) {
        String.format("%d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format("%02d:%02d", minutes, seconds)
    }
}
