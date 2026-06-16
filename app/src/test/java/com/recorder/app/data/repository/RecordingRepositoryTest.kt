package com.recorder.app.data.repository

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.recorder.app.data.database.AppDatabase
import com.recorder.app.data.entity.Recording
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RecordingRepositoryTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: AppDatabase
    private lateinit var repository: RecordingRepository
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).build()
        repository = RecordingRepository(
            database.recordingDao(),
            database.markerDao(),
            database.photoDao()
        )
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun insertRecording_returnsId() = runTest(testDispatcher) {
        val recording = Recording(
            fileName = "test.aac",
            filePath = "/test/test.aac",
            durationMs = 5000,
            fileSizeBytes = 1024
        )
        val id = repository.insertRecording(recording)
        assertTrue(id > 0)
    }

    @Test
    fun getRecordingById_returnsCorrectRecording() = runTest(testDispatcher) {
        val recording = Recording(
            fileName = "test.aac",
            filePath = "/test/test.aac",
            durationMs = 5000,
            fileSizeBytes = 1024
        )
        val id = repository.insertRecording(recording)
        val result = repository.getRecordingById(id)
        assertNotNull(result)
        assertEquals("test.aac", result?.fileName)
        assertEquals(5000, result?.durationMs)
    }

    @Test
    fun getAllRecordingsSync_returnsAll() = runTest(testDispatcher) {
        repository.insertRecording(Recording(fileName = "a.aac", filePath = "/a.aac"))
        repository.insertRecording(Recording(fileName = "b.aac", filePath = "/b.aac"))
        val list = repository.getAllRecordingsSync()
        assertEquals(2, list.size)
    }

    @Test
    fun updateRecording_updatesFields() = runTest(testDispatcher) {
        val recording = Recording(fileName = "old.aac", filePath = "/old.aac")
        val id = repository.insertRecording(recording)
        val updated = recording.copy(id = id, durationMs = 10000, fileSizeBytes = 2048)
        repository.updateRecording(updated)
        val result = repository.getRecordingById(id)
        assertEquals(10000, result?.durationMs)
        assertEquals(2048, result?.fileSizeBytes)
    }

    @Test
    fun deleteRecording_removesFromDb() = runTest(testDispatcher) {
        val recording = Recording(fileName = "del.aac", filePath = "/del.aac")
        val id = repository.insertRecording(recording)
        val saved = repository.getRecordingById(id)
        assertNotNull(saved)
        saved?.let { repository.deleteRecording(it) }
        val afterDelete = repository.getRecordingById(id)
        assertNull(afterDelete)
    }

    @Test
    fun addMarker_returnsId() = runTest(testDispatcher) {
        val recording = Recording(fileName = "m.aac", filePath = "/m.aac")
        val recId = repository.insertRecording(recording)
        val markerId = repository.addMarker(recId, 3000)
        assertTrue(markerId > 0)
    }

    @Test
    fun getMarkersForRecording_returnsAllMarkers() = runTest(testDispatcher) {
        val recording = Recording(fileName = "m.aac", filePath = "/m.aac")
        val recId = repository.insertRecording(recording)
        repository.addMarker(recId, 1000)
        repository.addMarker(recId, 5000)
        repository.addMarker(recId, 3000)

        val markers = repository.getMarkersForRecording(recId)
        assertEquals(3, markers.size)
        assertEquals(1000, markers[0].positionMs)
        assertEquals(3000, markers[1].positionMs)
        assertEquals(5000, markers[2].positionMs)
    }

    @Test
    fun addPhoto_returnsId() = runTest(testDispatcher) {
        val recording = Recording(fileName = "p.aac", filePath = "/p.aac")
        val recId = repository.insertRecording(recording)
        val photoId = repository.addPhoto(recId, "/photos/test.jpg", 2000)
        assertTrue(photoId > 0)
    }

    @Test
    fun getPhotosForRecording_returnsAllPhotos() = runTest(testDispatcher) {
        val recording = Recording(fileName = "p.aac", filePath = "/p.aac")
        val recId = repository.insertRecording(recording)
        repository.addPhoto(recId, "/photos/a.jpg", 1000)
        repository.addPhoto(recId, "/photos/b.jpg", 5000)

        val photos = repository.getPhotosForRecording(recId)
        assertEquals(2, photos.size)
        assertEquals("/photos/a.jpg", photos[0].filePath)
        assertEquals(1000, photos[0].positionMs)
    }

    @Test
    fun getPhotoById_returnsCorrectPhoto() = runTest(testDispatcher) {
        val recording = Recording(fileName = "p.aac", filePath = "/p.aac")
        val recId = repository.insertRecording(recording)
        val photoId = repository.addPhoto(recId, "/photos/test.jpg", 2000)

        val photo = repository.getPhotoById(photoId)
        assertNotNull(photo)
        assertEquals("/photos/test.jpg", photo?.filePath)
        assertEquals(2000, photo?.positionMs)
    }

    @Test
    fun deleteRecording_cascadesMarkers() = runTest(testDispatcher) {
        val recording = Recording(fileName = "c.aac", filePath = "/c.aac")
        val recId = repository.insertRecording(recording)
        repository.addMarker(recId, 1000)
        repository.addMarker(recId, 2000)

        val saved = repository.getRecordingById(recId)
        saved?.let { repository.deleteRecording(it) }

        val markers = repository.getMarkersForRecording(recId)
        assertTrue(markers.isEmpty())
    }

    @Test
    fun deleteRecording_cascadesPhotos() = runTest(testDispatcher) {
        val recording = Recording(fileName = "c.aac", filePath = "/c.aac")
        val recId = repository.insertRecording(recording)
        repository.addPhoto(recId, "/photos/x.jpg", 1000)
        repository.addPhoto(recId, "/photos/y.jpg", 2000)

        val saved = repository.getRecordingById(recId)
        saved?.let { repository.deleteRecording(it) }

        val photos = repository.getPhotosForRecording(recId)
        assertTrue(photos.isEmpty())
    }
}
