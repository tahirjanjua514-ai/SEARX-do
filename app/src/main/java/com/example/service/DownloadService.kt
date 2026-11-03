package com.example.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import com.example.MainActivity
import com.example.SEARXApplication
import com.example.data.db.DownloadEntity
import com.example.data.model.formatBytes
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class DownloadService : Service() {
    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var observeJob: Job? = null
    private lateinit var notificationManager: NotificationManager

    companion object {
        const val CHANNEL_DOWNLOADING = "searx_downloading_channel"
        const val CHANNEL_COMPLETED = "searx_completed_channel"
        const val NOTIFICATION_ID = 1001

        const val ACTION_START_SERVICE = "com.example.ACTION_START_SERVICE"
        const val ACTION_PAUSE_ALL = "com.example.ACTION_PAUSE_ALL"
        const val ACTION_RESUME_ALL = "com.example.ACTION_RESUME_ALL"
        const val ACTION_CANCEL_ALL = "com.example.ACTION_CANCEL_ALL"

        fun start(context: Context) {
            val intent = Intent(context, DownloadService::class.java).apply {
                action = ACTION_START_SERVICE
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannels()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val app = application as? SEARXApplication
        val engine = app?.downloadEngine

        when (intent?.action) {
            ACTION_PAUSE_ALL -> engine?.pauseAll()
            ACTION_RESUME_ALL -> engine?.resumeAll()
            ACTION_CANCEL_ALL -> {
                engine?.cancelAll()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
                return START_NOT_STICKY
            }
        }

        startForegroundWithNotification()
        observeActiveDownloads()

        return START_STICKY
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val downloadChannel = NotificationChannel(
                CHANNEL_DOWNLOADING,
                "Active Downloads",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows real-time download progress and speed"
                setShowBadge(false)
            }

            val completedChannel = NotificationChannel(
                CHANNEL_COMPLETED,
                "Download Completed",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifies when downloads finish"
            }

            notificationManager.createNotificationChannel(downloadChannel)
            notificationManager.createNotificationChannel(completedChannel)
        }
    }

    private fun startForegroundWithNotification() {
        val openIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_DOWNLOADING)
            .setContentTitle("SEARX AI Downloader")
            .setContentText("Background download engine active")
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ServiceCompat.startForeground(
                this,
                NOTIFICATION_ID,
                notification,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
                } else {
                    0
                }
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun observeActiveDownloads() {
        val app = application as? SEARXApplication ?: return
        val repo = app.repository

        observeJob?.cancel()
        observeJob = serviceScope.launch {
            repo.activeDownloads.collectLatest { downloads ->
                val activeDownloading = downloads.filter { it.status == "DOWNLOADING" }

                if (activeDownloading.isEmpty()) {
                    // When nothing is downloading, update notification to idle or stop foreground
                    if (downloads.isEmpty()) {
                        stopForeground(STOP_FOREGROUND_REMOVE)
                        stopSelf()
                    } else {
                        updateNotificationIdle(downloads.size)
                    }
                } else {
                    updateNotificationProgress(activeDownloading)
                }
            }
        }
    }

    private fun updateNotificationProgress(activeList: List<DownloadEntity>) {
        val count = activeList.size
        val primary = activeList.first()
        val totalSpeed = activeList.sumOf { it.speedBytesPerSec }
        val speedStr = formatSpeed(totalSpeed)

        val avgProgress = (activeList.sumOf { it.progress.toDouble() } / count.toDouble() * 100).toInt()

        val openIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Pause action
        val pauseIntent = Intent(this, DownloadService::class.java).apply { action = ACTION_PAUSE_ALL }
        val pausePendingIntent = PendingIntent.getService(
            this,
            1,
            pauseIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = if (count == 1) primary.title else "Downloading ($count files)"
        val contentText = "$avgProgress% • Speed: $speedStr • ETA: ${formatEta(primary.etaSeconds)}"

        val notification = NotificationCompat.Builder(this, CHANNEL_DOWNLOADING)
            .setContentTitle(title)
            .setContentText(contentText)
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setProgress(100, avgProgress.coerceIn(0, 100), false)
            .setContentIntent(pendingIntent)
            .addAction(android.R.drawable.ic_media_pause, "Pause All", pausePendingIntent)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun updateNotificationIdle(queuedCount: Int) {
        val openIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_DOWNLOADING)
            .setContentTitle("SEARX AI Downloader")
            .setContentText("$queuedCount items queued/paused")
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun formatSpeed(bytesPerSec: Long): String {
        val mb = bytesPerSec / (1024.0 * 1024.0)
        val kb = bytesPerSec / 1024.0
        return when {
            mb >= 1.0 -> String.format("%.1f MB/s", mb)
            kb >= 1.0 -> String.format("%.0f KB/s", kb)
            else -> "$bytesPerSec B/s"
        }
    }

    private fun formatEta(seconds: Long): String {
        if (seconds <= 0) return "--:--"
        val m = seconds / 60
        val s = seconds % 60
        return String.format("%02d:%02d", m, s)
    }

    override fun onDestroy() {
        super.onDestroy()
        observeJob?.cancel()
    }
}
