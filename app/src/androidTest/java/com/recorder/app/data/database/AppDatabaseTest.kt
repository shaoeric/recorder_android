package com.recorder.app.data.database

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.recorder.app.data.entity.Photo
import com.recorder.app.data.entity.Recording
import com.recorder.app.data.entity.TimeMarker
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppDatabaseTest {

    private lateinit var database: AppDatabase

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).build()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun insertAndRetrieveRecording() = runBlocking {
        val dao = database.recordingDao()
        val recording = Recording(
            fileName = "test.aac",
            filePath = "/test/test.aac",
            durationMs = 10000,
            fileSizeBytes = 2048
        )
        val id = dao.insert(recording)
        assertTrue(id > 0)

        val retrieved = dao.getRecordingById(id)
        assertNotNull(retrieved)
        assertEquals("test.aac", retrieved?.fileName)
        assertEquals(10000, retrieved?.durationMs)
    }

    @Test
    fun updateRecording() = runBlocking {
        val dao = database.recordingDao()
        val id = dao.insert(Recording(fileName = "old.aac", filePath = "/old.aac"))
        val rec = dao.getRecordingById(id)!!
        dao.update(rec.copy(durationMs = 5000, fileSizeBytes = 4096))

        val updated = dao.getRecordingById(id)
        assertEquals(5000, updated?.durationMs)
        assertEquals(4096, updated?.fileSizeBytes)
    }

    @Test
    fun deleteRecording() = runBlocking {
        val dao = database.recordingDao()
        val id = dao.insert(Recording(fileName = "del.aac", filePath = "/del.aac"))
        val rec = dao.getRecordingById(id)!!
        dao.delete(rec)
        assertNull(dao.getRecordingById(id))
    }

    @Test
    fun insertAndRetrieveMarkers() = runBlocking {
        val recDao = database.recordingDao()
        val markerDao = database.markerDao()

        val recId = recDao.insert(Recording(fileName = "m.aac", filePath = "/m.aac"))

        markerDao.insert(TimeMarker(recordingId = recId, positionMs = 1000))
        markerDao.insert(TimeMarker(recordingId = recId, positionMs = 5000))
        markerDao.insert(TimeMarker(recordingId = recId, positionMs = 3000))

        val markers = markerDao.getMarkersForRecording(recId)
        assertEquals(3, markers.size)
        assertEquals(1000, markers[0].positionMs)
        assertEquals(3000, markers[1].positionMs)
        assertEquals(5000, markers[2].positionMs)
    }

    @Test
    fun deleteMarkerById() = runBlocking {
        val recDao = database.recordingDao()
        val markerDao = database.markerDao()

        val recId = recDao.insert(Recording(fileName = "m.aac", filePath = "/m.aac"))
        val markerId = markerDao.insert(TimeMarker(recordingId = recId, positionMs = 1000))
        markerDao.insert(TimeMarker(recordingId = recId, positionMs = 2000))

        markerDao.deleteById(markerId)
        val markers = markerDao.getMarkersForRecording(recId)
        assertEquals(1, markers.size)
        assertEquals(2000, markers[0].positionMs)
    }

    @Test
    fun insertAndRetrievePhotos() = runBlocking {
        val recDao = database.recordingDao()
        val photoDao = database.photoDao()

        val recId = recDao.insert(Recording(fileName = "p.aac", filePath = "/p.aac"))

        photoDao.insert(Photo(recordingId = recId, filePath = "/a.jpg", positionMs = 2000))
        photoDao.insert(Photo(recordingId = recId, filePath = "/b.jpg", positionMs = 4000))

        val photos = photoDao.getPhotosForRecording(recId)
        assertEquals(2, photos.size)
        assertEquals("/a.jpg", photos[0].filePath)
        assertEquals(2000, photos[0].positionMs)
    }

    @Test
    fun getPhotoById() = runBlocking {
        val recDao = database.recordingDao()
        val photoDao = database.photoDao()

        val recId = recDao.insert(Recording(fileName = "p.aac", filePath = "/p.aac"))
        val photoId = photoDao.insert(Photo(recordingId = recId, filePath = "/x.jpg", positionMs = 3000))

        val photo = photoDao.getPhotoById(photoId)
        assertNotNull(photo)
        assertEquals("/x.jpg", photo?.filePath)
    }

    @Test
    fun cascadeDeleteMarkersOnRecordingDelete() = runBlocking {
        val recDao = database.recordingDao()
        val markerDao = database.markerDao()

        val recId = recDao.insert(Recording(fileName = "c.aac", filePath = "/c.aac"))
        markerDao.insert(TimeMarker(recordingId = recId, positionMs = 1000))
        markerDao.insert(TimeMarker(recordingId = recId, positionMs = 2000))

        val rec = recDao.getRecordingById(recId)!!
        recDao.delete(rec)

        val markers = markerDao.getMarkersForRecording(recId)
        assertTrue(markers.isEmpty())
    }

    @Test
    fun cascadeDeletePhotosOnRecordingDelete() = runBlocking {
        val recDao = database.recordingDao()
        val photoDao = database.photoDao()

        val recId = recDao.insert(Recording(fileName = "c.aac", filePath = "/c.aac"))
        photoDao.insert(Photo(recordingId = recId, filePath = "/x.jpg", positionMs = 1000))
        photoDao.insert(Photo(recordingId = recId, filePath = "/y.jpg", positionMs = 2000))

        val rec = recDao.getRecordingById(recId)!!
        recDao.delete(rec)

        val photos = photoDao.getPhotosForRecording(recId)
        assertTrue(photos.isEmpty())
    }
}
