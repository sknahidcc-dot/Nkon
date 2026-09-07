package com.example.ui.screens

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PictureInPictureAlt
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlaylistPlay
import androidx.compose.material.icons.filled.Subscriptions
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.PlaylistPlay
import androidx.compose.material.icons.outlined.Subscriptions
import androidx.compose.material.icons.outlined.VideoLibrary
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.model.PlaylistEntity
import com.example.data.model.VideoItem
import com.example.data.model.WatchHistoryEntity
import com.example.player.VideoPlayerViewModel
import com.example.ui.components.AddToPlaylistDialog
import com.example.ui.components.CastDialog
import com.example.ui.components.CategoryFilterRow
import com.example.ui.components.CreatePlaylistDialog
import com.example.ui.components.MiniPlayerBar
import com.example.ui.components.ShortsScreen
import com.example.ui.components.ThemeDialog
import com.example.ui.components.VideoCard
import com.example.ui.components.YouTubeShortsShelf
import com.example.ui.components.YouTubeTopBar
import com.example.ui.components.YouTubeYouScreen
import com.example.ui.theme.FrostedDarkBackground
import com.example.ui.theme.FrostedGlassBlack80
import com.example.ui.theme.FrostedGlassBorderSubtle
import com.example.ui.theme.FrostedGlassWhite05
import com.example.ui.theme.FrostedGlassWhite10
import com.example.ui.theme.FrostedTextPrimary
import com.example.ui.theme.FrostedTextSecondary
import com.example.ui.theme.YouTubeRed

enum class HomeTab(val title: String, val activeIcon: ImageVector, val inactiveIcon: ImageVector) {
    HOME("Home", Icons.Filled.Home, Icons.Outlined.Home),
    SHORTS("Shorts", Icons.Filled.Bolt, Icons.Outlined.Bolt),
    FOLDERS("Folders", Icons.Filled.Folder, Icons.Outlined.Folder),
    PLAYLISTS("Playlists", Icons.Filled.Subscriptions, Icons.Outlined.Subscriptions),
    YOU("You", Icons.Filled.AccountCircle, Icons.Outlined.AccountCircle)
}

