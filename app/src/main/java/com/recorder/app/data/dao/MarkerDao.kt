package com.recorder.app.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.recorder.app.data.entity.TimeMarker

@Dao
interface MarkerDao {

    @Query("SELECT * FROM time_markers WHERE recordingId = :recordingId ORDER BY positionMs ASC")
    suspend fun getMarkersForRecording(recordingId: Long): List<TimeMarker>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(marker: TimeMarker): Long

    @Query("DELETE FROM time_markers WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM time_markers WHERE recordingId = :recordingId")
    suspend fun deleteByRecordingId(recordingId: Long)
}
