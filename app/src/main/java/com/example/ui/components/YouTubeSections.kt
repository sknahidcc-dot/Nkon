package com.example.ui.components

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PictureInPictureAlt
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlaylistPlay
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.PlaylistEntity
import com.example.data.model.VideoItem
import com.example.data.model.WatchHistoryEntity
import com.example.ui.theme.FrostedDarkBackground
import com.example.ui.theme.FrostedGlassBorderSubtle
import com.example.ui.theme.FrostedGlassWhite05
import com.example.ui.theme.FrostedGlassWhite10
import com.example.ui.theme.FrostedGlassWhite15
import com.example.ui.theme.FrostedTextPrimary
import com.example.ui.theme.FrostedTextSecondary
import com.example.ui.theme.YouTubeRed

/**
 * Official YouTube Category Filter Chips Row (with Explore Compass pill)
 */
@Composable
fun CategoryFilterRow(
    selectedCategory: String,
    onCategorySelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val categories = listOf("All", "Shorts", "Videos", "Folders", "Playlists")

    LazyRow(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        // Explore Compass Pill (Official YouTube style)
        item {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = FrostedGlassWhite10,
                border = BorderStroke(1.dp, FrostedGlassBorderSubtle),
                modifier = Modifier
                    .height(34.dp)
                    .clickable { onCategorySelected("All") }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Explore,
                        contentDescription = "Explore",
                        tint = FrostedTextPrimary,
                        modifier = Modifier.size(17.dp)
                    )
                }
            }
        }

        items(categories) { category ->
            val isSelected = selectedCategory == category
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (isSelected) Color.White else FrostedGlassWhite10,
                border = BorderStroke(1.dp, if (isSelected) Color.White else FrostedGlassBorderSubtle),
                modifier = Modifier
                    .height(34.dp)
                    .clickable { onCategorySelected(category) }
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.padding(horizontal = 14.dp)
                ) {
                    Text(
                        text = category,
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) Color.Black else FrostedTextPrimary
                    )
                }
            }
        }
    }
}

/**
 * YouTube Short Card (Vertical 9:16 aspect ratio, cropped thumbnail, title scrim overlay)
 */
@Composable
fun YouTubeShortCard(
    video: VideoItem,
    onClick: () -> Unit,
    onFloatingPip: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showMenu by remember { mutableStateOf(false) }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF181818),
        modifier = modifier
            .width(155.dp)
            .height(255.dp)
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, FrostedGlassBorderSubtle, RoundedCornerShape(12.dp))
            .clickable { onClick() }
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(video.uri)
                    .crossfade(true)
                    .build(),
                contentDescription = video.title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // Scrim overlay at bottom for readability
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.3f),
                                Color.Black.copy(alpha = 0.9f)
                            ),
                            startY = 180f
                        )
                    )
            )

            // Top action bar: 3-dots menu
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
            ) {
                IconButton(
                    onClick = { showMenu = true },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Options",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                    modifier = Modifier.background(FrostedDarkBackground)
                ) {
                    DropdownMenuItem(
                        text = { Text("Play Video", color = FrostedTextPrimary) },
                        leadingIcon = {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = YouTubeRed)
                        },
                        onClick = {
                            showMenu = false
                            onClick()
                        }
                    )
                    if (onFloatingPip != null) {
                        DropdownMenuItem(
                            text = { Text("Floating PiP", color = FrostedTextPrimary) },
                            leadingIcon = {
                                Icon(Icons.Default.PictureInPictureAlt, contentDescription = null, tint = YouTubeRed)
                            },
                            onClick = {
                                showMenu = false
                                onFloatingPip()
                            }
                        )
                    }
                }
            }

            // Bottom metadata: Title, duration, folder
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(10.dp)
            ) {
                Text(
                    text = video.title,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 16.sp
                )
                Spacer(modifier = Modifier.height(5.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = Color.Black.copy(alpha = 0.75f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = video.getFormattedDuration(),
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = video.folderName,
                        color = Color.White.copy(alpha = 0.75f),
                        fontSize = 10.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

/**
 * Official YouTube Shorts Shelf (Displayed between video feed cards)
 */
@Composable
fun YouTubeShortsShelf(
    videos: List<VideoItem>,
    onShortClick: (VideoItem) -> Unit,
    onMoreClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
    ) {
        // Shelf Header: Red Shorts badge + title + options
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = YouTubeRed,
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.size(26.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = "Shorts",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Shorts",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = FrostedTextPrimary
                )
            }

            IconButton(onClick = onMoreClick) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "More Shorts",
                    tint = FrostedTextSecondary
                )
            }
        }

        // Horizontal Row of Vertical Shorts cards
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
        ) {
            val displayVideos = videos.take(8)
            items(displayVideos, key = { "shelf_short_${it.id}" }) { video ->
                YouTubeShortCard(
                    video = video,
                    onClick = { onShortClick(video) }
                )
            }
        }
    }
}

/**
 * Dedicated Shorts Screen (Tab 1)
 */
@Composable
fun ShortsScreen(
    videos: List<VideoItem>,
    onPlayVideo: (VideoItem) -> Unit,
    onFloatingPip: (VideoItem) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = YouTubeRed,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.size(30.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Bolt,
                        contentDescription = "Shorts",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "Shorts Feed",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = FrostedTextPrimary
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "(${videos.size})",
                color = FrostedTextSecondary,
                fontSize = 14.sp
            )
        }

        if (videos.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("No videos found to display as Shorts", color = FrostedTextSecondary)
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(videos, key = { "shorts_grid_${it.id}" }) { video ->
                    YouTubeShortCard(
                        video = video,
                        onClick = { onPlayVideo(video) },
                        onFloatingPip = { onFloatingPip(video) },
                        modifier = Modifier.fillMaxWidth().height(260.dp)
                    )
                }
            }
        }
    }
}

