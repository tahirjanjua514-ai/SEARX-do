package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "downloads")
data class DownloadEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val originalUrl: String,
    val downloadUrl: String,
    val title: String,
    val platform: String,
    val mediaType: String, // "VIDEO" or "AUDIO"
    val quality: String,
    val format: String,
    val thumbnailUrl: String? = null,
    val filePath: String? = null,
    val fileName: String,
    val totalBytes: Long = 0L,
    val downloadedBytes: Long = 0L,
    val status: String, // QUEUED, DOWNLOADING, PAUSED, COMPLETED, FAILED, CANCELLED
    val speedBytesPerSec: Long = 0L,
    val etaSeconds: Long = 0L,
    val progress: Float = 0f,
    val errorMessage: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null
)
