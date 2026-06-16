package com.recorder.app.ui.playback

import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.recorder.app.databinding.ActivityPhotoFullscreenBinding
import java.io.File

class PhotoFullscreenActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_PHOTO_PATH = "extra_photo_path"
    }

    private lateinit var binding: ActivityPhotoFullscreenBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPhotoFullscreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val photoPath = intent.getStringExtra(EXTRA_PHOTO_PATH)
        photoPath?.let { path ->
            val file = File(path)
            if (file.exists()) {
                binding.imageFullscreen.setImageURI(Uri.fromFile(file))
            }
        }

        binding.btnClose.setOnClickListener {
            finish()
        }
    }
}
