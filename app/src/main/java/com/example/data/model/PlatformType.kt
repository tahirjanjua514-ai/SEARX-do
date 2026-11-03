package com.example.data.model

import androidx.compose.ui.graphics.Color

enum class PlatformType(
    val displayName: String,
    val primaryColor: Color,
    val domainKeywords: List<String>
) {
    YOUTUBE(
        displayName = "YouTube",
        primaryColor = Color(0xFFFF0000),
        domainKeywords = listOf("youtube.com", "youtu.be", "m.youtube.com")
    ),
    FACEBOOK(
        displayName = "Facebook",
        primaryColor = Color(0xFF1877F2),
        domainKeywords = listOf("facebook.com", "fb.watch", "fb.com", "m.facebook.com")
    ),
    INSTAGRAM(
        displayName = "Instagram",
        primaryColor = Color(0xFFE1306C),
        domainKeywords = listOf("instagram.com", "instagr.am")
    ),
    TIKTOK(
        displayName = "TikTok",
        primaryColor = Color(0xFF00F2FE),
        domainKeywords = listOf("tiktok.com", "vm.tiktok.com", "vt.tiktok.com")
    ),
    DIRECT(
        displayName = "Direct Media",
        primaryColor = Color(0xFF8B5CF6),
        domainKeywords = emptyList()
    );

    companion object {
        fun detect(url: String): PlatformType {
            val clean = url.trim().lowercase()
            return when {
                YOUTUBE.domainKeywords.any { clean.contains(it) } -> YOUTUBE
                FACEBOOK.domainKeywords.any { clean.contains(it) } -> FACEBOOK
                INSTAGRAM.domainKeywords.any { clean.contains(it) } -> INSTAGRAM
                TIKTOK.domainKeywords.any { clean.contains(it) } -> TIKTOK
                else -> DIRECT
            }
        }
    }
}