@Composable
fun HomeScreen(
    viewModel: VideoPlayerViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = context as? Activity

    val playbackState by viewModel.playbackState.collectAsState()
    val allVideos by viewModel.allVideos.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val currentTheme by viewModel.currentThemeMode.collectAsState()
    val playlists by viewModel.playlists.collectAsState()
    val watchHistory by viewModel.watchHistory.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }
    var selectedCategoryChip by remember { mutableStateOf("All") }
    var showThemeDialog by remember { mutableStateOf(false) }
    var showCastDialog by remember { mutableStateOf(false) }
    var showCreatePlaylistDialog by remember { mutableStateOf(false) }
    var videoToAddToPlaylist by remember { mutableStateOf<VideoItem?>(null) }
    var selectedFolderFilter by remember { mutableStateOf<String?>(null) }
    var selectedPlaylistForDetail by remember { mutableStateOf<PlaylistEntity?>(null) }

    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) {
        viewModel.loadVideos()
    }

    // Single video file picker
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            val title = uri.lastPathSegment ?: "Local Video"
            val pickedVideo = VideoItem(
                id = System.currentTimeMillis(),
                uri = uri,
                title = title,
                durationMs = 0L,
                folderName = "Storage"
            )
            viewModel.playVideo(pickedVideo)
        }
    }

    LaunchedEffect(Unit) {
        val permissionsToRequest = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(Manifest.permission.READ_MEDIA_VIDEO, Manifest.permission.POST_NOTIFICATIONS)
        } else {
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        permissionLauncher.launch(permissionsToRequest)
    }

    // Filtered videos based on search and optional folder filter
    val filteredVideos = remember(allVideos, searchQuery, selectedFolderFilter) {
        allVideos.filter { video ->
            val matchesSearch = if (searchQuery.isBlank()) true else {
                video.title.contains(searchQuery, ignoreCase = true) ||
                        video.folderName.contains(searchQuery, ignoreCase = true)
            }
            val matchesFolder = if (selectedFolderFilter == null) true else {
                video.folderName.equals(selectedFolderFilter, ignoreCase = true)
            }
            matchesSearch && matchesFolder
        }
    }

    // Folders map
    val foldersMap = remember(allVideos) {
        allVideos.groupBy { it.folderName }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                YouTubeTopBar(
                    searchQuery = searchQuery,
                    onSearchQueryChange = { viewModel.updateSearchQuery(it) },
                    onCastClick = { showCastDialog = true },
                    onThemeClick = { showThemeDialog = true },
                    onOpenLocalFile = {
                        filePickerLauncher.launch(arrayOf("video/*"))
                    }
                )
            },
            bottomBar = {
                Column {
                    // Mini Player Bar (if minimized and active)
                    if (playbackState.isMiniPlayer && playbackState.currentVideo != null) {
                        MiniPlayerBar(
                            state = playbackState,
                            onExpand = { viewModel.expandFromMiniPlayer() },
                            onTogglePlay = { viewModel.togglePlayPause() },
                            onClose = { viewModel.closePlayer() },
                            onPip = { activity?.let { viewModel.enterPictureInPicture(it) } }
                        )
                    }

                    // Bottom Navigation Bar
                    NavigationBar(
                        containerColor = FrostedGlassBlack80,
                        tonalElevation = 0.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(BorderStroke(1.dp, FrostedGlassBorderSubtle))
                            .navigationBarsPadding()
                    ) {
                        HomeTab.values().forEachIndexed { index, tab ->
                            val isSelected = selectedTab == index
                            NavigationBarItem(
                                selected = isSelected,
                                onClick = {
                                    selectedTab = index
                                    if (index != 2) selectedFolderFilter = null
                                    if (index != 3) selectedPlaylistForDetail = null
                                },
                                icon = {
                                    Icon(
                                        imageVector = if (isSelected) tab.activeIcon else tab.inactiveIcon,
                                        contentDescription = tab.title
                                    )
                                },
                                label = {
                                    Text(
                                        text = tab.title,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = YouTubeRed,
                                    selectedTextColor = YouTubeRed,
                                    indicatorColor = FrostedGlassWhite10,
                                    unselectedIconColor = FrostedTextSecondary,
                                    unselectedTextColor = FrostedTextSecondary
                                )
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (selectedTab) {
                    // 0: Official YouTube Home Feed
                    0 -> {
                        Column(modifier = Modifier.fillMaxSize()) {
                            // Category Filter Chips
                            CategoryFilterRow(
                                selectedCategory = selectedCategoryChip,
                                onCategorySelected = { chip ->
                                    selectedCategoryChip = chip
                                    if (chip == "Shorts") {
                                        selectedTab = 1
                                    } else if (chip == "Folders") {
                                        selectedTab = 2
                                    } else if (chip == "Playlists") {
                                        selectedTab = 3
                                    }
                                }
                            )

                            if (filteredVideos.isEmpty()) {
                                EmptyVideosState(
                                    onOpenFile = { filePickerLauncher.launch(arrayOf("video/*")) }
                                )
                            } else {
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    contentPadding = PaddingValues(bottom = 24.dp),
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    // First video card
                                    val firstVideo = filteredVideos.firstOrNull()
                                    if (firstVideo != null) {
                                        item(key = "feed_video_${firstVideo.id}") {
                                            Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                                                VideoCard(
                                                    video = firstVideo,
                                                    onClick = { viewModel.playVideo(firstVideo, filteredVideos) },
                                                    onAddToPlaylist = { videoToAddToPlaylist = firstVideo },
                                                    onShare = { activity?.let { viewModel.shareVideo(it) } },
                                                    onPlayBackground = {
                                                        viewModel.playVideo(firstVideo, filteredVideos)
                                                        viewModel.minimizeToMiniPlayer()
                                                    },
                                                    onFloatingPip = {
                                                        activity?.let {
                                                            viewModel.playVideo(firstVideo, filteredVideos)
                                                            viewModel.enterPictureInPicture(it)
                                                        }
                                                    }
                                                )
                                            }
                                        }
                                    }

                                    // YouTube Shorts Shelf between videos
                                    if (allVideos.isNotEmpty()) {
                                        item(key = "shorts_shelf_feed") {
                                            YouTubeShortsShelf(
                                                videos = allVideos,
                                                onShortClick = { shortVideo ->
                                                    viewModel.playVideo(shortVideo, allVideos)
                                                },
                                                onMoreClick = { selectedTab = 1 }
                                            )
                                        }
                                    }

                                    // Remaining videos
                                    val remainingVideos = if (filteredVideos.size > 1) filteredVideos.drop(1) else emptyList()
                                    items(remainingVideos, key = { "feed_rem_${it.id}" }) { video ->
                                        Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                                            VideoCard(
                                                video = video,
                                                onClick = { viewModel.playVideo(video, filteredVideos) },
                                                onAddToPlaylist = { videoToAddToPlaylist = video },
                                                onShare = { activity?.let { viewModel.shareVideo(it) } },
                                                onPlayBackground = {
                                                    viewModel.playVideo(video, filteredVideos)
                                                    viewModel.minimizeToMiniPlayer()
                                                },
                                                onFloatingPip = {
                                                    activity?.let {
                                                        viewModel.playVideo(video, filteredVideos)
                                                        viewModel.enterPictureInPicture(it)
                                                    }
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 1: Official YouTube Shorts Feed
                    1 -> {
                        ShortsScreen(
                            videos = allVideos,
                            onPlayVideo = { video ->
                                viewModel.playVideo(video, allVideos)
                            },
                            onFloatingPip = { video ->
                                activity?.let {
                                    viewModel.playVideo(video, allVideos)
                                    viewModel.enterPictureInPicture(it)
                                }
                            }
                        )
                    }

                    // 2: Folders Tab
                    2 -> {
                        if (selectedFolderFilter != null) {
                            // Viewing specific folder videos
                            Column(modifier = Modifier.fillMaxSize()) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    TextButton(onClick = { selectedFolderFilter = null }) {
                                        Text("← Back to Folders", color = MaterialTheme.colorScheme.primary)
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = selectedFolderFilter.orEmpty(),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                }

                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                                    verticalArrangement = Arrangement.spacedBy(14.dp)
                                ) {
                                    items(filteredVideos, key = { it.id }) { video ->
                                        VideoCard(
                                            video = video,
                                            onClick = { viewModel.playVideo(video, filteredVideos) },
                                            onAddToPlaylist = { videoToAddToPlaylist = video },
                                            onShare = { activity?.let { viewModel.shareVideo(it) } },
                                            onPlayBackground = {
                                                viewModel.playVideo(video, filteredVideos)
                                                viewModel.minimizeToMiniPlayer()
                                            },
                                            onFloatingPip = {
                                                activity?.let {
                                                    viewModel.playVideo(video, filteredVideos)
                                                    viewModel.enterPictureInPicture(it)
                                                }
                                            }
                                        )
                                    }
                                }
                            }
                        } else {
                            // List all folders
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(foldersMap.keys.toList()) { folderName ->
                                    val count = foldersMap[folderName]?.size ?: 0
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { selectedFolderFilter = folderName },
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = FrostedGlassWhite05
                                        ),
                                        border = BorderStroke(1.dp, FrostedGlassBorderSubtle)
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(16.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Surface(
                                                shape = CircleShape,
                                                color = MaterialTheme.colorScheme.primaryContainer,
                                                modifier = Modifier.size(44.dp)
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Icon(
                                                        imageVector = Icons.Default.Folder,
                                                        contentDescription = null,
                                                        tint = MaterialTheme.colorScheme.primary,
                                                        modifier = Modifier.size(24.dp)
                                                    )
                                                }
                                            }
                                            Spacer(modifier = Modifier.width(16.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = folderName,
                                                    fontSize = 16.sp,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                                Text(
                                                    text = "$count videos",
                                                    fontSize = 12.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                            Text(
                                                text = "View →",
                                                color = MaterialTheme.colorScheme.primary,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 3: Playlists Tab
                    3 -> {
                        Box(modifier = Modifier.fillMaxSize()) {
                            if (playlists.isEmpty()) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(32.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlaylistPlay,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(64.dp)
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = "No Playlists Yet",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "Create custom playlists to organize your favorite video clips.",
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Button(onClick = { showCreatePlaylistDialog = true }) {
                                        Text("+ Create Playlist")
                                    }
                                }
                            } else {
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    item {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(bottom = 6.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "My Playlists (${playlists.size})",
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            TextButton(onClick = { showCreatePlaylistDialog = true }) {
                                                Text("+ New Playlist")
                                            }
                                        }
                                    }

                                    items(playlists, key = { it.id }) { playlist ->
                                        Card(
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(12.dp),
                                            colors = CardDefaults.cardColors(
                                                containerColor = FrostedGlassWhite05
                                            ),
                                            border = BorderStroke(1.dp, FrostedGlassBorderSubtle)
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(14.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Surface(
                                                    shape = CircleShape,
                                                    color = YouTubeRed.copy(alpha = 0.15f),
                                                    modifier = Modifier.size(40.dp)
                                                ) {
                                                    Box(contentAlignment = Alignment.Center) {
                                                        Icon(
                                                            imageVector = Icons.Default.PlaylistPlay,
                                                            contentDescription = null,
                                                            tint = YouTubeRed,
                                                            modifier = Modifier.size(24.dp)
                                                        )
                                                    }
                                                }
                                                Spacer(modifier = Modifier.width(14.dp))
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = playlist.name,
                                                        fontSize = 15.sp,
                                                        fontWeight = FontWeight.SemiBold
                                                    )
                                                    Text(
                                                        text = "Custom Collection",
                                                        fontSize = 12.sp,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                                IconButton(
                                                    onClick = { viewModel.deletePlaylist(playlist.id) }
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Delete,
                                                        contentDescription = "Delete Playlist",
                                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 4: Official YouTube "You" Tab
                    4 -> {
                        YouTubeYouScreen(
                            allVideos = allVideos,
                            foldersCount = foldersMap.size,
                            watchHistory = watchHistory,
                            playlists = playlists,
                            onPlayVideo = { video -> viewModel.playVideo(video) },
                            onClearHistory = { viewModel.clearWatchHistory() },
                            onDeleteHistoryItem = { uri -> viewModel.deleteHistoryItem(uri) },
                            onCreatePlaylist = { showCreatePlaylistDialog = true },
                            onOpenFolder = { folderName ->
                                selectedFolderFilter = folderName
                                selectedTab = 2
                            },
                            onFloatingPip = {
                                if (playbackState.currentVideo != null && activity != null) {
                                    viewModel.enterPictureInPicture(activity)
                                }
                            },
                            onOpenFile = { filePickerLauncher.launch(arrayOf("video/*")) }
                        )
                    }
                }
            }
        }

        // Full Screen Video Player (when active and not minimized)
        AnimatedVisibility(
            visible = playbackState.isPlayerVisible && !playbackState.isMiniPlayer,
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it }),
            modifier = Modifier.fillMaxSize()
        ) {
            VideoPlayerView(
                viewModel = viewModel,
                onAddToPlaylist = { video -> videoToAddToPlaylist = video }
            )
        }
    }

    // Dialogs
    if (showThemeDialog) {
        ThemeDialog(
            currentMode = currentTheme,
            onSelectMode = { viewModel.setThemeMode(it) },
            onDismiss = { showThemeDialog = false }
        )
    }

    if (showCastDialog) {
        CastDialog(
            videoTitle = playbackState.currentVideo?.title ?: "Video Player",
            onDismiss = { showCastDialog = false }
        )
    }

    if (showCreatePlaylistDialog) {
        CreatePlaylistDialog(
            onConfirm = { name ->
                viewModel.createPlaylist(name)
                showCreatePlaylistDialog = false
            },
            onDismiss = { showCreatePlaylistDialog = false }
        )
    }

    videoToAddToPlaylist?.let { video ->
        AddToPlaylistDialog(
            video = video,
            playlists = playlists,
            onSelectPlaylist = { playlistId ->
                viewModel.addVideoToPlaylist(playlistId, video)
                videoToAddToPlaylist = null
            },
            onCreateNewPlaylist = {
                showCreatePlaylistDialog = true
            },
            onDismiss = { videoToAddToPlaylist = null }
        )
    }
}

@Composable
fun EmptyVideosState(onOpenFile: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.size(80.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.VideoLibrary,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(40.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(18.dp))
        Text(
            text = "No Videos Found",
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "You can open any video file directly from your storage to play with full features.",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            lineHeight = 20.sp
        )
        Spacer(modifier = Modifier.height(20.dp))
        Button(
            onClick = onOpenFile,
            modifier = Modifier.testTag("empty_state_open_video_btn")
        ) {
            Text("Select Video File")
        }
    }
}
