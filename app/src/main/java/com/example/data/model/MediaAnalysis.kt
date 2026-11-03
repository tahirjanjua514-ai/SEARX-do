package com.example.data.model

data class QualityOption(
    val resolution: String,      // "1080p", "720p", "480p", "360p", "4K", etc.
    val label: String,           // "1080p Full HD", "720p HD"
    val format: String = "MP4",  // "MP4", "WEBM"
    val estimatedBytes: Long = 0L,
    val mediaUrl: String? = null
) {
    val formattedSize: String
        get() = formatBytes(estimatedBytes)
}

data class AudioOption(
    val format: String,          // "MP3", "M4A", "WAV", "OPUS"
    val bitrate: String,         // "320 kbps", "256 kbps", "192 kbps", "128 kbps"
    val estimatedBytes: Long = 0L,
    val mediaUrl: String? = null
) {
    val formattedSize: String
        get() = formatBytes(estimatedBytes)
}

data class MediaAnalysis(
    val originalUrl: String,
    val platform: PlatformType,
    val title: String,
    val author: String? = null,
    val durationSeconds: Int? = null,
    val thumbnailUrl: String? = null,
    val videoOptions: List<QualityOption> = emptyList(),
    val audioOptions: List<AudioOption> = emptyList(),
    val directDownloadUrl: String? = null,
    val isDirectDownloadable: Boolean = true,
    val notice: String? = null
) {
    val formattedDuration: String
        get() {
            val secs = durationSeconds ?: return ""
            val m = secs / 60
            val s = secs % 60
            return String.format("%02d:%02d", m, s)
        }
}

fun formatBytes(bytes: Long): String {
    if (bytes <= 0) return "Unknown size"
    val kb = bytes / 1024.0
    val mb = kb / 1024.0
    val gb = mb / 1024.0
    return when {
        gb >= 1.0 -> String.format("%.2f GB", gb)
        mb >= 1.0 -> String.format("%.1f MB", mb)
        kb >= 1.0 -> String.format("%.0f KB", kb)
        else -> "$bytes B"
    }
}
