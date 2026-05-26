package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "distraction_timeline")
data class DistractionTimelineEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val focusStability: Int, // 0 to 100%
    val predictedCollapseTime: String, // e.g. "11:42 PM"
    val triggerReason: String, // e.g. "Typing Hesitation", "Erratic Scroll Rhythm", etc.
    val recoveryDurationSeconds: Int, // e.g. 42
    val distractionSource: String, // e.g. "Short-form Scroll", "Notification Reaction", etc.
    val isInterventionSuccessful: Boolean
)
