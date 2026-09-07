package com.example

import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.media3.ui.PlayerView
import com.example.data.model.VideoItem
import com.example.data.repository.VideoRepository
import com.example.player.VideoPlayerViewModel
import com.example.ui.screens.HomeScreen
import com.example.ui.theme.VideoPlayerTheme

class MainActivity : ComponentActivity() {

    private lateinit var viewModel: VideoPlayerViewModel
    private var isPipMode by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val repository = VideoRepository(applicationContext)
        viewModel = ViewModelProvider(this, object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return VideoPlayerViewModel(applicationContext, repository) as T
            }
        })[VideoPlayerViewModel::class.java]

        handleIncomingIntent(intent)

        setContent {
            val themeMode by viewModel.currentThemeMode.collectAsState()
            val playbackState by viewModel.playbackState.collectAsState()

            VideoPlayerTheme(themeMode = themeMode) {
                if (isPipMode && playbackState.currentVideo != null) {
                    // Minimalist video view in Picture-in-Picture mode
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black)
                    ) {
                        AndroidView(
                            factory = { ctx ->
                                PlayerView(ctx).apply {
                                    player = viewModel.player
                                    useController = false
                                }
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                } else {
                    HomeScreen(viewModel = viewModel)
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIncomingIntent(intent)
    }

    private fun handleIncomingIntent(intent: Intent?) {
        val uri = intent?.data ?: return
        if (intent.action == Intent.ACTION_VIEW) {
            val title = uri.lastPathSegment ?: "Incoming Video"
            val video = VideoItem(
                id = System.currentTimeMillis(),
                uri = uri,
                title = title,
                durationMs = 0L,
                folderName = "External"
            )
            viewModel.playVideo(video)
        }
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        val state = viewModel.playbackState.value
        if (state.isPlaying || (state.isPlayerVisible && state.currentVideo != null)) {
            viewModel.enterPictureInPicture(this)
        }
    }

    override fun onPictureInPictureModeChanged(
        isInPictureInPictureMode: Boolean,
        newConfig: Configuration
    ) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
        isPipMode = isInPictureInPictureMode
    }

    override fun onStop() {
        super.onStop()
        if (!isPipMode) {
            viewModel.onAppBackgrounded()
        }
    }
}
