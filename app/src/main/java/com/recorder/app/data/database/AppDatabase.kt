package com.recorder.app.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.recorder.app.data.dao.MarkerDao
import com.recorder.app.data.dao.PhotoDao
import com.recorder.app.data.dao.RecordingDao
import com.recorder.app.data.entity.Photo
import com.recorder.app.data.entity.Recording
import com.recorder.app.data.entity.TimeMarker

@Database(
    entities = [Recording::class, TimeMarker::class, Photo::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun recordingDao(): RecordingDao
    abstract fun markerDao(): MarkerDao
    abstract fun photoDao(): PhotoDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "recorder_db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
