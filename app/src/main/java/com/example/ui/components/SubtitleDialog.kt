package com.example.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ClosedCaption
import androidx.compose.material.icons.filled.ClosedCaptionOff
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.FrostedDialogBackground
import com.example.ui.theme.FrostedGlassBorder
import com.example.ui.theme.FrostedGlassBorderSubtle
import com.example.ui.theme.FrostedGlassWhite10
import com.example.ui.theme.FrostedTextPrimary
import com.example.ui.theme.FrostedTextSecondary
import com.example.ui.theme.YouTubeRed

@Composable
fun SubtitleDialog(
    subtitlesEnabled: Boolean,
    currentFileName: String?,
    onToggleSubtitles: () -> Unit,
    onSubtitleFileSelected: (Uri, String) -> Unit,
    onDismiss: () -> Unit
) {
    val subtitlePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            val fileName = uri.lastPathSegment ?: "subtitle.srt"
            onSubtitleFileSelected(uri, fileName)
            onDismiss()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = FrostedDialogBackground,
        modifier = Modifier.border(1.dp, FrostedGlassBorder, RoundedCornerShape(20.dp)),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.ClosedCaption,
                    contentDescription = null,
                    tint = YouTubeRed
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Subtitles / Closed Captions",
                    fontWeight = FontWeight.Bold,
                    color = FrostedTextPrimary
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Toggle subtitles
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Show Subtitles",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            color = FrostedTextPrimary
                        )
                        Text(
                            text = if (subtitlesEnabled) "Captions are active" else "Captions are hidden",
                            fontSize = 12.sp,
                            color = FrostedTextSecondary
                        )
                    }
                    Switch(
                        checked = subtitlesEnabled,
                        onCheckedChange = { onToggleSubtitles() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = YouTubeRed,
                            checkedTrackColor = YouTubeRed.copy(alpha = 0.5f),
                            uncheckedThumbColor = FrostedTextSecondary,
                            uncheckedTrackColor = FrostedGlassWhite10
                        )
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Current file status
                if (currentFileName != null) {
                    Text(
                        text = "Active file: $currentFileName",
                        fontSize = 13.sp,
                        color = YouTubeRed,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                }

                // Choose local subtitle file button
                FilledTonalButton(
                    onClick = {
                        // Open file picker for subtitle formats
                        subtitlePickerLauncher.launch(arrayOf("*/*"))
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, FrostedGlassBorderSubtle, RoundedCornerShape(12.dp))
                        .testTag("load_subtitle_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = FrostedGlassWhite10,
                        contentColor = FrostedTextPrimary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.FileUpload,
                        contentDescription = "Upload",
                        tint = FrostedTextPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Select Local Subtitle (.srt, .vtt)", color = FrostedTextPrimary)
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("dismiss_subtitle_dialog")
            ) {
                Text("Done", color = YouTubeRed)
            }
        },
        shape = RoundedCornerShape(20.dp)
    )
}
