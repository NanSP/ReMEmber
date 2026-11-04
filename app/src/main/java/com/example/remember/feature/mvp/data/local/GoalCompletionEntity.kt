package com.example.remember.feature.mvp.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "goal_completions",
    indices = [Index("goalId"), Index("completedEpochDay")]
)
data class GoalCompletionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val goalId: Long,
    val goalTitle: String,
    val category: String,
    val completedEpochDay: Long,
    val completedAtMillis: Long,
    val experienceEarned: Int,
    val coinsEarned: Int
)
