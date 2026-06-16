package com.recorder.app.ui.playback

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.recorder.app.R
import com.recorder.app.data.entity.Photo
import com.recorder.app.databinding.ActivityPlaybackBinding
import com.recorder.app.util.TimeUtils

class PlaybackActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_RECORDING_ID = "extra_recording_id"
    }

    private lateinit var binding: ActivityPlaybackBinding
    private lateinit var viewModel: PlaybackViewModel
    private lateinit var photoAdapter: PhotoAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlaybackBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[PlaybackViewModel::class.java]

        val recordingId = intent.getLongExtra(EXTRA_RECORDING_ID, -1)
        if (recordingId == -1L) {
            finish()
            return
        }

        setupPhotoRecyclerView()
        setupObservers()
        setupClickListeners()

        viewModel.loadRecording(recordingId)
    }

    private fun setupPhotoRecyclerView() {
        photoAdapter = PhotoAdapter { photo ->
            showFullScreenPhoto(photo)
        }

        binding.recyclerPhotos.apply {
            layoutManager = LinearLayoutManager(
                this@PlaybackActivity,
                LinearLayoutManager.HORIZONTAL,
                false
            )
            adapter = photoAdapter
        }
    }

    private fun setupObservers() {
        viewModel.recording.observe(this) { recording ->
            binding.textFilename.text = recording.fileName
            binding.textTotalTime.text = TimeUtils.formatDuration(recording.durationMs)
            viewModel.initMediaPlayer(recording.filePath)
            viewModel.play()
        }

        viewModel.isPlaying.observe(this) { playing ->
            if (playing) {
                binding.btnPlayPause.setImageResource(R.drawable.ic_pause)
            } else {
                binding.btnPlayPause.setImageResource(R.drawable.ic_play)
            }
        }

        viewModel.currentPositionMs.observe(this) { positionMs ->
            binding.textCurrentTime.text = TimeUtils.formatDuration(positionMs)

            val totalMs = viewModel.durationMs.value ?: 1
            val progress = if (totalMs > 0) {
                ((positionMs.toFloat() / totalMs.toFloat()) * 1000).toInt()
            } else {
                0
            }
            binding.seekbar.progress = progress

            updateProgressBar(positionMs)
        }

        viewModel.durationMs.observe(this) { _ ->
            updateMarkers()
            updatePhotoDots()
        }

        viewModel.markers.observe(this) {
            updateMarkers()
        }

        viewModel.photos.observe(this) { photos ->
            photoAdapter.submitList(photos)
            updatePhotoDots()
        }
    }

    private fun setupClickListeners() {
        binding.btnPlayPause.setOnClickListener {
            viewModel.togglePlayPause()
        }

        binding.btnForward.setOnClickListener {
            viewModel.forward(3)
        }

        binding.btnRewind.setOnClickListener {
            viewModel.rewind(3)
        }

        binding.seekbar.setOnSeekBarChangeListener(object :
            android.widget.SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: android.widget.SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    val totalMs = viewModel.durationMs.value ?: return
                    val positionMs = (progress.toLong() * totalMs / 1000)
                    binding.textCurrentTime.text = TimeUtils.formatDuration(positionMs)
                }
            }

            override fun onStartTrackingTouch(seekBar: android.widget.SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: android.widget.SeekBar?) {
                val progress = seekBar?.progress ?: return
                val totalMs = viewModel.durationMs.value ?: return
                val positionMs = (progress.toLong() * totalMs / 1000)
                viewModel.seekTo(positionMs)
            }
        })
    }

    private fun updateProgressBar(currentPositionMs: Long) {
        val totalMs = viewModel.durationMs.value ?: return
        if (totalMs <= 0) return

        val progressWidth = binding.layoutProgress.width
        if (progressWidth <= 0) return

        val fraction = currentPositionMs.toFloat() / totalMs.toFloat()
        val playedWidth = (fraction * progressWidth).toInt()

        val playedParams = binding.progressPlayed.layoutParams
        playedParams.width = playedWidth
        binding.progressPlayed.layoutParams = playedParams
    }

    private fun updateMarkers() {
        val totalMs = viewModel.durationMs.value ?: return
        if (totalMs <= 0) return

        binding.layoutMarkers.removeAllViews()
        binding.layoutMarkers.post {
            val progressWidth = binding.layoutProgress.width
            if (progressWidth <= 0) return@post

            val markers = viewModel.markers.value ?: return@post
            for (marker in markers) {
                val fraction = marker.positionMs.toFloat() / totalMs.toFloat()
                val x = (fraction * progressWidth).toInt()

                val dot = ImageView(this)
                val size = 24
                val params = LinearLayout.LayoutParams(size, size)
                params.marginStart = x - size / 2
                dot.layoutParams = params
                dot.setImageResource(R.drawable.ic_marker_dot)
                dot.setOnClickListener {
                    viewModel.seekTo(marker.positionMs)
                }
                binding.layoutMarkers.addView(dot)
            }
        }
    }

    private fun updatePhotoDots() {
        val totalMs = viewModel.durationMs.value ?: return
        if (totalMs <= 0) return

        binding.layoutPhotoDots.removeAllViews()
        binding.layoutPhotoDots.post {
            val progressWidth = binding.layoutProgress.width
            if (progressWidth <= 0) return@post

            val photos = viewModel.photos.value ?: return@post
            for (photo in photos) {
                val fraction = photo.positionMs.toFloat() / totalMs.toFloat()
                val x = (fraction * progressWidth).toInt()

                val dot = ImageView(this)
                val size = 24
                val params = LinearLayout.LayoutParams(size, size)
                params.topMargin = 36
                params.marginStart = x - size / 2
                dot.layoutParams = params
                dot.setImageResource(R.drawable.ic_photo_dot)
                dot.setOnClickListener {
                    viewModel.seekTo(photo.positionMs)
                    showFullScreenPhoto(photo)
                }
                binding.layoutPhotoDots.addView(dot)
            }
        }
    }

    private fun showFullScreenPhoto(photo: Photo) {
        viewModel.seekTo(photo.positionMs)
        val intent = Intent(this, PhotoFullscreenActivity::class.java).apply {
            putExtra(PhotoFullscreenActivity.EXTRA_PHOTO_PATH, photo.filePath)
        }
        startActivity(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.pause()
    }
}
