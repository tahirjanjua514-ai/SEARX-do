package com.example.data.downloader

import android.content.Context
import android.os.Environment
import android.util.Log
import com.example.data.db.DownloadEntity
import com.example.data.db.DownloadRepository
import com.example.data.model.DownloadStatus
import com.example.data.preferences.AppPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.io.RandomAccessFile
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

class DownloadEngine(
    private val context: Context,
    private val repository: DownloadRepository,
    private val preferences: AppPreferences
) {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val activeJobs = ConcurrentHashMap<Long, Job>()
    private val pausedIds = ConcurrentHashMap.newKeySet<Long>()

    private val _engineEvents = MutableSharedFlow<String>(extraBufferCapacity = 64)
    val engineEvents = _engineEvents.asSharedFlow()

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .followRedirects(true)
        .build()

    companion object {
        private const val TAG = "DownloadEngine"
        private const val BUFFER_SIZE = 32 * 1024 // 32 KB buffer for optimal throughput
    }

    fun startQueueProcessor() {
        scope.launch {
            while (isActive) {
                processNextQueueItems()
                delay(1500)
            }
        }
    }

    private suspend fun processNextQueueItems() {
        val maxConcurrent = preferences.maxConcurrentDownloads.value
        val activeCount = activeJobs.size
        val availableSlots = maxConcurrent - activeCount

        if (availableSlots > 0) {
            val nextList = repository.getNextQueued(availableSlots)
            for (item in nextList) {
                if (!activeJobs.containsKey(item.id) && !pausedIds.contains(item.id)) {
                    startDownloadInternal(item.id)
                }
            }
        }
    }

    fun enqueueDownload(
        originalUrl: String,
        downloadUrl: String,
        title: String,
        platform: String,
        mediaType: String,
        quality: String,
        format: String,
        thumbnailUrl: String?,
        totalEstimatedBytes: Long
    ) {
        scope.launch {
            val extension = format.lowercase()
            val sanitizedTitle = title.replace(Regex("[^a-zA-Z0-9_\\-\\s]"), "").trim().take(40)
            val fileName = "${sanitizedTitle}_${System.currentTimeMillis()}.$extension"

            val entity = DownloadEntity(
                originalUrl = originalUrl,
                downloadUrl = downloadUrl,
                title = title,
                platform = platform,
                mediaType = mediaType,
                quality = quality,
                format = format,
                thumbnailUrl = thumbnailUrl,
                fileName = fileName,
                totalBytes = totalEstimatedBytes,
                downloadedBytes = 0L,
                status = DownloadStatus.QUEUED.name,
                progress = 0f,
                createdAt = System.currentTimeMillis()
            )

            val id = repository.insert(entity)
            _engineEvents.tryEmit("Enqueued: $title")
            processNextQueueItems()
        }
    }

    fun resumeDownload(id: Long) {
        pausedIds.remove(id)
        scope.launch {
            val item = repository.getDownloadById(id) ?: return@launch
            if (item.status == DownloadStatus.PAUSED.name || item.status == DownloadStatus.FAILED.name) {
                repository.updateStatus(id, DownloadStatus.QUEUED.name)
                processNextQueueItems()
            }
        }
    }

    fun pauseDownload(id: Long) {
        pausedIds.add(id)
        val job = activeJobs.remove(id)
        job?.cancel()
        scope.launch {
            repository.updateStatus(id, DownloadStatus.PAUSED.name)
            processNextQueueItems()
        }
    }

    fun cancelDownload(id: Long) {
        pausedIds.remove(id)
        val job = activeJobs.remove(id)
        job?.cancel()
        scope.launch {
            val item = repository.getDownloadById(id)
            if (item?.filePath != null) {
                try {
                    File(item.filePath).delete()
                } catch (ignored: Exception) {}
            }
            repository.updateStatus(id, DownloadStatus.CANCELLED.name)
            processNextQueueItems()
        }
    }

    fun retryDownload(id: Long) {
        pausedIds.remove(id)
        scope.launch {
            repository.updateStatus(id, DownloadStatus.QUEUED.name)
            processNextQueueItems()
        }
    }

    fun pauseAll() {
        val runningIds = activeJobs.keys.toList()
        for (id in runningIds) {
            pauseDownload(id)
        }
    }

    fun resumeAll() {
        scope.launch {
            val paused = repository.getNextQueued(100)
            for (item in paused) {
                resumeDownload(item.id)
            }
        }
    }

    fun cancelAll() {
        val runningIds = activeJobs.keys.toList()
        for (id in runningIds) {
            cancelDownload(id)
        }
    }

    private fun getStorageDir(): File {
        val dir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
            ?: File(context.filesDir, "downloads")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    private fun startDownloadInternal(id: Long) {
        if (activeJobs.containsKey(id)) return

        val job = scope.launch {
            var targetFile: File? = null
            try {
                val item = repository.getDownloadById(id) ?: return@launch
                repository.updateStatus(id, DownloadStatus.DOWNLOADING.name)

                val storageDir = getStorageDir()
                targetFile = File(storageDir, item.fileName)
                val existingBytes = if (targetFile.exists()) targetFile.length() else 0L

                // Build OkHttp request with Range support for resuming
                val requestBuilder = Request.Builder()
                    .url(item.downloadUrl)
                    .header("User-Agent", "SEARX-AI-Downloader/1.0 (Android)")

                if (existingBytes > 0) {
                    requestBuilder.header("Range", "bytes=$existingBytes-")
                }

                val response = httpClient.newCall(requestBuilder.build()).execute()
                val responseBody = response.body
                if (!response.isSuccessful || responseBody == null) {
                    throw Exception("HTTP ${response.code}: ${response.message}")
                }

                val isPartial = response.code == 206
                val startingBytes = if (isPartial) existingBytes else 0L
                val remoteContentLength = responseBody.contentLength()
                val totalBytes = if (remoteContentLength > 0) {
                    startingBytes + remoteContentLength
                } else if (item.totalBytes > 0) {
                    item.totalBytes
                } else {
                    -1L
                }

                val raf = RandomAccessFile(targetFile, "rw")
                if (isPartial) {
                    raf.seek(startingBytes)
                } else {
                    raf.setLength(0)
                }

                val inputStream = responseBody.byteStream()
                val buffer = ByteArray(BUFFER_SIZE)
                var downloaded = startingBytes
                var bytesRead: Int

                var lastSpeedCalcTime = System.currentTimeMillis()
                var bytesSinceLastCalc = 0L
                var currentSpeed = 0L
                var lastDbUpdateTime = 0L

                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                    if (!isActive || pausedIds.contains(id)) {
                        break
                    }

                    raf.write(buffer, 0, bytesRead)
                    downloaded += bytesRead
                    bytesSinceLastCalc += bytesRead

                    val now = System.currentTimeMillis()
                    val timeDiff = now - lastSpeedCalcTime
                    if (timeDiff >= 500) {
                        currentSpeed = (bytesSinceLastCalc * 1000L) / timeDiff
                        bytesSinceLastCalc = 0L
                        lastSpeedCalcTime = now
                    }

                    // Update Room Database at reasonable throttle (every ~400ms)
                    if (now - lastDbUpdateTime >= 400 || (totalBytes > 0 && downloaded >= totalBytes)) {
                        lastDbUpdateTime = now
                        val progress = if (totalBytes > 0) (downloaded.toFloat() / totalBytes.toFloat()).coerceIn(0f, 1f) else 0.5f
                        val remainingBytes = if (totalBytes > 0) (totalBytes - downloaded).coerceAtLeast(0) else 0L
                        val etaSeconds = if (currentSpeed > 0 && remainingBytes > 0) remainingBytes / currentSpeed else 0L

                        repository.updateProgress(
                            id = id,
                            downloadedBytes = downloaded,
                            totalBytes = if (totalBytes > 0) totalBytes else downloaded,
                            progress = progress,
                            speedBytesPerSec = currentSpeed,
                            etaSeconds = etaSeconds,
                            status = DownloadStatus.DOWNLOADING.name
                        )
                    }
                }

                raf.close()
                inputStream.close()
                response.close()

                if (pausedIds.contains(id)) {
                    repository.updateStatus(id, DownloadStatus.PAUSED.name)
                    return@launch
                }

                // Verify file exists and has size
                if (targetFile.exists() && targetFile.length() > 0) {
                    var finalFile = targetFile

                    // If user requested audio extraction and source was video
                    if (item.mediaType == "AUDIO" && !item.fileName.endsWith(".${item.format.lowercase()}")) {
                        val extractedAudioFile = File(storageDir, "${item.title.take(30)}_${System.currentTimeMillis()}.${item.format.lowercase()}")
                        val success = AudioUtils.extractAudioTrack(targetFile, extractedAudioFile)
                        if (success) {
                            finalFile = extractedAudioFile
                        }
                    }

                    repository.updateStatus(
                        id = id,
                        status = DownloadStatus.COMPLETED.name,
                        filePath = finalFile.absolutePath,
                        completedAt = System.currentTimeMillis()
                    )
                    _engineEvents.tryEmit("Completed: ${item.title}")
                } else {
                    throw Exception("Downloaded file is empty or missing")
                }
            } catch (e: Exception) {
                if (pausedIds.contains(id)) {
                    repository.updateStatus(id, DownloadStatus.PAUSED.name)
                } else {
                    Log.e(TAG, "Download failed for id $id", e)
                    repository.updateStatus(
                        id = id,
                        status = DownloadStatus.FAILED.name,
                        errorMessage = e.message ?: "Download failed"
                    )
                }
            } finally {
                activeJobs.remove(id)
                processNextQueueItems()
            }
        }

        activeJobs[id] = job
    }
}
