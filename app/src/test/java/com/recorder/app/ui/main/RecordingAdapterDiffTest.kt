package com.recorder.app.ui.main

import com.recorder.app.data.entity.Recording
import com.recorder.app.util.TimeUtils
import org.junit.Assert.assertEquals
import org.junit.Test

class RecordingAdapterDiffTest {

    @Test
    fun areItemsTheSame_sameId_returnsTrue() {
        val r1 = Recording(id = 1, fileName = "a.aac", filePath = "/a.aac")
        val r2 = Recording(id = 1, fileName = "b.aac", filePath = "/b.aac")
        assertEquals(r1.id, r2.id)
    }

    @Test
    fun areItemsTheSame_differentId_returnsFalse() {
        val r1 = Recording(id = 1, fileName = "a.aac", filePath = "/a.aac")
        val r2 = Recording(id = 2, fileName = "a.aac", filePath = "/a.aac")
        assertEquals(r1.id != r2.id, true)
    }

    @Test
    fun areContentsTheSame_identicalRecordings_returnsTrue() {
        val r1 = Recording(id = 1, fileName = "a.aac", filePath = "/a.aac", durationMs = 5000)
        val r2 = Recording(id = 1, fileName = "a.aac", filePath = "/a.aac", durationMs = 5000)
        assertEquals(r1, r2)
    }

    @Test
    fun areContentsTheSame_differentDuration_returnsFalse() {
        val r1 = Recording(id = 1, fileName = "a.aac", filePath = "/a.aac", durationMs = 5000)
        val r2 = Recording(id = 1, fileName = "a.aac", filePath = "/a.aac", durationMs = 6000)
        assertEquals(r1 != r2, true)
    }

    @Test
    fun formatRecordingDisplay_duration() {
        val r = Recording(
            id = 1,
            fileName = "test.aac",
            filePath = "/test.aac",
            durationMs = 150000
        )
        assertEquals("02:30", TimeUtils.formatDuration(r.durationMs))
    }

    @Test
    fun formatRecordingDisplay_fileSize() {
        val r = Recording(
            id = 1,
            fileName = "test.aac",
            filePath = "/test.aac",
            fileSizeBytes = 1048576
        )
        assertEquals("1.0 MB", TimeUtils.formatFileSize(r.fileSizeBytes))
    }
}
