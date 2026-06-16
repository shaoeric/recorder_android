package com.recorder.app.data.repository

import androidx.lifecycle.LiveData
import com.recorder.app.data.dao.MarkerDao
import com.recorder.app.data.dao.PhotoDao
import com.recorder.app.data.dao.RecordingDao
import com.recorder.app.data.entity.Photo
import com.recorder.app.data.entity.Recording
import com.recorder.app.data.entity.TimeMarker
import java.io.File

class RecordingRepository(
    private val recordingDao: RecordingDao,
    private val markerDao: MarkerDao,
    private val photoDao: PhotoDao
) {

    val allRecordings: LiveData<List<Recording>> = recordingDao.getAllRecordings()

    suspend fun getAllRecordingsSync(): List<Recording> = recordingDao.getAllRecordingsSync()

    suspend fun getRecordingById(id: Long): Recording? = recordingDao.getRecordingById(id)

    suspend fun insertRecording(recording: Recording): Long = recordingDao.insert(recording)

    suspend fun updateRecording(recording: Recording) = recordingDao.update(recording)

    suspend fun deleteRecording(recording: Recording) {
        deleteRecordingFile(recording.filePath)
        deletePhotosForRecording(recording.id)
        recordingDao.delete(recording)
    }

    suspend fun addMarker(recordingId: Long, positionMs: Long): Long {
        val marker = TimeMarker(recordingId = recordingId, positionMs = positionMs)
        return markerDao.insert(marker)
    }

    suspend fun getMarkersForRecording(recordingId: Long): List<TimeMarker> =
        markerDao.getMarkersForRecording(recordingId)

    suspend fun addPhoto(recordingId: Long, filePath: String, positionMs: Long): Long {
        val photo = Photo(recordingId = recordingId, filePath = filePath, positionMs = positionMs)
        return photoDao.insert(photo)
    }

    suspend fun getPhotosForRecording(recordingId: Long): List<Photo> =
        photoDao.getPhotosForRecording(recordingId)

    suspend fun getPhotoById(id: Long): Photo? = photoDao.getPhotoById(id)

    private fun deleteRecordingFile(filePath: String) {
        try {
            val file = File(filePath)
            if (file.exists()) {
                file.delete()
            }
        } catch (_: Exception) {
        }
    }

    private suspend fun deletePhotosForRecording(recordingId: Long) {
        val photos = photoDao.getPhotosForRecording(recordingId)
        photos.forEach { photo ->
            try {
                val file = File(photo.filePath)
                if (file.exists()) {
                    file.delete()
                }
            } catch (_: Exception) {
            }
        }
        photoDao.deleteByRecordingId(recordingId)
    }
}
