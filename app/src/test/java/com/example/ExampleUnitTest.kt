package com.example

import com.example.data.model.PlatformType
import com.example.data.model.formatBytes
import org.junit.Assert.assertEquals
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun testPlatformDetection() {
        assertEquals(PlatformType.YOUTUBE, PlatformType.detect("https://www.youtube.com/watch?v=dQw4w9WgXcQ"))
        assertEquals(PlatformType.YOUTUBE, PlatformType.detect("https://youtu.be/dQw4w9WgXcQ"))
        assertEquals(PlatformType.TIKTOK, PlatformType.detect("https://www.tiktok.com/@creator/video/1234567890"))
        assertEquals(PlatformType.INSTAGRAM, PlatformType.detect("https://www.instagram.com/reel/Cx123456/"))
        assertEquals(PlatformType.FACEBOOK, PlatformType.detect("https://fb.watch/abcdef123/"))
        assertEquals(PlatformType.DIRECT, PlatformType.detect("https://example.com/video.mp4"))
    }

    @Test
    fun testFormatBytes() {
        assertEquals("500 B", formatBytes(500))
        assertEquals("100 KB", formatBytes(100 * 1024))
        assertEquals("50.0 MB", formatBytes(50 * 1024 * 1024))
        assertEquals("1.50 GB", formatBytes((1.5 * 1024 * 1024 * 1024).toLong()))
    }
}
