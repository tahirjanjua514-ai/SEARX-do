package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface DownloadDao {
    @Query("SELECT * FROM downloads ORDER BY createdAt DESC")
    fun getAllDownloads(): Flow<List<DownloadEntity>>

    @Query("SELECT * FROM downloads WHERE status IN ('DOWNLOADING', 'QUEUED', 'PAUSED') ORDER BY createdAt ASC")
    fun getActiveDownloads(): Flow<List<DownloadEntity>>

    @Query("SELECT * FROM downloads WHERE status = 'QUEUED' ORDER BY createdAt ASC")
    fun getQueuedDownloads(): Flow<List<DownloadEntity>>

    @Query("SELECT * FROM downloads WHERE status = 'COMPLETED' ORDER BY completedAt DESC")
    fun getCompletedDownloads(): Flow<List<DownloadEntity>>

    @Query("SELECT * FROM downloads WHERE id = :id LIMIT 1")
    suspend fun getDownloadById(id: Long): DownloadEntity?

    @Query("SELECT * FROM downloads WHERE id = :id LIMIT 1")
    fun observeDownloadById(id: Long): Flow<DownloadEntity?>

    @Query("SELECT COUNT(*) FROM downloads WHERE status = 'DOWNLOADING'")
    suspend fun countActiveDownloading(): Int

    @Query("SELECT * FROM downloads WHERE status = 'QUEUED' ORDER BY createdAt ASC LIMIT :limit")
    suspend fun getNextQueued(limit: Int): List<DownloadEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: DownloadEntity): Long

    @Update
    suspend fun update(entity: DownloadEntity)

    @Query("""
        UPDATE downloads 
        SET downloadedBytes = :downloadedBytes,
            totalBytes = :totalBytes,
            progress = :progress,
            speedBytesPerSec = :speedBytesPerSec,
            etaSeconds = :etaSeconds,
            status = :status
        WHERE id = :id
    """)
    suspend fun updateProgress(
        id: Long,
        downloadedBytes: Long,
        totalBytes: Long,
        progress: Float,
        speedBytesPerSec: Long,
        etaSeconds: Long,
        status: String
    )

    @Query("""
        UPDATE downloads 
        SET status = :status,
            filePath = :filePath,
            completedAt = :completedAt,
            errorMessage = :errorMessage,
            speedBytesPerSec = 0,
            etaSeconds = 0
        WHERE id = :id
    """)
    suspend fun updateStatus(
        id: Long,
        status: String,
        filePath: String? = null,
        completedAt: Long? = null,
        errorMessage: String? = null
    )

    @Query("DELETE FROM downloads WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM downloads WHERE status = 'COMPLETED'")
    suspend fun clearCompleted()

    @Query("DELETE FROM downloads")
    suspend fun clearAll()
}
