package com.recorder.app.data.entity

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class EntitiesTest {

    @Test
    fun recording_defaultIdIsZero() {
        val recording = Recording(fileName = "test.aac", filePath = "/test.aac")
        assertEquals(0, recording.id)
    }

    @Test
    fun recording_equalityById() {
        val r1 = Recording(id = 1, fileName = "a.aac", filePath = "/a.aac")
        val r2 = Recording(id = 1, fileName = "a.aac", filePath = "/a.aac")
        assertEquals(r1, r2)
    }

    @Test
    fun recording_differentIds_notEqual() {
        val r1 = Recording(id = 1, fileName = "a.aac", filePath = "/a.aac")
        val r2 = Recording(id = 2, fileName = "a.aac", filePath = "/a.aac")
        assertNotEquals(r1, r2)
    }

    @Test
    fun recording_copy_preservesId() {
        val r1 = Recording(id = 5, fileName = "orig.aac", filePath = "/orig.aac")
        val copied = r1.copy(durationMs = 9999)
        assertEquals(5, copied.id)
        assertEquals(9999, copied.durationMs)
        assertEquals("orig.aac", copied.fileName)
    }

    @Test
    fun timeMarker_defaultValues() {
        val marker = TimeMarker(recordingId = 1, positionMs = 5000)
        assertEquals(0, marker.id)
        assertEquals(1, marker.recordingId)
        assertEquals(5000, marker.positionMs)
    }

    @Test
    fun timeMarker_copyPreservesValues() {
        val marker = TimeMarker(id = 10, recordingId = 3, positionMs = 7000)
        val copied = marker.copy(positionMs = 8000)
        assertEquals(10, copied.id)
        assertEquals(3, copied.recordingId)
        assertEquals(8000, copied.positionMs)
    }

    @Test
    fun photo_defaultValues() {
        val photo = Photo(recordingId = 2, filePath = "/photos/x.jpg", positionMs = 3000)
        assertEquals(0, photo.id)
        assertEquals(2, photo.recordingId)
        assertEquals("/photos/x.jpg", photo.filePath)
        assertEquals(3000, photo.positionMs)
    }

    @Test
    fun photo_copyPreservesValues() {
        val photo = Photo(id = 7, recordingId = 4, filePath = "/a.jpg", positionMs = 5000)
        val copied = photo.copy(filePath = "/b.jpg")
        assertEquals(7, copied.id)
        assertEquals(4, copied.recordingId)
        assertEquals("/b.jpg", copied.filePath)
        assertEquals(5000, copied.positionMs)
    }
}
