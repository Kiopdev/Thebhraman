package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.AttentionProfileEntity
import com.example.data.model.DistractionTimelineEntity

@Database(
    entities = [DistractionTimelineEntity::class, AttentionProfileEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AttentionDatabase : RoomDatabase() {
    abstract fun attentionDao(): AttentionDao

    companion object {
        @Volatile
        private var INSTANCE: AttentionDatabase? = null

        fun getDatabase(context: Context): AttentionDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AttentionDatabase::class.java,
                    "attention_monitoring_db"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
