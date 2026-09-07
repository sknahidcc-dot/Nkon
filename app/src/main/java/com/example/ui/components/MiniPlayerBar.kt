package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PictureInPictureAlt
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.player.PlaybackState
import com.example.ui.theme.FrostedDarkBackground
import com.example.ui.theme.FrostedGlassBorder
import com.example.ui.theme.FrostedGlassBorderSubtle
import com.example.ui.theme.FrostedGlassWhite10
import com.example.ui.theme.FrostedGlassWhite15
import com.example.ui.theme.FrostedGlassWhite20
import com.example.ui.theme.FrostedTextPrimary
import com.example.ui.theme.FrostedTextSecondary
import com.example.ui.theme.YouTubeRed
import kotlin.math.roundToInt

@Composable
fun MiniPlayerBar(
    state: PlaybackState,
    onExpand: () -> Unit,
    onTogglePlay: () -> Unit,
    onClose: () -> Unit,
    onPip: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val video = state.currentVideo ?: return
    val context = LocalContext.current
    var offsetX by remember { mutableFloatStateOf(0f) }

    val progress = if (state.durationMs > 0) {
        (state.currentPositionMs.toFloat() / state.durationMs.toFloat()).coerceIn(0f, 1f)
    } else 0f

    Surface(
        color = FrostedDarkBackground.copy(alpha = 0.94f),
        tonalElevation = 8.dp,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        modifier = modifier
            .fillMaxWidth()
            .offset { IntOffset(offsetX.roundToInt(), 0) }
            .border(1.dp, FrostedGlassBorder, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onHorizontalDrag = { change, dragAmount ->
                        change.consume()
                        offsetX += dragAmount
                    },
                    onDragEnd = {
                        if (kotlin.math.abs(offsetX) > 200f) {
                            onClose()
                        }
                        offsetX = 0f
                    },
                    onDragCancel = {
                        offsetX = 0f
                    }
                )
            }
            .clickable { onExpand() }
            .testTag("mini_player_bar")
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .padding(horizontal = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Video thumbnail with rounded corners and frosted border
                Box(
                    modifier = Modifier
                        .height(48.dp)
                        .aspectRatio(16f / 9f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.Black)
                        .border(1.dp, FrostedGlassBorderSubtle, RoundedCornerShape(8.dp))
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(video.uri)
                            .crossfade(true)
                            .build(),
                        contentDescription = video.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Title & Subtitle
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = video.title,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = FrostedTextPrimary
                    )
                    Text(
                        text = "${video.folderName} • ${video.getFormattedDuration()}",
                        fontSize = 12.sp,
                        color = FrostedTextSecondary
                    )
                }

                // Frosted Play / Pause button
                Surface(
                    shape = CircleShape,
                    color = FrostedGlassWhite15,
                    modifier = Modifier
                        .size(38.dp)
                        .border(1.dp, FrostedGlassBorder, CircleShape)
                ) {
                    IconButton(
                        onClick = onTogglePlay,
                        modifier = Modifier.testTag("mini_player_play_pause")
                    ) {
                        Icon(
                            imageVector = if (state.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (state.isPlaying) "Pause" else "Play",
                            tint = FrostedTextPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                if (onPip != null) {
                    Spacer(modifier = Modifier.width(6.dp))

                    // PiP Floating button
                    Surface(
                        shape = CircleShape,
                        color = FrostedGlassWhite10,
                        modifier = Modifier
                            .size(34.dp)
                            .border(1.dp, FrostedGlassBorderSubtle, CircleShape)
                    ) {
                        IconButton(
                            onClick = onPip,
                            modifier = Modifier.testTag("mini_player_pip")
                        ) {
                            Icon(
                                imageVector = Icons.Default.PictureInPictureAlt,
                                contentDescription = "Floating Window",
                                tint = FrostedTextPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Close Button
                Surface(
                    shape = CircleShape,
                    color = FrostedGlassWhite10,
                    modifier = Modifier
                        .size(34.dp)
                        .border(1.dp, FrostedGlassBorderSubtle, CircleShape)
                ) {
                    IconButton(
                        onClick = onClose,
                        modifier = Modifier.testTag("mini_player_close")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = FrostedTextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Progress bar on bottom edge
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.5.dp),
                color = YouTubeRed,
                trackColor = FrostedGlassWhite10
            )
        }
    }
}
