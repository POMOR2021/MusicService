package com.example.myapplication.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.myapplication.dao.TrackDao
import com.example.myapplication.models.Track

@Database(entities = [Track::class], version = 2)
abstract class TrackDatabase : RoomDatabase() {
    abstract fun trackDao(): TrackDao

    companion object{
        @Volatile
        private var INSTANCE: TrackDatabase? = null

        fun getDatabase(context: Context): TrackDatabase{
            return INSTANCE?: synchronized(this){

                val instance = Room.databaseBuilder(
                    context = context.applicationContext,
                    TrackDatabase::class.java,
                    "track_db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}