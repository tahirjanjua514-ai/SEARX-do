package com.example

import android.app.Application
import com.example.data.db.AppDatabase
import com.example.data.db.DownloadRepository
import com.example.data.downloader.DownloadEngine
import com.example.data.preferences.AppPreferences

class SEARXApplication : Application() {
    lateinit var database: AppDatabase
        private set

    lateinit var repository: DownloadRepository
        private set

    lateinit var preferences: AppPreferences
        private set

    lateinit var downloadEngine: DownloadEngine
        private set

    override fun onCreate() {
        super.onCreate()
        database = AppDatabase.getInstance(this)
        repository = DownloadRepository(database.downloadDao())
        preferences = AppPreferences(this)
        downloadEngine = DownloadEngine(this, repository, preferences)

        downloadEngine.startQueueProcessor()
    }
}
