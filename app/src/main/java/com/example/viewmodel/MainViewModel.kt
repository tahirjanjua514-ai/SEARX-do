package com.example.viewmodel

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.analyzer.MediaAnalyzer
import com.example.data.db.DownloadEntity
import com.example.data.db.DownloadRepository
import com.example.data.downloader.DownloadEngine
import com.example.data.model.DownloadStatus
import com.example.data.model.MediaAnalysis
import com.example.data.model.PlatformType
import com.example.data.preferences.AppPreferences
import com.example.service.DownloadService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File

class MainViewModel(
    private val repository: DownloadRepository,
    private val downloadEngine: DownloadEngine,
    val preferences: AppPreferences
) : ViewModel() {

    private val _urlInput = MutableStateFlow("")
    val urlInput: StateFlow<String> = _urlInput.asStateFlow()

    private val _detectedPlatform = MutableStateFlow(PlatformType.DIRECT)
    val detectedPlatform: StateFlow<PlatformType> = _detectedPlatform.asStateFlow()

    private val _isAnalyzing = MutableStateFlow(false)
    val isAnalyzing: StateFlow<Boolean> = _isAnalyzing.asStateFlow()

    private val _analysisResult = MutableStateFlow<MediaAnalysis?>(null)
    val analysisResult: StateFlow<MediaAnalysis?> = _analysisResult.asStateFlow()

    private val _selectedMediaType = MutableStateFlow("VIDEO") // "VIDEO" or "AUDIO"
    val selectedMediaType: StateFlow<String> = _selectedMediaType.asStateFlow()

    private val _selectedResolution = MutableStateFlow("1080p")
    val selectedResolution: StateFlow<String> = _selectedResolution.asStateFlow()

    private val _selectedAudioFormat = MutableStateFlow("MP3")
    val selectedAudioFormat: StateFlow<String> = _selectedAudioFormat.asStateFlow()

    private val _selectedAudioBitrate = MutableStateFlow("320 kbps")
    val selectedAudioBitrate: StateFlow<String> = _selectedAudioBitrate.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

    val allDownloads: StateFlow<List<DownloadEntity>> = repository.allDownloads
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeDownloads: StateFlow<List<DownloadEntity>> = repository.activeDownloads
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val queuedDownloads: StateFlow<List<DownloadEntity>> = repository.queuedDownloads
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val completedDownloads: StateFlow<List<DownloadEntity>> = repository.completedDownloads
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        // Collect engine messages for toasts/snackbars
        viewModelScope.launch {
            downloadEngine.engineEvents.collect { msg ->
                _userMessage.value = msg
            }
        }
    }

    fun onUrlChange(newUrl: String) {
        _urlInput.value = newUrl
        _detectedPlatform.value = PlatformType.detect(newUrl)
        _errorMessage.value = null
    }

    fun pasteUrl(pasted: String) {
        onUrlChange(pasted)
    }

    fun clearUrl() {
        _urlInput.value = ""
        _detectedPlatform.value = PlatformType.DIRECT
        _analysisResult.value = null
        _errorMessage.value = null
    }

    fun selectPreset(preset: MediaAnalysis) {
        _urlInput.value = preset.originalUrl
        _detectedPlatform.value = preset.platform
        _analysisResult.value = preset
        _errorMessage.value = null

        if (preset.videoOptions.isNotEmpty()) {
            _selectedResolution.value = preset.videoOptions.first().resolution
        }
        if (preset.audioOptions.isNotEmpty()) {
            _selectedAudioFormat.value = preset.audioOptions.first().format
            _selectedAudioBitrate.value = preset.audioOptions.first().bitrate
        }
    }

    fun analyzeUrl() {
        val url = _urlInput.value.trim()
        if (url.isBlank()) {
            _errorMessage.value = "Please enter or paste a valid media URL"
            return
        }

        viewModelScope.launch {
            _isAnalyzing.value = true
            _errorMessage.value = null
            try {
                val analysis = MediaAnalyzer.analyzeUrl(url)
                _analysisResult.value = analysis

                if (analysis.videoOptions.isNotEmpty()) {
                    _selectedResolution.value = analysis.videoOptions.first().resolution
                }
                if (analysis.audioOptions.isNotEmpty()) {
                    _selectedAudioFormat.value = analysis.audioOptions.first().format
                    _selectedAudioBitrate.value = analysis.audioOptions.first().bitrate
                }
            } catch (e: Exception) {
                _errorMessage.value = "Analysis error: ${e.message ?: "Could not resolve URL"}"
            } finally {
                _isAnalyzing.value = false
            }
        }
    }

    fun setMediaType(type: String) {
        _selectedMediaType.value = type
    }

    fun setResolution(res: String) {
        _selectedResolution.value = res
    }

    fun setAudioFormat(format: String) {
        _selectedAudioFormat.value = format
    }

    fun setAudioBitrate(bitrate: String) {
        _selectedAudioBitrate.value = bitrate
    }

    fun startDownload(context: Context) {
        val analysis = _analysisResult.value
        val fallbackUrl = _urlInput.value.trim()

        if (analysis == null && fallbackUrl.isBlank()) {
            _errorMessage.value = "Analyze a link or enter a URL first"
            return
        }

        val mediaType = _selectedMediaType.value
        val title = analysis?.title ?: "Media Download"
        val platform = analysis?.platform?.name ?: PlatformType.detect(fallbackUrl).name
        val thumb = analysis?.thumbnailUrl

        val (downloadUrl, format, quality, estBytes) = if (mediaType == "VIDEO") {
            val opt = analysis?.videoOptions?.find { it.resolution == _selectedResolution.value }
                ?: analysis?.videoOptions?.firstOrNull()
            val url = opt?.mediaUrl ?: analysis?.directDownloadUrl ?: fallbackUrl
            val fmt = opt?.format ?: "MP4"
            val q = opt?.resolution ?: _selectedResolution.value
            val bytes = opt?.estimatedBytes ?: 0L
            listOf(url, fmt, q, bytes)
        } else {
            val opt = analysis?.audioOptions?.find { it.format == _selectedAudioFormat.value }
                ?: analysis?.audioOptions?.firstOrNull()
            val url = opt?.mediaUrl ?: analysis?.directDownloadUrl ?: fallbackUrl
            val fmt = opt?.format ?: _selectedAudioFormat.value
            val q = opt?.bitrate ?: _selectedAudioBitrate.value
            val bytes = opt?.estimatedBytes ?: 0L
            listOf(url, fmt, q, bytes)
        }

        downloadEngine.enqueueDownload(
            originalUrl = analysis?.originalUrl ?: fallbackUrl,
            downloadUrl = downloadUrl as String,
            title = title,
            platform = platform,
            mediaType = mediaType,
            quality = quality as String,
            format = format as String,
            thumbnailUrl = thumb,
            totalEstimatedBytes = estBytes as Long
        )

        // Start Foreground Service to maintain download in background
        DownloadService.start(context)

        _userMessage.value = "Added to download queue: $title"
    }

    fun pauseDownload(id: Long) = downloadEngine.pauseDownload(id)
    fun resumeDownload(id: Long) = downloadEngine.resumeDownload(id)
    fun cancelDownload(id: Long) = downloadEngine.cancelDownload(id)
    fun retryDownload(id: Long) = downloadEngine.retryDownload(id)

    fun pauseAll() = downloadEngine.pauseAll()
    fun resumeAll() = downloadEngine.resumeAll()
    fun cancelAll() = downloadEngine.cancelAll()

    fun deleteDownload(entity: DownloadEntity, deleteFileFromDisk: Boolean) {
        viewModelScope.launch {
            if (deleteFileFromDisk && entity.filePath != null) {
                try {
                    File(entity.filePath).delete()
                } catch (ignored: Exception) {}
            }
            repository.delete(entity.id)
            _userMessage.value = "Deleted: ${entity.title}"
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            repository.clearCompleted()
            _userMessage.value = "Download history cleared"
        }
    }

    fun dismissUserMessage() {
        _userMessage.value = null
    }

    fun openFile(context: Context, entity: DownloadEntity) {
        val path = entity.filePath
        if (path == null) {
            Toast.makeText(context, "File path is unavailable", Toast.LENGTH_SHORT).show()
            return
        }

        val file = File(path)
        if (!file.exists()) {
            Toast.makeText(context, "File not found on storage", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val authority = "${context.packageName}.fileprovider"
            val uri = FileProvider.getUriForFile(context, authority, file)

            val mimeType = when (entity.format.uppercase()) {
                "MP4", "WEBM", "MKV" -> "video/*"
                "MP3", "M4A", "WAV", "OPUS", "AAC" -> "audio/*"
                else -> "*/*"
            }

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, mimeType)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "No app available to open this file", Toast.LENGTH_SHORT).show()
        }
    }

    fun shareFile(context: Context, entity: DownloadEntity) {
        val path = entity.filePath ?: return
        val file = File(path)
        if (!file.exists()) {
            Toast.makeText(context, "File does not exist", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val authority = "${context.packageName}.fileprovider"
            val uri = FileProvider.getUriForFile(context, authority, file)

            val mimeType = if (entity.mediaType == "AUDIO") "audio/*" else "video/*"
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, entity.title)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(shareIntent, "Share via"))
        } catch (e: Exception) {
            Toast.makeText(context, "Failed to share: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
