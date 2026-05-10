package com.example.myapplication.player

import android.content.Context
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import com.example.myapplication.models.Track

class PlayerManager(private val context: Context) {

    val player = ExoPlayer.Builder(context).build()

    var onTrackChanged: ((Int) -> Unit)? = null

    init {
        player.setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(C.USAGE_MEDIA)
                .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                .build(),
            true
        )

        player.addListener(object : androidx.media3.common.Player.Listener {
            override fun onMediaItemTransition(
                mediaItem: MediaItem?,
                reason: Int
            ) {
                onTrackChanged?.invoke(player.currentMediaItemIndex)
            }
        })
    }

    fun setPlaylist(tracks: List<Track>, startIndex: Int) {
        val items = tracks.map { track ->
            MediaItem.Builder()
                .setUri(track.filePath)
                .setMediaId(track.id.toString())
                .setMediaMetadata(
                    androidx.media3.common.MediaMetadata.Builder()
                        .setTitle(track.title)
                        .setArtist(track.artist)
                        .build()
                )
                .build()
        }

        player.setMediaItems(items)
        player.seekTo(startIndex, 0L)
        player.prepare()
        player.playWhenReady = true
    }

    fun getCurrentPosition(): Long {
        return player.currentPosition
    }

    fun getDuration(): Long {
        return if (player.duration < 0) 0 else player.duration
    }
}

object PlayerProvider {
    private var instance: PlayerManager? = null

    fun getInstance(context: Context): PlayerManager {
        return instance ?: PlayerManager(context.applicationContext).also {
            instance = it
        }
    }
}

