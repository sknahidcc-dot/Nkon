package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PictureInPictureAlt
import androidx.compose.material.icons.filled.PlayCircleFilled
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.VideoItem
import com.example.ui.theme.FrostedDarkBackground
import com.example.ui.theme.FrostedDialogBackground
import com.example.ui.theme.FrostedGlassBlack80
import com.example.ui.theme.FrostedGlassBorder
import com.example.ui.theme.FrostedGlassBorderSubtle
import com.example.ui.theme.FrostedGlassWhite05
import com.example.ui.theme.FrostedGlassWhite10
import com.example.ui.theme.FrostedGlassWhite15
import com.example.ui.theme.FrostedGlassWhite20
import com.example.ui.theme.FrostedTextPrimary
import com.example.ui.theme.FrostedTextSecondary
import com.example.ui.theme.YouTubeRed

@Composable
fun VideoCard(
    video: VideoItem,
    onClick: () -> Unit,
    onAddToPlaylist: () -> Unit,
    onShare: () -> Unit,
    onPlayBackground: () -> Unit,
    onFloatingPip: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, FrostedGlassBorderSubtle, RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .testTag("video_card_${video.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = FrostedGlassWhite05
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        ) {
            // Video Thumbnail
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .clip(RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp))
                    .background(Color(0xFF1E1E22))
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

                // Center Play Icon watermark in frosted glass circle
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        shape = CircleShape,
                        color = FrostedGlassWhite20,
                        modifier = Modifier
                            .size(44.dp)
                            .border(1.dp, FrostedGlassBorder, CircleShape)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    }
                }

                // Duration Badge (Bottom Right) with frosted glass look
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = FrostedGlassBlack80,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp)
                        .border(1.dp, FrostedGlassBorderSubtle, RoundedCornerShape(4.dp))
                ) {
                    Text(
                        text = video.getFormattedDuration(),
                        color = FrostedTextPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                // Resolution Badge (Bottom Left)
                if (video.resolution.isNotBlank()) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = YouTubeRed.copy(alpha = 0.85f),
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(8.dp)
                            .border(1.dp, FrostedGlassBorderSubtle, RoundedCornerShape(4.dp))
                    ) {
                        Text(
                            text = video.resolution,
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Metadata Row: Folder Icon + Title + Info + 3-dots Menu
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 2.dp),
                verticalAlignment = Alignment.Top
            ) {
                // Folder Avatar Pill with frosted glass
                Surface(
                    shape = CircleShape,
                    color = FrostedGlassWhite10,
                    modifier = Modifier
                        .size(36.dp)
                        .border(1.dp, FrostedGlassBorderSubtle, CircleShape)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Folder,
                            contentDescription = null,
                            tint = YouTubeRed,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                // Title & Details
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = video.title,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        color = FrostedTextPrimary,
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(3.dp))

                    val sizeText = video.getFormattedSize()
                    val detailsText = buildString {
                        append(video.folderName)
                        if (sizeText.isNotBlank()) append(" • $sizeText")
                    }

                    Text(
                        text = detailsText,
                        fontSize = 12.sp,
                        color = FrostedTextSecondary
                    )
                }

                // 3-dots Menu
                Box {
                    IconButton(
                        onClick = { showMenu = true },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Options",
                            tint = FrostedTextSecondary
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                        modifier = Modifier
                            .background(FrostedDialogBackground)
                            .border(1.dp, FrostedGlassBorder, RoundedCornerShape(12.dp))
                    ) {
                        DropdownMenuItem(
                            text = { Text("Add to Playlist", color = FrostedTextPrimary) },
                            onClick = {
                                showMenu = false
                                onAddToPlaylist()
                            },
                            leadingIcon = {
                                Icon(Icons.Default.PlaylistAdd, contentDescription = null, tint = FrostedTextPrimary)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Share Video", color = FrostedTextPrimary) },
                            onClick = {
                                showMenu = false
                                onShare()
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Share, contentDescription = null, tint = FrostedTextPrimary)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Play in Background", color = FrostedTextPrimary) },
                            onClick = {
                                showMenu = false
                                onPlayBackground()
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Headphones, contentDescription = null, tint = FrostedTextPrimary)
                            }
                        )
                        if (onFloatingPip != null) {
                            DropdownMenuItem(
                                text = { Text("Floating Window (PiP)", color = FrostedTextPrimary) },
                                onClick = {
                                    showMenu = false
                                    onFloatingPip()
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.PictureInPictureAlt, contentDescription = null, tint = FrostedTextPrimary)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
