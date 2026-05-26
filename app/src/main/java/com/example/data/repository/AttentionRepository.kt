package com.example.data.repository

import com.example.data.database.AttentionDao
import com.example.data.model.AttentionProfileEntity
import com.example.data.model.DistractionTimelineEntity
import kotlinx.coroutines.flow.Flow

class AttentionRepository(private val attentionDao: AttentionDao) {
    val allEvents: Flow<List<DistractionTimelineEntity>> = attentionDao.getAllEvents()
    val profileFlow: Flow<AttentionProfileEntity?> = attentionDao.getProfileFlow()

    suspend fun insertEvent(event: DistractionTimelineEntity) {
        attentionDao.insertEvent(event)
    }

    suspend fun clearTimeline() {
        attentionDao.clearTimeline()
    }

    suspend fun getOrCreateProfile(): AttentionProfileEntity {
        val existing = attentionDao.getProfile()
        if (existing != null) {
            return existing
        }
        val defaultProfile = AttentionProfileEntity()
        attentionDao.saveProfile(defaultProfile)
        return defaultProfile
    }

    suspend fun saveProfile(profile: AttentionProfileEntity) {
        attentionDao.saveProfile(profile)
    }
}
