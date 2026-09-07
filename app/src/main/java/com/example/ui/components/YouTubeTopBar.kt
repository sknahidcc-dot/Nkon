package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cast
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.FrostedDarkBackground
import com.example.ui.theme.FrostedGlassBorder
import com.example.ui.theme.FrostedGlassBorderSubtle
import com.example.ui.theme.FrostedGlassWhite10
import com.example.ui.theme.FrostedGlassWhite15
import com.example.ui.theme.FrostedTextPrimary
import com.example.ui.theme.FrostedTextSecondary
import com.example.ui.theme.YouTubeRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun YouTubeTopBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onCastClick: () -> Unit,
    onThemeClick: () -> Unit,
    onOpenLocalFile: () -> Unit
) {
    var isSearchActive by remember { mutableStateOf(false) }
    var showNotificationsDialog by remember { mutableStateOf(false) }

    Surface(
        color = FrostedDarkBackground,
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .drawBehind {
                drawLine(
                    color = FrostedGlassBorderSubtle,
                    start = Offset(0f, size.height),
                    end = Offset(size.width, size.height),
                    strokeWidth = 1.dp.toPx()
                )
            }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            if (isSearchActive) {
                // Search Input Field with Frosted Glass look
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    placeholder = { Text("Search videos, folders...", fontSize = 14.sp, color = FrostedTextSecondary) },
                    singleLine = true,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = FrostedTextPrimary
                        )
                    },
                    trailingIcon = {
                        IconButton(onClick = {
                            if (searchQuery.isNotEmpty()) {
                                onSearchQueryChange("")
                            } else {
                                isSearchActive = false
                            }
                        }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Close Search", tint = FrostedTextSecondary)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .border(1.dp, FrostedGlassBorder, RoundedCornerShape(26.dp))
                        .testTag("search_text_field"),
                    shape = RoundedCornerShape(26.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = FrostedGlassWhite10,
                        unfocusedContainerColor = FrostedGlassWhite10,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedTextColor = FrostedTextPrimary,
                        unfocusedTextColor = FrostedTextPrimary
                    )
                )
            } else {
                // Logo & Title (Frosted Red Play Badge + LocalTube title)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    // Red brand square with white play triangle
                    Surface(
                        color = YouTubeRed,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.size(32.dp, 32.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Logo",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Text(
                        text = "Nakon Player",
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.5).sp,
                        color = FrostedTextPrimary
                    )
                }

                // Frosted Action Icons
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Cast Button with frosted feedback
                    Surface(
                        shape = CircleShape,
                        color = FrostedGlassWhite10,
                        modifier = Modifier
                            .size(38.dp)
                            .border(1.dp, FrostedGlassBorderSubtle, CircleShape)
                    ) {
                        IconButton(
                            onClick = onCastClick,
                            modifier = Modifier.testTag("cast_top_bar_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Cast,
                                contentDescription = "Cast",
                                tint = FrostedTextPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    // Notification Bell with "9+" Badge (Official YouTube style)
                    Box {
                        Surface(
                            shape = CircleShape,
                            color = FrostedGlassWhite10,
                            modifier = Modifier
                                .size(38.dp)
                                .border(1.dp, FrostedGlassBorderSubtle, CircleShape)
                        ) {
                            IconButton(
                                onClick = { showNotificationsDialog = true },
                                modifier = Modifier.testTag("notifications_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Notifications,
                                    contentDescription = "Notifications",
                                    tint = FrostedTextPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        // "9+" Red Pill Badge
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = YouTubeRed,
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(top = 2.dp, end = 2.dp)
                        ) {
                            Text(
                                text = "9+",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }

                    // Open File Button (Pick local video)
                    Surface(
                        shape = CircleShape,
                        color = FrostedGlassWhite10,
                        modifier = Modifier
                            .size(38.dp)
                            .border(1.dp, FrostedGlassBorderSubtle, CircleShape)
                    ) {
                        IconButton(
                            onClick = onOpenLocalFile,
                            modifier = Modifier.testTag("open_file_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.FolderOpen,
                                contentDescription = "Open Local File",
                                tint = FrostedTextPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    // Search Button
                    Surface(
                        shape = CircleShape,
                        color = FrostedGlassWhite10,
                        modifier = Modifier
                            .size(38.dp)
                            .border(1.dp, FrostedGlassBorderSubtle, CircleShape)
                    ) {
                        IconButton(
                            onClick = { isSearchActive = true },
                            modifier = Modifier.testTag("search_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = FrostedTextPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    // Theme Button
                    Surface(
                        shape = CircleShape,
                        color = FrostedGlassWhite15,
                        modifier = Modifier
                            .size(38.dp)
                            .border(1.dp, FrostedGlassBorder, CircleShape)
                    ) {
                        IconButton(
                            onClick = onThemeClick,
                            modifier = Modifier.testTag("theme_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.DarkMode,
                                contentDescription = "Theme",
                                tint = FrostedTextPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    if (showNotificationsDialog) {
        AlertDialog(
            onDismissRequest = { showNotificationsDialog = false },
            containerColor = FrostedDarkBackground,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = null,
                        tint = YouTubeRed,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Notifications", color = FrostedTextPrimary, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                androidx.compose.foundation.layout.Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    NotificationItem(
                        title = "Nakon Player Ready",
                        desc = "Full-screen auto-rotation and floating PiP are active.",
                        time = "Just now"
                    )
                    NotificationItem(
                        title = "Background Playback Enabled",
                        desc = "Videos can continue playing when screen is off or app is minimized.",
                        time = "10m ago"
                    )
                    NotificationItem(
                        title = "Library Synchronized",
                        desc = "Local video files loaded with thumbnail indexing.",
                        time = "1h ago"
                    )
                }
            },
            confirmButton = {
                androidx.compose.material3.TextButton(onClick = { showNotificationsDialog = false }) {
                    Text("Close", color = YouTubeRed)
                }
            }
        )
    }
}

@Composable
private fun NotificationItem(
    title: String,
    desc: String,
    time: String
) {
    Surface(
        color = FrostedGlassWhite10,
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        androidx.compose.foundation.layout.Column(
            modifier = Modifier.padding(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = title,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = FrostedTextPrimary
                )
                Text(
                    text = time,
                    fontSize = 11.sp,
                    color = FrostedTextSecondary
                )
            }
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = desc,
                fontSize = 12.sp,
                color = FrostedTextSecondary
            )
        }
    }
}
