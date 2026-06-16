package com.recorder.app.service

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.File
import java.io.IOException

enum class RecordingState {
    IDLE, RECORDING, PAUSED
}

class RecordingManager(private val context: Context) {

    private var mediaRecorder: MediaRecorder? = null
    private var _state = MutableStateFlow(RecordingState.IDLE)
    val state: StateFlow<RecordingState> = _state

    private var _currentPositionMs = MutableStateFlow(0L)
    val currentPositionMs: StateFlow<Long> = _currentPositionMs

    var currentFilePath: String = ""
        private set

    private var startTimeMs: Long = 0
    private var pausedDurationMs: Long = 0
    private var pauseStartMs: Long = 0

    @Throws(IOException::class)
    fun startRecording(outputFile: File) {
        currentFilePath = outputFile.absolutePath
        pausedDurationMs = 0
        startTimeMs = System.currentTimeMillis()
        _currentPositionMs.value = 0

        mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            @Suppress("DEPRECATION")
            MediaRecorder()
        }

        mediaRecorder?.apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setAudioSamplingRate(44100)
            setAudioEncodingBitRate(128000)
            setOutputFile(outputFile.absolutePath)
            prepare()
            start()
        }

        _state.value = RecordingState.RECORDING
    }

    fun pauseRecording() {
        if (_state.value == RecordingState.RECORDING && Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            mediaRecorder?.pause()
            pauseStartMs = System.currentTimeMillis()
            _state.value = RecordingState.PAUSED
        }
    }

    fun resumeRecording() {
        if (_state.value == RecordingState.PAUSED && Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            mediaRecorder?.resume()
            pausedDurationMs += System.currentTimeMillis() - pauseStartMs
            _state.value = RecordingState.RECORDING
        }
    }

    fun stopRecording(): Long {
        val finalDuration = if (_state.value == RecordingState.PAUSED) {
            pauseStartMs - startTimeMs - pausedDurationMs
        } else {
            System.currentTimeMillis() - startTimeMs - pausedDurationMs
        }

        try {
            mediaRecorder?.stop()
        } catch (_: Exception) {
        }

        try {
            mediaRecorder?.release()
        } catch (_: Exception) {
        }

        mediaRecorder = null
        _state.value = RecordingState.IDLE
        _currentPositionMs.value = finalDuration

        return finalDuration.coerceAtLeast(0)
    }

    fun getElapsedTimeMs(): Long {
        if (_state.value == RecordingState.IDLE) return 0
        if (_state.value == RecordingState.PAUSED) {
            return pauseStartMs - startTimeMs - pausedDurationMs
        }
        return System.currentTimeMillis() - startTimeMs - pausedDurationMs
    }

    fun release() {
        try {
            mediaRecorder?.stop()
        } catch (_: Exception) {
        }
        try {
            mediaRecorder?.release()
        } catch (_: Exception) {
        }
        mediaRecorder = null
        _state.value = RecordingState.IDLE
    }
}
