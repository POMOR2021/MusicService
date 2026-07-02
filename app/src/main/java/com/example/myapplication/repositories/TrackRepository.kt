package com.example.myapplication.repositories

import com.example.myapplication.dao.TrackDao
import com.example.myapplication.models.Track
import kotlinx.coroutines.flow.Flow

class TrackRepository(
    private val trackDao: TrackDao
){
    val allTracks: Flow<List<Track>> = trackDao.getAllTracks()
    suspend fun insertTrack(track: Track){
        trackDao.insertTrack(track)
    }
    suspend fun deleteTrack(track:Track){
        trackDao.deleteTrack(track)
    }
    suspend fun updateTrack(track: Track){
        trackDao.updateTrack(track)
    }
    val allTrackFavorite = trackDao.getTrackByIsFavorite()
}