/**
 * Official YouTube "You" Screen (Tab 4)
 */
@Composable
fun YouTubeYouScreen(
    allVideos: List<VideoItem>,
    foldersCount: Int,
    watchHistory: List<WatchHistoryEntity>,
    playlists: List<PlaylistEntity>,
    onPlayVideo: (VideoItem) -> Unit,
    onClearHistory: () -> Unit,
    onDeleteHistoryItem: (String) -> Unit,
    onCreatePlaylist: () -> Unit,
    onOpenFolder: (String) -> Unit,
    onFloatingPip: () -> Unit,
    onOpenFile: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // User Profile / Library Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = YouTubeRed,
                    modifier = Modifier.size(60.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "N",
                            fontWeight = FontWeight.Bold,
                            fontSize = 28.sp,
                            color = Color.White
                        )
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = "Nakon Player",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = FrostedTextPrimary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${allVideos.size} videos • $foldersCount folders",
                        fontSize = 13.sp,
                        color = FrostedTextSecondary
                    )
                }
            }
        }

        // Quick feature pills (Floating PiP, Background Play, Open File)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = FrostedGlassWhite10,
                    border = BorderStroke(1.dp, FrostedGlassBorderSubtle),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onFloatingPip() }
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 12.dp, horizontal = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PictureInPictureAlt,
                            contentDescription = "Floating PiP",
                            tint = YouTubeRed,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Floating PiP", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = FrostedTextPrimary)
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = FrostedGlassWhite10,
                    border = BorderStroke(1.dp, FrostedGlassBorderSubtle),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onOpenFile() }
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 12.dp, horizontal = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.FolderOpen,
                            contentDescription = "Open Storage",
                            tint = YouTubeRed,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Open File", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = FrostedTextPrimary)
                    }
                }
            }
        }

        // Watch History Section
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "History",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = FrostedTextPrimary
                    )
                    if (watchHistory.isNotEmpty()) {
                        TextButton(onClick = onClearHistory) {
                            Text("Clear", color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
                        }
                    }
                }

                if (watchHistory.isEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = FrostedGlassWhite05,
                        border = BorderStroke(1.dp, FrostedGlassBorderSubtle),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.History, contentDescription = null, tint = FrostedTextSecondary, modifier = Modifier.size(32.dp))
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("No watch history yet", fontSize = 13.sp, color = FrostedTextSecondary)
                        }
                    }
                } else {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        items(watchHistory, key = { "you_hist_${it.videoUriString}" }) { item ->
                            val matchedVideo = allVideos.find { it.uri.toString() == item.videoUriString }
                            Card(
                                modifier = Modifier
                                    .width(160.dp)
                                    .clickable {
                                        if (matchedVideo != null) {
                                            onPlayVideo(matchedVideo)
                                        }
                                    },
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = FrostedGlassWhite10),
                                border = BorderStroke(1.dp, FrostedGlassBorderSubtle)
                            ) {
                                Column {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(90.dp)
                                            .background(Color.Black)
                                    ) {
                                        if (matchedVideo != null) {
                                            AsyncImage(
                                                model = ImageRequest.Builder(context)
                                                    .data(matchedVideo.uri)
                                                    .crossfade(true)
                                                    .build(),
                                                contentDescription = item.title,
                                                modifier = Modifier.fillMaxSize(),
                                                contentScale = ContentScale.Crop
                                            )
                                        }
                                        LinearProgressIndicator(
                                            progress = { item.progressPercent },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(3.dp)
                                                .align(Alignment.BottomCenter),
                                            color = YouTubeRed,
                                            trackColor = Color.Transparent
                                        )
                                    }
                                    Column(modifier = Modifier.padding(8.dp)) {
                                        Text(
                                            text = item.title,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            color = FrostedTextPrimary
                                        )
                                        Text(
                                            text = "${(item.progressPercent * 100).toInt()}% watched",
                                            fontSize = 10.sp,
                                            color = FrostedTextSecondary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Playlists Section
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Playlists",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = FrostedTextPrimary
                    )
                    TextButton(onClick = onCreatePlaylist) {
                        Text("+ New playlist", color = YouTubeRed, fontSize = 13.sp)
                    }
                }

                if (playlists.isEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = FrostedGlassWhite05,
                        border = BorderStroke(1.dp, FrostedGlassBorderSubtle),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.PlaylistPlay, contentDescription = null, tint = FrostedTextSecondary, modifier = Modifier.size(32.dp))
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("Create playlists to organize favorite videos", fontSize = 13.sp, color = FrostedTextSecondary)
                        }
                    }
                } else {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        items(playlists, key = { "you_pl_${it.id}" }) { playlist ->
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = FrostedGlassWhite10,
                                border = BorderStroke(1.dp, FrostedGlassBorderSubtle),
                                modifier = Modifier.width(140.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Icon(Icons.Default.PlaylistPlay, contentDescription = null, tint = YouTubeRed, modifier = Modifier.size(24.dp))
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = playlist.name,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        color = FrostedTextPrimary
                                    )
                                    Text("Collection", fontSize = 11.sp, color = FrostedTextSecondary)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
