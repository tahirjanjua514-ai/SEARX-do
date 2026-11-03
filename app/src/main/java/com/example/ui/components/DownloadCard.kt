package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.db.DownloadEntity
import com.example.data.model.DownloadStatus
import com.example.data.model.PlatformType
import com.example.data.model.formatBytes
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.PrimaryGradient
import com.example.ui.theme.StatusError
import com.example.ui.theme.StatusInfo
import com.example.ui.theme.StatusSuccess
import com.example.ui.theme.StatusWarning
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary

@Composable
fun DownloadCard(
    entity: DownloadEntity,
    onPause: (Long) -> Unit = {},
    onResume: (Long) -> Unit = {},
    onCancel: (Long) -> Unit = {},
    onRetry: (Long) -> Unit = {},
    onDelete: (DownloadEntity) -> Unit = {},
    onOpen: (DownloadEntity) -> Unit = {},
    onShare: (DownloadEntity) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = entity.progress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label = "downloadProgress"
    )

    val platform = try {
        PlatformType.valueOf(entity.platform)
    } catch (e: Exception) {
        PlatformType.DIRECT
    }

    val statusColor = when (entity.status) {
        DownloadStatus.DOWNLOADING.name -> NeonCyan
        DownloadStatus.COMPLETED.name -> StatusSuccess
        DownloadStatus.PAUSED.name -> StatusWarning
        DownloadStatus.FAILED.name -> StatusError
        DownloadStatus.QUEUED.name -> StatusInfo
        else -> TextTertiary
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("download_card_${entity.id}")
            .border(1.dp, BorderSubtle, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            // Top row: Thumbnail + Details + Action button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Thumbnail or Type Icon
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(DarkSurfaceElevated),
                    contentAlignment = Alignment.Center
                ) {
                    if (!entity.thumbnailUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = entity.thumbnailUrl,
                            contentDescription = entity.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        Icon(
                            imageVector = if (entity.mediaType == "AUDIO") Icons.Default.Audiotrack else Icons.Default.Videocam,
                            contentDescription = entity.mediaType,
                            tint = if (entity.mediaType == "AUDIO") ElectricViolet else NeonCyan,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    // Format badge overlaid on bottom right of thumbnail
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .background(
                                color = Color.Black.copy(alpha = 0.8f),
                                shape = RoundedCornerShape(topStart = 6.dp)
                            )
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = entity.format.uppercase(),
                            color = Color.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Info: Title, Platform, Filename
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Platform Tag
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(platform.primaryColor.copy(alpha = 0.2f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = platform.displayName,
                                color = platform.primaryColor,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        // Quality / Bitrate Tag
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(DarkSurfaceVariant)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = entity.quality,
                                color = TextSecondary,
                                fontSize = 10.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = entity.title,
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = entity.fileName,
                        color = TextTertiary,
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Primary Quick Action based on state
                when (entity.status) {
                    DownloadStatus.DOWNLOADING.name -> {
                        IconButton(
                            onClick = { onPause(entity.id) },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(DarkSurfaceElevated)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Pause,
                                contentDescription = "Pause",
                                tint = NeonCyan,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    DownloadStatus.PAUSED.name -> {
                        IconButton(
                            onClick = { onResume(entity.id) },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(DarkSurfaceElevated)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Resume",
                                tint = NeonCyan,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    DownloadStatus.FAILED.name -> {
                        IconButton(
                            onClick = { onRetry(entity.id) },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(DarkSurfaceElevated)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Retry",
                                tint = StatusWarning,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    DownloadStatus.COMPLETED.name -> {
                        IconButton(
                            onClick = { onOpen(entity) },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(StatusSuccess.copy(alpha = 0.2f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Play",
                                tint = StatusSuccess,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Animated Progress Bar (for downloading, paused, queued, completed)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(DarkSurfaceVariant)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(animatedProgress)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            if (entity.status == DownloadStatus.COMPLETED.name) {
                                Brush.horizontalGradient(listOf(StatusSuccess, NeonCyan))
                            } else {
                                PrimaryGradient
                            }
                        )
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Stats row matching prompt exact requirement:
            // Progress: 45%
            // Speed: 6.8 MB/s
            // Downloaded: 225 MB / 500 MB
            // Remaining: 00:38
            if (entity.status == DownloadStatus.DOWNLOADING.name || entity.status == DownloadStatus.PAUSED.name || entity.status == DownloadStatus.QUEUED.name) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Progress: ${(entity.progress * 100).toInt()}%",
                        color = TextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    if (entity.status == DownloadStatus.DOWNLOADING.name) {
                        Text(
                            text = "Speed: ${formatSpeed(entity.speedBytesPerSec)}",
                            color = NeonCyan,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    } else {
                        Text(
                            text = "Status: ${entity.status.lowercase().replaceFirstChar { it.uppercase() }}",
                            color = statusColor,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Downloaded: ${formatBytes(entity.downloadedBytes)} / ${formatBytes(entity.totalBytes)}",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )

                    if (entity.status == DownloadStatus.DOWNLOADING.name) {
                        Text(
                            text = "Remaining: ${formatEta(entity.etaSeconds)}",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    } else {
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "Cancel",
                                color = StatusError,
                                fontSize = 11.sp,
                                modifier = Modifier
                                    .clickable { onCancel(entity.id) }
                                    .padding(2.dp)
                            )
                        }
                    }
                }
            } else if (entity.status == DownloadStatus.COMPLETED.name) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Completed: ${formatBytes(entity.downloadedBytes)}",
                        color = StatusSuccess,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IconButton(
                            onClick = { onShare(entity) },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Share",
                                tint = TextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        IconButton(
                            onClick = { onDelete(entity) },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = StatusError.copy(alpha = 0.8f),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            } else if (entity.status == DownloadStatus.FAILED.name) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Error: ${entity.errorMessage ?: "Download interrupted"}",
                        color = StatusError,
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "Retry",
                        color = NeonCyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clickable { onRetry(entity.id) }
                            .padding(4.dp)
                    )
                }
            }
        }
    }
}

private fun formatSpeed(bytesPerSec: Long): String {
    if (bytesPerSec <= 0) return "0.0 KB/s"
    val mb = bytesPerSec / (1024.0 * 1024.0)
    val kb = bytesPerSec / 1024.0
    return when {
        mb >= 1.0 -> String.format("%.1f MB/s", mb)
        kb >= 1.0 -> String.format("%.0f KB/s", kb)
        else -> "$bytesPerSec B/s"
    }
}

private fun formatEta(seconds: Long): String {
    if (seconds <= 0) return "00:00"
    val m = seconds / 60
    val s = seconds % 60
    return String.format("%02d:%02d", m, s)
}
