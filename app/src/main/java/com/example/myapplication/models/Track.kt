package com.example.myapplication.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tracks")
data class Track(
    val title: String,
    val artist: String,
    val coverUri: String?,
    val filePath: String,
    val isFavorite: Boolean,
    val durationMs: Long = 0L,
    @PrimaryKey(autoGenerate = true) val id: Int = 0
)
