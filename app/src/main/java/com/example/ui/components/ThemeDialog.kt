package com.example.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.player.AppThemeMode
import com.example.ui.theme.FrostedDialogBackground
import com.example.ui.theme.FrostedGlassBorder
import com.example.ui.theme.FrostedTextPrimary
import com.example.ui.theme.FrostedTextSecondary
import com.example.ui.theme.YouTubeRed

data class ThemeOption(
    val mode: AppThemeMode,
    val title: String,
    val subtitle: String,
    val icon: ImageVector
)

@Composable
fun ThemeDialog(
    currentMode: AppThemeMode,
    onSelectMode: (AppThemeMode) -> Unit,
    onDismiss: () -> Unit
) {
    val options = listOf(
        ThemeOption(
            mode = AppThemeMode.FROSTED_GLASS,
            title = "Frosted Glass (Default)",
            subtitle = "Translucent blurred surfaces, border glows & high contrast",
            icon = Icons.Default.AutoAwesome
        ),
        ThemeOption(
            mode = AppThemeMode.DARK,
            title = "YouTube Dark",
            subtitle = "Sleek deep gray YouTube aesthetic, easy on eyes",
            icon = Icons.Default.DarkMode
        ),
        ThemeOption(
            mode = AppThemeMode.AMOLED_BLACK,
            title = "AMOLED Pure Black",
            subtitle = "Pure black #000000 background for OLED screens",
            icon = Icons.Default.Palette
        ),
        ThemeOption(
            mode = AppThemeMode.SYSTEM,
            title = "Automatic (System)",
            subtitle = "Switch between Light and Dark based on Android settings",
            icon = Icons.Default.BrightnessAuto
        ),
        ThemeOption(
            mode = AppThemeMode.LIGHT,
            title = "Light Theme",
            subtitle = "Crisp white daytime appearance",
            icon = Icons.Default.LightMode
        )
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = FrostedDialogBackground,
        modifier = Modifier.border(1.dp, FrostedGlassBorder, RoundedCornerShape(20.dp)),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Palette,
                    contentDescription = null,
                    tint = YouTubeRed
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Appearance & Theme", fontWeight = FontWeight.Bold, color = FrostedTextPrimary)
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                options.forEach { opt ->
                    val isSelected = currentMode == opt.mode
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onSelectMode(opt.mode)
                                onDismiss()
                            }
                            .padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = isSelected,
                            onClick = {
                                onSelectMode(opt.mode)
                                onDismiss()
                            },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = YouTubeRed
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = opt.icon,
                            contentDescription = null,
                            tint = if (isSelected) YouTubeRed else FrostedTextSecondary
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = opt.title,
                                fontSize = 15.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) YouTubeRed else FrostedTextPrimary
                            )
                            Text(
                                text = opt.subtitle,
                                fontSize = 12.sp,
                                color = FrostedTextSecondary
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss, modifier = Modifier.testTag("dismiss_theme_dialog")) {
                Text("Done", color = YouTubeRed)
            }
        },
        shape = RoundedCornerShape(20.dp)
    )
}
