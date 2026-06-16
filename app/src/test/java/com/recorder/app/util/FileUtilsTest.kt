package com.recorder.app.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FileUtilsTest {

    @Test
    fun generateRecordingFileName_containsUnderscore() {
        val name = FileUtils.generateRecordingFileName()
        assertTrue(name.contains("录音_"))
        assertTrue(name.endsWith(".aac"))
    }

    @Test
    fun generatePhotoFileName_containsPrefix() {
        val name = FileUtils.generatePhotoFileName()
        assertTrue(name.startsWith("photo_"))
        assertTrue(name.endsWith(".jpg"))
    }

    @Test
    fun generateRecordingFileName_uniquePerCall() {
        val name1 = FileUtils.generateRecordingFileName()
        Thread.sleep(1100)
        val name2 = FileUtils.generateRecordingFileName()
        assertTrue(name1 != name2)
    }
}
