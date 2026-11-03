package com.example.ui.screens

import android.os.Environment
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.theme.BorderGlow
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.PrimaryGradient
import com.example.ui.theme.StatusError
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import com.example.viewmodel.MainViewModel

@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val prefs = viewModel.preferences

    val maxConcurrent by prefs.maxConcurrentDownloads.collectAsStateWithLifecycle()
    val defaultVideoQuality by prefs.defaultVideoQuality.collectAsStateWithLifecycle()
    val defaultAudioFormat by prefs.defaultAudioFormat.collectAsStateWithLifecycle()
    val defaultAudioQuality by prefs.defaultAudioQuality.collectAsStateWithLifecycle()
    val wifiOnly by prefs.wifiOnly.collectAsStateWithLifecycle()
    val notificationsEnabled by prefs.notificationsEnabled.collectAsStateWithLifecycle()
    val autoRetry by prefs.autoRetry.collectAsStateWithLifecycle()

    var showClearHistoryDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }

    val downloadDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)?.absolutePath
        ?: context.filesDir.absolutePath

    if (showClearHistoryDialog) {
        AlertDialog(
            onDismissRequest = { showClearHistoryDialog = false },
            title = { Text("Clear All Download Records?", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = { Text("This will reset your download history list.", color = TextSecondary) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearHistory()
                        showClearHistoryDialog = false
                    }
                ) {
                    Text("Clear", color = StatusError, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearHistoryDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = DarkSurface,
            shape = RoundedCornerShape(16.dp)
        )
    }

    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("SEARX AI", color = TextPrimary, fontWeight = FontWeight.Bold)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(PrimaryGradient)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text("v1.0.0", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Black)
                    }
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "SEARX AI is an All-in-One High-Performance Media Downloader developed for Android.",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                    Text(
                        text = "• Native Foreground Service for uninterrupted background downloads.\n• High-throughput chunk streaming with HTTP Range pause/resume.\n• Local SQLite Room Database.\n• Native audio track extraction.\n• Strict zero-DRM bypass & copyright compliance.",
                        color = TextTertiary,
                        fontSize = 12.sp
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showAboutDialog = false }) {
                    Text("Close", color = NeonCyan)
                }
            },
            containerColor = DarkSurface,
            shape = RoundedCornerShape(16.dp)
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Preferences & Settings",
                color = TextPrimary,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Customize downloader performance and storage",
                color = TextSecondary,
                fontSize = 12.sp
            )
        }

        // Section: Engine & Performance
        item {
            SettingSectionHeader("ENGINE & PERFORMANCE")
        }

        item {
            SettingsCard {
                // Max Concurrent Slider
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(Icons.Default.Tune, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(20.dp))
                            Text("Simultaneous Downloads", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        }
                        Text("$maxConcurrent Active", color = NeonCyan, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Slider(
                        value = maxConcurrent.toFloat(),
                        onValueChange = { prefs.setMaxConcurrentDownloads(it.toInt()) },
                        valueRange = 1f..10f,
                        steps = 8,
                        colors = SliderDefaults.colors(
                            thumbColor = NeonCyan,
                            activeTrackColor = NeonCyan,
                            inactiveTrackColor = DarkSurfaceElevated
                        ),
                        modifier = Modifier.testTag("settings_concurrent_slider")
                    )

                    Text(
                        text = "Configures parallel download threads (1-10, default 3).",
                        color = TextTertiary,
                        fontSize = 11.sp
                    )
                }

                // Wi-Fi Only Switch
                SettingsSwitchRow(
                    title = "Download over Wi-Fi Only",
                    subtitle = "Prevents downloading on cellular data networks",
                    icon = Icons.Default.Wifi,
                    checked = wifiOnly,
                    onCheckedChange = { prefs.setWifiOnly(it) }
                )

                // Auto Retry Switch
                SettingsSwitchRow(
                    title = "Automatic Retry on Failure",
                    subtitle = "Automatically re-queues transient connection drops",
                    icon = Icons.Default.Replay,
                    checked = autoRetry,
                    onCheckedChange = { prefs.setAutoRetry(it) }
                )
            }
        }

        // Section: Media Defaults
        item {
            SettingSectionHeader("DEFAULT FORMATS & QUALITY")
        }

        item {
            SettingsCard {
                // Default Video Quality
                SettingsPickerRow(
                    title = "Default Video Resolution",
                    currentValue = defaultVideoQuality,
                    options = listOf("Best Available", "1080p Full HD", "720p HD", "480p SD", "360p"),
                    icon = Icons.Default.Videocam,
                    onSelect = { prefs.setDefaultVideoQuality(it) }
                )

                // Default Audio Format
                SettingsPickerRow(
                    title = "Default Audio Format",
                    currentValue = defaultAudioFormat,
                    options = listOf("MP3", "M4A", "WAV", "OPUS"),
                    icon = Icons.Default.Audiotrack,
                    onSelect = { prefs.setDefaultAudioFormat(it) }
                )

                // Default Audio Quality
                SettingsPickerRow(
                    title = "Default Audio Bitrate",
                    currentValue = defaultAudioQuality,
                    options = listOf("320 kbps", "256 kbps", "192 kbps", "128 kbps"),
                    icon = Icons.Default.Audiotrack,
                    onSelect = { prefs.setDefaultAudioQuality(it) }
                )
            }
        }

        // Section: Storage & System
        item {
            SettingSectionHeader("STORAGE & NOTIFICATIONS")
        }

        item {
            SettingsCard {
                // Download Directory
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(Icons.Default.Folder, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(20.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Download Storage Location", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        Text(downloadDir, color = TextTertiary, fontSize = 11.sp, maxLines = 1)
                    }
                }

                // Notifications Toggle
                SettingsSwitchRow(
                    title = "Foreground Notifications",
                    subtitle = "Shows real-time speed & progress in status bar",
                    icon = Icons.Default.Notifications,
                    checked = notificationsEnabled,
                    onCheckedChange = { prefs.setNotificationsEnabled(it) }
                )

                // Clear History Option
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showClearHistoryDialog = true }
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(Icons.Default.DeleteForever, contentDescription = null, tint = StatusError, modifier = Modifier.size(20.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Clear Download Records", color = StatusError, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        Text("Removes history logs from local database", color = TextTertiary, fontSize = 11.sp)
                    }
                }
            }
        }

        // Section: About & Terms
        item {
            SettingSectionHeader("ABOUT & LEGAL COMPLIANCE")
        }

        item {
            SettingsCard {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showAboutDialog = true }
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = ElectricViolet, modifier = Modifier.size(20.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("About SEARX AI Downloader", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        Text("Version 1.0.0 (Build 1) • Legal Terms & Architecture", color = TextTertiary, fontSize = 11.sp)
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(Icons.Default.Security, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(20.dp))
                    Text(
                        text = "SEARX AI respects content creators and copyright terms. It supports authorized and public media downloading.",
                        color = TextTertiary,
                        fontSize = 11.sp
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SettingSectionHeader(title: String) {
    Text(
        text = title,
        color = NeonCyan,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.sp,
        modifier = Modifier.padding(start = 4.dp, top = 4.dp)
    )
}

@Composable
private fun SettingsCard(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, BorderSubtle, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column {
            content()
        }
    }
}

@Composable
private fun SettingsSwitchRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.weight(1f)
        ) {
            Icon(icon, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(20.dp))
            Column {
                Text(title, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                Text(subtitle, color = TextTertiary, fontSize = 11.sp)
            }
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.Black,
                checkedTrackColor = NeonCyan,
                uncheckedThumbColor = TextTertiary,
                uncheckedTrackColor = DarkSurfaceElevated
            )
        )
    }
}

@Composable
private fun SettingsPickerRow(
    title: String,
    currentValue: String,
    options: List<String>,
    icon: ImageVector,
    onSelect: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = true }
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.weight(1f)
        ) {
            Icon(icon, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(20.dp))
            Text(title, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        }

        Box {
            Text(currentValue, color = NeonCyan, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(DarkSurfaceElevated)
            ) {
                options.forEach { opt ->
                    DropdownMenuItem(
                        text = { Text(opt, color = TextPrimary) },
                        onClick = {
                            onSelect(opt)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}
