package com.recorder.app.ui.playback

import android.app.Application
import android.media.MediaPlayer
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.recorder.app.RecorderApplication
import com.recorder.app.data.entity.Photo
import com.recorder.app.data.entity.Recording
import com.recorder.app.data.entity.TimeMarker
import com.recorder.app.data.repository.RecordingRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class PlaybackViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as RecorderApplication
    private val repository: RecordingRepository

    val recording = MutableLiveData<Recording>()
    val markers = MutableLiveData<List<TimeMarker>>(emptyList())
    val photos = MutableLiveData<List<Photo>>(emptyList())
    val currentPositionMs = MutableLiveData(0L)
    val durationMs = MutableLiveData(0L)
    val isPlaying = MutableLiveData(false)

    private var mediaPlayer: MediaPlayer? = null
    private var updateJob: Job? = null

    init {
        val db = app.database
        repository = RecordingRepository(
            db.recordingDao(),
            db.markerDao(),
            db.photoDao()
        )
    }

    fun loadRecording(recordingId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            val rec = repository.getRecordingById(recordingId)
            rec?.let {
                launch(Dispatchers.Main) {
                    recording.value = it
                    durationMs.value = it.durationMs
                }
            }

            val markerList = repository.getMarkersForRecording(recordingId)
            launch(Dispatchers.Main) {
                markers.value = markerList
            }

            val photoList = repository.getPhotosForRecording(recordingId)
            launch(Dispatchers.Main) {
                photos.value = photoList
            }
        }
    }

    fun initMediaPlayer(filePath: String) {
        mediaPlayer?.release()
        mediaPlayer = MediaPlayer().apply {
            setDataSource(filePath)
            prepare()
        }
    }

    fun play() {
        mediaPlayer?.let { mp ->
            if (!mp.isPlaying) {
                mp.start()
                isPlaying.value = true
                startPositionUpdate()
            }
        }
    }

    fun pause() {
        mediaPlayer?.let { mp ->
            if (mp.isPlaying) {
                mp.pause()
                isPlaying.value = false
                stopPositionUpdate()
            }
        }
    }

    fun togglePlayPause() {
        mediaPlayer?.let { mp ->
            if (mp.isPlaying) {
                pause()
            } else {
                play()
            }
        }
    }

    fun forward(seconds: Int = 3) {
        mediaPlayer?.let { mp ->
            val newPos = (mp.currentPosition + seconds * 1000L).coerceAtMost(mp.duration.toLong())
            mp.seekTo(newPos.toInt())
            currentPositionMs.value = newPos
        }
    }

    fun rewind(seconds: Int = 3) {
        mediaPlayer?.let { mp ->
            val newPos = (mp.currentPosition - seconds * 1000L).coerceAtLeast(0)
            mp.seekTo(newPos.toInt())
            currentPositionMs.value = newPos
        }
    }

    fun seekTo(positionMs: Long) {
        mediaPlayer?.let { mp ->
            val pos = positionMs.coerceIn(0, mp.duration.toLong())
            mp.seekTo(pos.toInt())
            currentPositionMs.value = pos
        }
    }

    fun getCurrentPositionMsDirect(): Long {
        return mediaPlayer?.currentPosition?.toLong() ?: 0L
    }

    private fun startPositionUpdate() {
        stopPositionUpdate()
        updateJob = viewModelScope.launch {
            while (isActive) {
                mediaPlayer?.let { mp ->
                    if (mp.isPlaying) {
                        val pos = mp.currentPosition.toLong()
                        currentPositionMs.postValue(pos)
                    } else {
                        stopPositionUpdate()
                        isPlaying.postValue(false)
                    }
                }
                delay(100)
            }
        }
    }

    private fun stopPositionUpdate() {
        updateJob?.cancel()
        updateJob = null
    }

    override fun onCleared() {
        super.onCleared()
        stopPositionUpdate()
        mediaPlayer?.release()
        mediaPlayer = null
    }
}
