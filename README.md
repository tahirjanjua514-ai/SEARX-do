# SEARX AI – All-in-One Media Downloader

**SEARX AI** is a professional Android media downloader developed with **Kotlin**, **Jetpack Compose**, **Material 3**, and modern Android architectural standards.

---

## 🌟 Key Features

### 1. Multi-Platform Media Analyzer
- **Automatic Platform Detection**: Paste any URL to instantly detect the platform (**YouTube**, **Facebook**, **Instagram**, **TikTok**, or **Direct Media**).
- **Metadata Extraction**: Resolves video title, creator, thumbnail, and duration via official oEmbed and OpenGraph protocols.
- **Sample High-Speed Streams**: Built-in 1-tap test streams (Big Buck Bunny, Tears of Steel, Chromecast) to test download speeds immediately.
- **Quality & Format Selection**:
  - **Video**: 360p, 480p, 720p HD, 1080p Full HD, 4K Ultra HD, Best Available (MP4 / WEBM).
  - **Audio Extraction**: MP3, M4A, WAV, and OPUS with bitrate selection (128 kbps, 192 kbps, 256 kbps, 320 kbps).

### 2. High-Speed Download Manager
- **Chunk-based OkHttp Engine**: Streams media directly into verified local storage files.
- **Accurate Real-Time Telemetry**: Real windowed download speed (e.g. `6.8 MB/s`), byte counts (`225 MB / 500 MB`), progress percentage, and dynamic ETA calculation (`00:38`).
- **HTTP Range Pause & Resume**: Paused downloads resume from the exact byte location without starting over.
- **Multiple Simultaneous Downloads**:
  - Unlimited queueing.
  - Configurable worker slots (1 to 10 parallel threads, default: 3).
  - Batch operations: **Pause All**, **Resume All**, **Cancel All**.

### 3. Background Execution & Foreground Service
- **Android Foreground Service (`DownloadService`)**: Downloads proceed seamlessly when the user switches apps or the screen locks.
- **Persistent Progress Notification**: Real-time progress bar, speed telemetry, and quick pause action right from the Android notification shade.
- **Foreground Service Type**: Compatible with Android 14+ (`FOREGROUND_SERVICE_DATA_SYNC`).

### 4. Verified Local Download History (Room Database)
- **Room SQLite Persistence**: Saves all completed and active download records with full metadata.
- **Secure File Provider**: Open files in system media players (`Intent.ACTION_VIEW`) or share files (`Intent.ACTION_SEND`) safely without exposing raw file paths.
- **Disk Verification**: Only verified files on device storage are marked as completed.

### 5. Premium Dark Theme Design
- Futuristic dark obsidian aesthetic with neon cyan and electric violet gradient accents.
- Responsive Jetpack Compose layout respecting gesture and camera cutouts (`WindowInsets`).

---

## 📱 How to Build & Install the APK

### 1. Build Debug APK (for Testing)
```bash
gradle :app:assembleDebug
```
The generated APK will be located at:
```
app/build/outputs/apk/debug/app-debug.apk
```

### 2. Build Signed Release APK (for Production)
```bash
gradle :app:assembleRelease
```
The output release APK will be located at:
```
app/build/outputs/apk/release/app-release.apk
```

### 3. Install on Android Device via ADB
```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

---

## 🛡️ Privacy & Compliance Notice
SEARX AI adheres strictly to Android security guidelines and copyright standards:
- Operates on publicly accessible and authorized media URLs.
- Does not bypass private login walls or DRM encryption.
- Zero credential harvesting or analytics tracking.
