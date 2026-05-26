package com.example.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.AttentionProfileEntity
import com.example.data.model.DistractionTimelineEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AttentionDao {
    @Query("SELECT * FROM distraction_timeline ORDER BY timestamp DESC")
    fun getAllEvents(): Flow<List<DistractionTimelineEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: DistractionTimelineEntity)

    @Query("DELETE FROM distraction_timeline")
    suspend fun clearTimeline()

    @Query("SELECT * FROM attention_profile WHERE id = 1")
    fun getProfileFlow(): Flow<AttentionProfileEntity?>

    @Query("SELECT * FROM attention_profile WHERE id = 1")
    suspend fun getProfile(): AttentionProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveProfile(profile: AttentionProfileEntity)
}
