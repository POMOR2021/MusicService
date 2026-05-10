package com.example.myapplication.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.myapplication.models.Track
import kotlinx.coroutines.flow.Flow

@Dao
interface TrackDao {
    @Query("SELECT * FROM tracks")
    fun getAllTracks() : Flow<List<Track>>

    @Query("SELECT * FROM tracks WHERE isFavorite = true")
    fun getTrackByIsFavorite() : Flow<List<Track>>
    @Insert
    suspend fun insertTrack(track: Track)

    @Delete
    suspend fun deleteTrack(track: Track)

    @Update
    suspend fun updateTrack(track: Track)



}