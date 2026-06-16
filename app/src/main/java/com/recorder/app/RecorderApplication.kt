package com.recorder.app

import android.app.Application
import com.recorder.app.data.database.AppDatabase

class RecorderApplication : Application() {

    val database: AppDatabase by lazy { AppDatabase.getInstance(this) }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        lateinit var instance: RecorderApplication
            private set
    }
}
