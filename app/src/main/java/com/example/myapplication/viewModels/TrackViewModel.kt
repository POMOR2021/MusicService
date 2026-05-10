package com.example.myapplication.viewModels

import com.example.myapplication.player.PlayerProvider
import android.app.Application
import android.content.ComponentName
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.example.myapplication.db.TrackDatabase
import com.example.myapplication.models.Track
import com.example.myapplication.player.MediaService
import com.example.myapplication.repositories.TrackRepository
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class TrackViewModel(application: Application) : AndroidViewModel(application) {
    private val player = PlayerProvider.getInstance(application.applicationContext)
    private val repository: TrackRepository
    val tracks: StateFlow<List<Track>>
    val favoriteTracks: StateFlow<List<Track>>
    private var mediaController: MediaController? = null
    private var controllerFuture: ListenableFuture<MediaController>? = null
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying

    private val _currentTrack = MutableStateFlow<Track?>(null)
    val currentTrack: StateFlow<Track?> = _currentTrack
    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition: StateFlow<Long> = _currentPosition
    private var progressJob: Job? = null

    init {
        val dao = TrackDatabase.getDatabase(application).trackDao()
        repository = TrackRepository(dao)

        tracks = repository.allTracks.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        favoriteTracks = repository.allTrackFavorite.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    }

    fun initMediaController(context: Context) {

        val sessionToken = SessionToken(
            context,
            ComponentName(context, MediaService::class.java)
        )

        controllerFuture =
            MediaController.Builder(context, sessionToken).buildAsync()

        controllerFuture?.addListener({
            val controller = controllerFuture?.get() ?: return@addListener
            this.mediaController = controller

            controller.addListener(object : Player.Listener {
                override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {

                    _currentTrack.value = findTrackByUri(mediaItem?.mediaId)
                }

                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    _isPlaying.value = isPlaying
                    if (isPlaying) startUpdatingProgress() else progressJob?.cancel()
                }
            })


            _isPlaying.value = controller.isPlaying
            _currentTrack.value = findTrackByUri(controller.currentMediaItem?.mediaId)
            if (controller.isPlaying) startUpdatingProgress()
        }, MoreExecutors.directExecutor())
    }

    fun insertTrack(track: Track) {
        viewModelScope.launch {
            repository.insertTrack(track)
        }
    }

    fun deleteTracks(track: Track) {
        viewModelScope.launch {
            repository.deleteTrack(track)
        }
    }

    fun playTrack(selectedTrack: Track) {
        _isPlaying.value = true
        _currentTrack.value = selectedTrack
        val trackList = tracks.value
        val index = trackList.indexOf(selectedTrack)
        player.setPlaylist(trackList, index)
        startUpdatingProgress()
    }

    fun togglePlayPause() {
        val controller = mediaController ?: return
        if (controller.isPlaying) {
            controller.pause()
        } else {
            controller.play()
        }
    }

    fun toggleFavorite(track: Track) {
        viewModelScope.launch {
            val updatedTrack = track.copy(isFavorite = !track.isFavorite)
            repository.updateTrack(updatedTrack)
        }
    }

    private fun startUpdatingProgress() {
        progressJob?.cancel()

        progressJob = viewModelScope.launch {
            while (isActive && _isPlaying.value) {
                _currentPosition.value = player.getCurrentPosition()
                delay(60)
            }
        }
    }

    private fun findTrackByUri(uri: String?): Track? {
        if (uri == null)
            return null
        return tracks.value.find { it.id.toString() == uri }
    }

    fun seekToNextMediaItem() {
        mediaController?.seekToNextMediaItem()
    }

    fun seekToPreviousMediaItem() {
        mediaController?.seekToPreviousMediaItem()
    }

}