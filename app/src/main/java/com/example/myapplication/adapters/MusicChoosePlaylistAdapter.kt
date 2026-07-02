package com.example.myapplication.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.myapplication.R
import com.example.myapplication.databinding.ItemPlaylistTrackBinding
import com.example.myapplication.models.Track

class MusicChoosePlaylistAdapter :
    androidx.recyclerview.widget.ListAdapter<Track, MusicChoosePlaylistAdapter.TrackHolder>(
        DiffCallback
    ) {
    class TrackHolder(val binding: ItemPlaylistTrackBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        p1: Int
    ): TrackHolder {
        val binding = ItemPlaylistTrackBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        return TrackHolder(binding)
    }

    override fun onBindViewHolder(
        holder: TrackHolder,
        position: Int
    ) {
        val item = getItem(position)

        holder.binding.title.text = item.title
        holder.binding.artist.text = item.artist

        Glide.with(holder.itemView)
            .load(item.coverUri)
            .placeholder(R.drawable.default_album_art)
            .into(holder.binding.albumImage)
    }

    companion object {

        val DiffCallback = object : DiffUtil.ItemCallback<Track>() {

            override fun areItemsTheSame(oldItem: Track, newItem: Track): Boolean {
                return oldItem.filePath == newItem.filePath
            }

            override fun areContentsTheSame(oldItem: Track, newItem: Track): Boolean {
                return oldItem == newItem
            }
        }
    }
}