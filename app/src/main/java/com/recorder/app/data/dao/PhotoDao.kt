package com.recorder.app.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.recorder.app.data.entity.Photo

@Dao
interface PhotoDao {

    @Query("SELECT * FROM photos WHERE recordingId = :recordingId ORDER BY positionMs ASC")
    suspend fun getPhotosForRecording(recordingId: Long): List<Photo>

    @Query("SELECT * FROM photos WHERE id = :id")
    suspend fun getPhotoById(id: Long): Photo?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(photo: Photo): Long

    @Query("DELETE FROM photos WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM photos WHERE recordingId = :recordingId")
    suspend fun deleteByRecordingId(recordingId: Long)
}
