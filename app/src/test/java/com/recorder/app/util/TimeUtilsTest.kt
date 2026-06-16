package com.recorder.app.util

import org.junit.Assert.assertEquals
import org.junit.Test

class TimeUtilsTest {

    @Test
    fun formatDuration_zero() {
        assertEquals("00:00", TimeUtils.formatDuration(0))
    }

    @Test
    fun formatDuration_seconds() {
        assertEquals("00:05", TimeUtils.formatDuration(5000))
    }

    @Test
    fun formatDuration_minutesAndSeconds() {
        assertEquals("02:30", TimeUtils.formatDuration(150000))
    }

    @Test
    fun formatDuration_oneHour() {
        assertEquals("60:00", TimeUtils.formatDuration(3600000))
    }

    @Test
    fun formatDuration_largeValue() {
        assertEquals("100:00", TimeUtils.formatDuration(6000000))
    }

    @Test
    fun formatFileSize_bytes() {
        assertEquals("500 B", TimeUtils.formatFileSize(500))
    }

    @Test
    fun formatFileSize_kb() {
        assertEquals("15 KB", TimeUtils.formatFileSize(15360))
    }

    @Test
    fun formatFileSize_mb() {
        assertEquals("2.5 MB", TimeUtils.formatFileSize(2621440))
    }

    @Test
    fun formatFileSize_zero() {
        assertEquals("0 B", TimeUtils.formatFileSize(0))
    }

    @Test
    fun formatDate_returnsNonEmptyString() {
        val result = TimeUtils.formatDate(System.currentTimeMillis())
        assert(result.isNotEmpty())
        assert(result.contains("-"))
        assert(result.contains(":"))
    }
}
