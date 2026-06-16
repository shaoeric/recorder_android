package com.recorder.app.ui.main

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.TakePicture
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.recorder.app.data.entity.Recording
import com.recorder.app.databinding.ActivityMainBinding
import com.recorder.app.service.RecordingState
import com.recorder.app.ui.playback.PlaybackActivity
import com.recorder.app.util.FileUtils
import java.io.File

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainViewModel
    private lateinit var adapter: RecordingAdapter

    private var isLongPressing = false
    private var longPressStartTime = 0L
    private var stopTriggered = false

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val allGranted = permissions.values.all { it }
            if (!allGranted) {
                Toast.makeText(this, "需要录音和相机权限才能使用全部功能", Toast.LENGTH_LONG).show()
            }
        }

    private val takePhotoLauncher =
        registerForActivityResult(TakePicture()) { success ->
            if (success && viewModel.getCurrentRecordingId() > 0) {
                currentPhotoFile?.let { file ->
                    viewModel.addPhoto(file.absolutePath)
                }
            }
        }

    private var currentPhotoFile: File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[MainViewModel::class.java]
        setupRecyclerView()
        setupObservers()
        setupClickListeners()

        requestPermissions()
    }

    private fun setupRecyclerView() {
        adapter = RecordingAdapter { recording ->
            val intent = Intent(this, PlaybackActivity::class.java).apply {
                putExtra(PlaybackActivity.EXTRA_RECORDING_ID, recording.id)
            }
            startActivity(intent)
        }

        binding.recyclerRecordings.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = this@MainActivity.adapter
        }
    }

    private fun setupObservers() {
        viewModel.allRecordings.observe(this) { recordings ->
            adapter.submitList(recordings)
            binding.textEmpty.visibility =
                if (recordings.isEmpty()) View.VISIBLE else View.GONE
        }

        viewModel.recordingState.observe(this) { state ->
            updateControlButtons(state)
        }
    }

    private fun setupClickListeners() {
        binding.btnRecord.setOnTouchListener { view, event ->
            when (event.action) {
                android.view.MotionEvent.ACTION_DOWN -> {
                    if (viewModel.recordingManager.state.value == RecordingState.PAUSED) {
                        isLongPressing = true
                        stopTriggered = false
                        longPressStartTime = System.currentTimeMillis()
                        view.postDelayed({
                            if (isLongPressing && !stopTriggered) {
                                stopTriggered = true
                                viewModel.stopRecording()
                            }
                        }, 3000)
                    }
                    true
                }
                android.view.MotionEvent.ACTION_UP, android.view.MotionEvent.ACTION_CANCEL -> {
                    isLongPressing = false
                    if (viewModel.recordingManager.state.value == RecordingState.PAUSED
                        && !stopTriggered
                    ) {
                        viewModel.resumeRecording()
                    } else if (viewModel.recordingManager.state.value == RecordingState.IDLE
                        && !stopTriggered
                    ) {
                        viewModel.startRecording()
                    } else if (viewModel.recordingManager.state.value == RecordingState.RECORDING
                        && !stopTriggered
                    ) {
                        viewModel.pauseRecording()
                    }
                    true
                }
                else -> false
            }
        }

        binding.btnMark.setOnClickListener {
            viewModel.addTimeMarker()
        }

        binding.btnPhoto.setOnClickListener {
            takePhoto()
        }
    }

    private fun updateControlButtons(state: RecordingState) {
        when (state) {
            RecordingState.IDLE -> {
                binding.btnRecord.setImageResource(com.recorder.app.R.drawable.ic_mic)
                binding.btnMark.visibility = View.GONE
                binding.btnPhoto.visibility = View.GONE
            }
            RecordingState.RECORDING -> {
                binding.btnRecord.setImageResource(com.recorder.app.R.drawable.ic_pause)
                binding.btnMark.visibility = View.VISIBLE
                binding.btnPhoto.visibility = View.VISIBLE
            }
            RecordingState.PAUSED -> {
                binding.btnRecord.setImageResource(com.recorder.app.R.drawable.ic_stop)
                binding.btnMark.visibility = View.VISIBLE
                binding.btnPhoto.visibility = View.VISIBLE
            }
        }
    }

    private fun takePhoto() {
        val photoFile = File(
            FileUtils.getPhotosDir(this),
            FileUtils.generatePhotoFileName()
        )
        currentPhotoFile = photoFile

        val uri = FileProvider.getUriForFile(
            this,
            "${packageName}.fileprovider",
            photoFile
        )

        takePhotoLauncher.launch(uri)
    }

    private fun requestPermissions() {
        val permissions = mutableListOf<String>()
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED
        ) {
            permissions.add(Manifest.permission.RECORD_AUDIO)
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED
        ) {
            permissions.add(Manifest.permission.CAMERA)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                permissions.add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
        if (permissions.isNotEmpty()) {
            requestPermissionLauncher.launch(permissions.toTypedArray())
        }
    }
}
