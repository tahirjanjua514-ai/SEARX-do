package com.example.data.db

import kotlinx.coroutines.flow.Flow

class DownloadRepository(private val dao: DownloadDao) {
    val allDownloads: Flow<List<DownloadEntity>> = dao.getAllDownloads()
    val activeDownloads: Flow<List<DownloadEntity>> = dao.getActiveDownloads()
    val queuedDownloads: Flow<List<DownloadEntity>> = dao.getQueuedDownloads()
    val completedDownloads: Flow<List<DownloadEntity>> = dao.getCompletedDownloads()

    suspend fun getDownloadById(id: Long): DownloadEntity? = dao.getDownloadById(id)

    fun observeDownload(id: Long): Flow<DownloadEntity?> = dao.observeDownloadById(id)

    suspend fun countActiveDownloading(): Int = dao.countActiveDownloading()

    suspend fun getNextQueued(limit: Int): List<DownloadEntity> = dao.getNextQueued(limit)

    suspend fun insert(entity: DownloadEntity): Long = dao.insert(entity)

    suspend fun update(entity: DownloadEntity) = dao.update(entity)

    suspend fun updateProgress(
        id: Long,
        downloadedBytes: Long,
        totalBytes: Long,
        progress: Float,
        speedBytesPerSec: Long,
        etaSeconds: Long,
        status: String
    ) = dao.updateProgress(id, downloadedBytes, totalBytes, progress, speedBytesPerSec, etaSeconds, status)

    suspend fun updateStatus(
        id: Long,
        status: String,
        filePath: String? = null,
        completedAt: Long? = null,
        errorMessage: String? = null
    ) = dao.updateStatus(id, status, filePath, completedAt, errorMessage)

    suspend fun delete(id: Long) = dao.deleteById(id)

    suspend fun clearCompleted() = dao.clearCompleted()

    suspend fun clearAll() = dao.clearAll()
}
