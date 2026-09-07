package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brightness5
import androidx.compose.material.icons.filled.BrightnessMedium
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.VolumeDown
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.FrostedDialogBackground
import com.example.ui.theme.FrostedGlassBorder
import com.example.ui.theme.FrostedGlassBorderLight
import com.example.ui.theme.FrostedGlassWhite15
import com.example.ui.theme.FrostedGlassWhite20
import com.example.ui.theme.FrostedGlassWhite30
import com.example.ui.theme.FrostedTextPrimary
import com.example.ui.theme.FrostedTextSecondary
import com.example.ui.theme.YouTubeRed
import kotlinx.coroutines.delay
import kotlin.math.abs

enum class DragMode {
    NONE,
    BRIGHTNESS,
    VOLUME,
    SEEK,
    SWIPE_DOWN_MINIMIZE
}

@Composable
fun GestureControlOverlay(
    modifier: Modifier = Modifier,
    volumePercent: Float,
    brightnessPercent: Float,
    currentPositionMs: Long,
    durationMs: Long,
    isControlsLocked: Boolean,
    onSingleTap: () -> Unit,
    onDoubleTapSeek: (Long) -> Unit,
    onAdjustVolume: (Float) -> Unit,
    onAdjustBrightness: (Float) -> Unit,
    onSeekTo: (Long) -> Unit,
    onSwipeDownMinimize: () -> Unit
) {
    var dragMode by remember { mutableStateOf(DragMode.NONE) }
    var totalDragY by remember { mutableFloatStateOf(0f) }
    var totalDragX by remember { mutableFloatStateOf(0f) }
    var seekPreviewPosMs by remember { mutableLongStateOf(0L) }
    var seekDeltaSec by remember { mutableLongStateOf(0L) }

    // Double-tap visual ripple state
    var doubleTapSeekLeft by remember { mutableStateOf(false) }
    var doubleTapSeekRight by remember { mutableStateOf(false) }

    LaunchedEffect(doubleTapSeekLeft) {
        if (doubleTapSeekLeft) {
            delay(650)
            doubleTapSeekLeft = false
        }
    }

    LaunchedEffect(doubleTapSeekRight) {
        if (doubleTapSeekRight) {
            delay(650)
            doubleTapSeekRight = false
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("gesture_overlay")
            .pointerInput(isControlsLocked) {
                if (isControlsLocked) {
                    detectTapGestures(onTap = { onSingleTap() })
                } else {
                    detectTapGestures(
                        onTap = { onSingleTap() },
                        onDoubleTap = { offset ->
                            val width = size.width
                            if (offset.x < width * 0.4f) {
                                // Double tap left -> rewind 10s
                                doubleTapSeekLeft = true
                                onDoubleTapSeek(-10000L)
                            } else if (offset.x > width * 0.6f) {
                                // Double tap right -> forward 10s
                                doubleTapSeekRight = true
                                onDoubleTapSeek(10000L)
                            } else {
                                onSingleTap()
                            }
                        }
                    )
                }
            }
            .pointerInput(isControlsLocked) {
                if (isControlsLocked) return@pointerInput
                detectDragGestures(
                    onDragStart = { offset ->
                        val width = size.width
                        totalDragY = 0f
                        totalDragX = 0f
                        seekPreviewPosMs = currentPositionMs
                        seekDeltaSec = 0L

                        // Determine primary drag intention based on starting area
                        dragMode = when {
                            offset.x < width * 0.35f -> DragMode.BRIGHTNESS
                            offset.x > width * 0.65f -> DragMode.VOLUME
                            else -> DragMode.NONE
                        }
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        totalDragY += dragAmount.y
                        totalDragX += dragAmount.x

                        if (dragMode == DragMode.NONE) {
                            if (abs(totalDragX) > abs(totalDragY) && abs(totalDragX) > 20f) {
                                dragMode = DragMode.SEEK
                            } else if (totalDragY > 40f && abs(totalDragY) > abs(totalDragX)) {
                                dragMode = DragMode.SWIPE_DOWN_MINIMIZE
                            }
                        }

                        when (dragMode) {
                            DragMode.BRIGHTNESS -> {
                                val delta = -dragAmount.y / 500f
                                onAdjustBrightness(delta)
                            }
                            DragMode.VOLUME -> {
                                val delta = -dragAmount.y / 500f
                                onAdjustVolume(delta)
                            }
                            DragMode.SEEK -> {
                                val seconds = (totalDragX / 10f).toLong()
                                seekDeltaSec = seconds
                                seekPreviewPosMs = (currentPositionMs + seconds * 1000L).coerceIn(0L, durationMs)
                            }
                            DragMode.SWIPE_DOWN_MINIMIZE -> {
                                if (totalDragY > 150f) {
                                    onSwipeDownMinimize()
                                    dragMode = DragMode.NONE
                                }
                            }
                            DragMode.NONE -> {}
                        }
                    },
                    onDragEnd = {
                        if (dragMode == DragMode.SEEK) {
                            onSeekTo(seekPreviewPosMs)
                        }
                        dragMode = DragMode.NONE
                    },
                    onDragCancel = {
                        dragMode = DragMode.NONE
                    }
                )
            }
    ) {
        // Double-tap left animated ripple
        AnimatedVisibility(
            visible = doubleTapSeekLeft,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 32.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = FrostedGlassWhite20,
                modifier = Modifier
                    .size(90.dp)
                    .border(1.dp, FrostedGlassBorderLight, CircleShape)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.FastRewind,
                        contentDescription = "Rewind 10s",
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                    Text(
                        text = "10 seconds",
                        color = FrostedTextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Double-tap right animated ripple
        AnimatedVisibility(
            visible = doubleTapSeekRight,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 32.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = FrostedGlassWhite20,
                modifier = Modifier
                    .size(90.dp)
                    .border(1.dp, FrostedGlassBorderLight, CircleShape)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.FastForward,
                        contentDescription = "Forward 10s",
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                    Text(
                        text = "10 seconds",
                        color = FrostedTextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Left vertical brightness slider indicator (matching Design HTML)
        AnimatedVisibility(
            visible = dragMode == DragMode.BRIGHTNESS,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 16.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier
                    .background(FrostedDialogBackground, RoundedCornerShape(12.dp))
                    .border(1.dp, FrostedGlassBorder, RoundedCornerShape(12.dp))
                    .padding(horizontal = 8.dp, vertical = 12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height(96.dp)
                        .clip(CircleShape)
                        .background(FrostedGlassWhite20)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(brightnessPercent.coerceIn(0f, 1f))
                            .align(Alignment.BottomCenter)
                            .clip(CircleShape)
                            .background(Color.White)
                    )
                }
                Icon(
                    imageVector = Icons.Default.Brightness5,
                    contentDescription = "Brightness",
                    tint = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        // Right vertical volume slider indicator (matching Design HTML)
        AnimatedVisibility(
            visible = dragMode == DragMode.VOLUME,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 16.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier
                    .background(FrostedDialogBackground, RoundedCornerShape(12.dp))
                    .border(1.dp, FrostedGlassBorder, RoundedCornerShape(12.dp))
                    .padding(horizontal = 8.dp, vertical = 12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height(96.dp)
                        .clip(CircleShape)
                        .background(FrostedGlassWhite20)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(volumePercent.coerceIn(0f, 1f))
                            .align(Alignment.BottomCenter)
                            .clip(CircleShape)
                            .background(Color.White)
                    )
                }
                val volIcon = when {
                    volumePercent <= 0.01f -> Icons.Default.VolumeMute
                    volumePercent < 0.5f -> Icons.Default.VolumeDown
                    else -> Icons.Default.VolumeUp
                }
                Icon(
                    imageVector = volIcon,
                    contentDescription = "Volume",
                    tint = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        // Gesture HUD for Brightness
        if (dragMode == DragMode.BRIGHTNESS) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = FrostedDialogBackground,
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(16.dp)
                    .border(1.dp, FrostedGlassBorder, RoundedCornerShape(16.dp))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.BrightnessMedium,
                        contentDescription = "Brightness",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Brightness: ${(brightnessPercent * 100).toInt()}%",
                            color = FrostedTextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = { brightnessPercent },
                            modifier = Modifier
                                .width(120.dp)
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = YouTubeRed,
                            trackColor = FrostedGlassWhite15
                        )
                    }
                }
            }
        }

        // Gesture HUD for Volume
        if (dragMode == DragMode.VOLUME) {
            val volIcon = when {
                volumePercent <= 0.01f -> Icons.Default.VolumeMute
                volumePercent < 0.5f -> Icons.Default.VolumeDown
                else -> Icons.Default.VolumeUp
            }
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = FrostedDialogBackground,
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(16.dp)
                    .border(1.dp, FrostedGlassBorder, RoundedCornerShape(16.dp))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = volIcon,
                        contentDescription = "Volume",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Volume: ${(volumePercent * 100).toInt()}%",
                            color = FrostedTextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = { volumePercent },
                            modifier = Modifier
                                .width(120.dp)
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = YouTubeRed,
                            trackColor = FrostedGlassWhite15
                        )
                    }
                }
            }
        }

        // Gesture HUD for Scrub Seek
        if (dragMode == DragMode.SEEK) {
            val sign = if (seekDeltaSec >= 0) "+$seekDeltaSec" else "$seekDeltaSec"
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = FrostedDialogBackground,
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(16.dp)
                    .border(1.dp, FrostedGlassBorder, RoundedCornerShape(16.dp))
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "[$sign sec]",
                        color = YouTubeRed,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = formatTime(seekPreviewPosMs) + " / " + formatTime(durationMs),
                        color = FrostedTextPrimary,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

private fun formatTime(ms: Long): String {
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
