package com.example.data.preferences

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AppPreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("searx_ai_settings", Context.MODE_PRIVATE)

    private val _maxConcurrentDownloads = MutableStateFlow(
        prefs.getInt(KEY_MAX_CONCURRENT, 3).coerceIn(1, 10)
    )
    val maxConcurrentDownloads: StateFlow<Int> = _maxConcurrentDownloads.asStateFlow()

    private val _defaultVideoQuality = MutableStateFlow(
        prefs.getString(KEY_DEFAULT_VIDEO_QUALITY, "1080p Full HD") ?: "1080p Full HD"
    )
    val defaultVideoQuality: StateFlow<String> = _defaultVideoQuality.asStateFlow()

    private val _defaultAudioFormat = MutableStateFlow(
        prefs.getString(KEY_DEFAULT_AUDIO_FORMAT, "MP3") ?: "MP3"
    )
    val defaultAudioFormat: StateFlow<String> = _defaultAudioFormat.asStateFlow()

    private val _defaultAudioQuality = MutableStateFlow(
        prefs.getString(KEY_DEFAULT_AUDIO_QUALITY, "320 kbps") ?: "320 kbps"
    )
    val defaultAudioQuality: StateFlow<String> = _defaultAudioQuality.asStateFlow()

    private val _wifiOnly = MutableStateFlow(
        prefs.getBoolean(KEY_WIFI_ONLY, false)
    )
    val wifiOnly: StateFlow<Boolean> = _wifiOnly.asStateFlow()

    private val _notificationsEnabled = MutableStateFlow(
        prefs.getBoolean(KEY_NOTIFICATIONS_ENABLED, true)
    )
    val notificationsEnabled: StateFlow<Boolean> = _notificationsEnabled.asStateFlow()

    private val _autoRetry = MutableStateFlow(
        prefs.getBoolean(KEY_AUTO_RETRY, true)
    )
    val autoRetry: StateFlow<Boolean> = _autoRetry.asStateFlow()

    fun setMaxConcurrentDownloads(value: Int) {
        val clamped = value.coerceIn(1, 10)
        prefs.edit().putInt(KEY_MAX_CONCURRENT, clamped).apply()
        _maxConcurrentDownloads.value = clamped
    }

    fun setDefaultVideoQuality(value: String) {
        prefs.edit().putString(KEY_DEFAULT_VIDEO_QUALITY, value).apply()
        _defaultVideoQuality.value = value
    }

    fun setDefaultAudioFormat(value: String) {
        prefs.edit().putString(KEY_DEFAULT_AUDIO_FORMAT, value).apply()
        _defaultAudioFormat.value = value
    }

    fun setDefaultAudioQuality(value: String) {
        prefs.edit().putString(KEY_DEFAULT_AUDIO_QUALITY, value).apply()
        _defaultAudioQuality.value = value
    }

    fun setWifiOnly(value: Boolean) {
        prefs.edit().putBoolean(KEY_WIFI_ONLY, value).apply()
        _wifiOnly.value = value
    }

    fun setNotificationsEnabled(value: Boolean) {
        prefs.edit().putBoolean(KEY_NOTIFICATIONS_ENABLED, value).apply()
        _notificationsEnabled.value = value
    }

    fun setAutoRetry(value: Boolean) {
        prefs.edit().putBoolean(KEY_AUTO_RETRY, value).apply()
        _autoRetry.value = value
    }

    companion object {
        private const val KEY_MAX_CONCURRENT = "max_concurrent"
        private const val KEY_DEFAULT_VIDEO_QUALITY = "default_video_quality"
        private const val KEY_DEFAULT_AUDIO_FORMAT = "default_audio_format"
        private const val KEY_DEFAULT_AUDIO_QUALITY = "default_audio_quality"
        private const val KEY_WIFI_ONLY = "wifi_only"
        private const val KEY_NOTIFICATIONS_ENABLED = "notifications_enabled"
        private const val KEY_AUTO_RETRY = "auto_retry"
    }
}
