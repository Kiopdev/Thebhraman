package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "attention_profile")
data class AttentionProfileEntity(
    @PrimaryKey val id: Int = 1, // Single profile row
    val stabilityStreak: Int = 0,
    val maxStreak: Int = 0,
    val totalConfronted: Int = 0,
    val totalDefeated: Int = 0,
    val selectedInterventionMode: String = "tactile_shifter" // "tactile_shifter", "neural_mesh", "icon_scramble"
)
