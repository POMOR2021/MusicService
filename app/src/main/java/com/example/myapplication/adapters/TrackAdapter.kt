package com.example.myapplication.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.os.trace
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.myapplication.R
import com.example.myapplication.databinding.ItemTrackBinding
import com.example.myapplication.models.Track

class TrackAdapter(
    private val onClick: (Track) -> Unit,
    private val onFavoriteClick: (Track) -> Unit
) : androidx.recyclerview.widget.ListAdapter<Track, TrackAdapter.TrackHolder>(DiffCallback) {

    var playingTrackPath: String? = null
    var isPlayerRunning: Boolean = false
    class TrackHolder(val binding: ItemTrackBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        p1: Int
    ): TrackHolder {
        val binding = ItemTrackBinding.inflate(
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

        val icon =
        if (item.isFavorite)
            R.drawable.icon_favorite_list_is_liked
        else
            R.drawable.icon_favorite_list_is_not_liked
        holder.binding.favoriteButton.setImageResource(icon)

        Glide.with(holder.itemView)
            .load(item.coverUri)
            .placeholder(R.drawable.default_album_art)
            .into(holder.binding.albumImage)

        holder.itemView.setOnClickListener {
            onClick(item)
        }
        holder.binding.favoriteButton.setOnClickListener {
            onFavoriteClick(item)
        }
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



