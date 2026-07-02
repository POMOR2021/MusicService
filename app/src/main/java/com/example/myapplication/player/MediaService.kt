package com.example.myapplication.player

import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService

class MediaService : MediaSessionService() {

    private var mediaSession: MediaSession? = null
    override fun onCreate(){
        super.onCreate()
        val player = PlayerProvider.getInstance(this).player
        mediaSession = MediaSession.Builder(this, player).build()
    }
    override fun onGetSession(p0: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    override fun onDestroy(){
        mediaSession?.release()
        mediaSession = null
        super.onDestroy()
    }
}