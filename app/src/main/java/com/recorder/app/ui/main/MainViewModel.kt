package com.recorder.app.ui.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.recorder.app.RecorderApplication
import com.recorder.app.data.entity.Recording
import com.recorder.app.data.repository.RecordingRepository
import com.recorder.app.service.RecordingManager
import com.recorder.app.service.RecordingState
import com.recorder.app.util.FileUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as RecorderApplication
    private val repository: RecordingRepository
    val recordingManager: RecordingManager

    val allRecordings: LiveData<List<Recording>>
    val recordingState = MutableLiveData(RecordingState.IDLE)
    val elapsedTimeMs = MutableLiveData(0L)

    private var currentRecordingId: Long = -1
    private var elapsedJob: Job? = null

    init {
        val db = app.database
        repository = RecordingRepository(
            db.recordingDao(),
            db.markerDao(),
            db.photoDao()
        )
        recordingManager = RecordingManager(application)
        allRecordings = repository.allRecordings
    }

    fun startRecording() {
        val context = getApplication<RecorderApplication>()
        val file = File(FileUtils.getRecordingsDir(context), FileUtils.generateRecordingFileName())

        viewModelScope.launch(Dispatchers.IO) {
            try {
                recordingManager.startRecording(file)
                recordingState.postValue(RecordingState.RECORDING)
                startElapsedTimer()

                val recording = Recording(
                    fileName = file.name,
                    filePath = file.absolutePath,
                    createdAt = System.currentTimeMillis()
                )
                currentRecordingId = repository.insertRecording(recording)
            } catch (e: Exception) {
                recordingState.postValue(RecordingState.IDLE)
            }
        }
    }

    fun pauseRecording() {
        if (recordingManager.state.value == RecordingState.RECORDING) {
            recordingManager.pauseRecording()
            recordingState.value = RecordingState.PAUSED
            stopElapsedTimer()
        }
    }

    fun resumeRecording() {
        if (recordingManager.state.value == RecordingState.PAUSED) {
            recordingManager.resumeRecording()
            recordingState.value = RecordingState.RECORDING
            startElapsedTimer()
        }
    }

    fun stopRecording() {
        val durationMs = recordingManager.stopRecording()
        stopElapsedTimer()
        recordingState.value = RecordingState.IDLE

        if (currentRecordingId > 0) {
            viewModelScope.launch(Dispatchers.IO) {
                val recording = repository.getRecordingById(currentRecordingId)
                recording?.let {
                    val file = File(it.filePath)
                    val fileSize = if (file.exists()) file.length() else 0
                    val updated = it.copy(durationMs = durationMs, fileSizeBytes = fileSize)
                    repository.updateRecording(updated)
                }
                launch(Dispatchers.Main) {
                    elapsedTimeMs.value = durationMs
                }
            }
        }
    }

    fun addTimeMarker() {
        if (currentRecordingId <= 0) return
        viewModelScope.launch(Dispatchers.IO) {
            val positionMs = recordingManager.getElapsedTimeMs()
            repository.addMarker(currentRecordingId, positionMs)
        }
    }

    fun addPhoto(filePath: String) {
        if (currentRecordingId <= 0) return
        viewModelScope.launch(Dispatchers.IO) {
            val positionMs = recordingManager.getElapsedTimeMs()
            repository.addPhoto(currentRecordingId, filePath, positionMs)
        }
    }

    fun toggleRecord() {
        when (recordingManager.state.value) {
            RecordingState.IDLE -> startRecording()
            RecordingState.RECORDING -> pauseRecording()
            RecordingState.PAUSED -> resumeRecording()
        }
    }

    fun getCurrentRecordingId(): Long = currentRecordingId

    private fun startElapsedTimer() {
        elapsedJob?.cancel()
        elapsedJob = viewModelScope.launch {
            while (isActive) {
                elapsedTimeMs.value = recordingManager.getElapsedTimeMs()
                delay(50)
            }
        }
    }

    private fun stopElapsedTimer() {
        elapsedJob?.cancel()
        elapsedJob = null
    }

    override fun onCleared() {
        super.onCleared()
        stopElapsedTimer()
        recordingManager.release()
    }
}
