package com.recorder.app.ui.main

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.recorder.app.data.entity.Recording
import com.recorder.app.databinding.ItemRecordingBinding
import com.recorder.app.util.TimeUtils

class RecordingAdapter(
    private val onItemClick: (Recording) -> Unit
) : ListAdapter<Recording, RecordingAdapter.ViewHolder>(DiffCallback()) {

    class ViewHolder(private val binding: ItemRecordingBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(recording: Recording, onItemClick: (Recording) -> Unit) {
            binding.textFilename.text = recording.fileName
            binding.textDuration.text = TimeUtils.formatDuration(recording.durationMs)
            binding.textSize.text = TimeUtils.formatFileSize(recording.fileSizeBytes)
            binding.textDate.text = TimeUtils.formatDate(recording.createdAt)
            binding.root.setOnClickListener { onItemClick(recording) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemRecordingBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), onItemClick)
    }

    class DiffCallback : DiffUtil.ItemCallback<Recording>() {
        override fun areItemsTheSame(oldItem: Recording, newItem: Recording): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Recording, newItem: Recording): Boolean =
            oldItem == newItem
    }
}
