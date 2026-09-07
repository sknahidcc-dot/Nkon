package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cast
import androidx.compose.material.icons.filled.CastConnected
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.FrostedDialogBackground
import com.example.ui.theme.FrostedGlassBorder
import com.example.ui.theme.FrostedGlassBorderSubtle
import com.example.ui.theme.FrostedGlassWhite10
import com.example.ui.theme.FrostedGlassWhite20
import com.example.ui.theme.FrostedTextPrimary
import com.example.ui.theme.FrostedTextSecondary
import com.example.ui.theme.YouTubeRed
import kotlinx.coroutines.delay

@Composable
fun CastDialog(
    videoTitle: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var isSearching by remember { mutableStateOf(true) }
    var connectedDevice by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        delay(1200)
        isSearching = false
    }

    val devices = listOf(
        "Living Room Chromecast 4K",
        "Samsung Smart TV",
        "Android TV Box",
        "Mi TV Stick (Bedroom)"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = FrostedDialogBackground,
        modifier = Modifier.border(1.dp, FrostedGlassBorder, RoundedCornerShape(20.dp)),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (connectedDevice != null) Icons.Default.CastConnected else Icons.Default.Cast,
                    contentDescription = null,
                    tint = if (connectedDevice != null) YouTubeRed else FrostedTextPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Cast to Screen / TV",
                    fontWeight = FontWeight.Bold,
                    color = FrostedTextPrimary
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Playing: $videoTitle",
                    fontSize = 13.sp,
                    color = FrostedTextSecondary,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(14.dp))

                if (isSearching) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = YouTubeRed,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = "Scanning for wireless displays & TVs...",
                            fontSize = 13.sp,
                            color = FrostedTextSecondary
                        )
                    }
                } else {
                    Text(
                        text = "Available Devices:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = YouTubeRed
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    devices.forEach { deviceName ->
                        val isThisConnected = connectedDevice == deviceName
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    connectedDevice = if (isThisConnected) null else deviceName
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = if (isThisConnected) YouTubeRed.copy(alpha = 0.25f)
                                else FrostedGlassWhite10,
                                modifier = Modifier
                                    .size(36.dp)
                                    .border(1.dp, FrostedGlassBorderSubtle, CircleShape)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Tv,
                                        contentDescription = null,
                                        tint = if (isThisConnected) YouTubeRed else FrostedTextSecondary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = deviceName,
                                    fontSize = 14.sp,
                                    fontWeight = if (isThisConnected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isThisConnected) YouTubeRed else FrostedTextPrimary
                                )
                                Text(
                                    text = if (isThisConnected) "Connected • Ready to Stream" else "Ready to connect",
                                    fontSize = 11.sp,
                                    color = if (isThisConnected) YouTubeRed else FrostedTextSecondary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Android System Cast Settings Button
                    OutlinedButton(
                        onClick = {
                            try {
                                val intent = Intent(Settings.ACTION_CAST_SETTINGS)
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, FrostedGlassBorderSubtle, RoundedCornerShape(12.dp)),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = FrostedTextPrimary
                        )
                    ) {
                        Text("Open System Cast Settings", color = FrostedTextPrimary)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("dismiss_cast_dialog")
            ) {
                Text(if (connectedDevice != null) "Done" else "Close", color = YouTubeRed)
            }
        },
        shape = RoundedCornerShape(20.dp)
    )
}
