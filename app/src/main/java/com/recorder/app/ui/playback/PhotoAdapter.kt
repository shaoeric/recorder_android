package com.recorder.app.ui.playback

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.recorder.app.data.entity.Photo
import com.recorder.app.databinding.ItemPhotoBinding
import java.io.File

class PhotoAdapter(
    private val onPhotoClick: (Photo) -> Unit
) : RecyclerView.Adapter<PhotoAdapter.ViewHolder>() {

    private var photos: List<Photo> = emptyList()

    class ViewHolder(private val binding: ItemPhotoBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(photo: Photo, onPhotoClick: (Photo) -> Unit) {
            val file = File(photo.filePath)
            if (file.exists()) {
                binding.imagePhoto.setImageURI(Uri.fromFile(file))
            }
            binding.imagePhoto.setOnClickListener { onPhotoClick(photo) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemPhotoBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(photos[position], onPhotoClick)
    }

    override fun getItemCount(): Int = photos.size

    fun submitList(newPhotos: List<Photo>) {
        photos = newPhotos
        notifyDataSetChanged()
    }
}
