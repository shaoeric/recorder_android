package com.recorder.app.util

import android.content.Context
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object FileUtils {

    fun getRecordingsDir(context: Context): File {
        val dir = File(context.filesDir, "recordings")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    fun getPhotosDir(context: Context): File {
        val dir = File(context.cacheDir, "photos")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    fun generateRecordingFileName(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault())
        return "录音_${sdf.format(Date())}.aac"
    }

    fun generatePhotoFileName(): String {
        val sdf = SimpleDateFormat("yyyyMMdd_HHmmss_SSS", Locale.getDefault())
        return "photo_${sdf.format(Date())}.jpg"
    }
}
