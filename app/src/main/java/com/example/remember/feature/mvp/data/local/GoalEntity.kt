package com.example.remember.feature.mvp.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "goals")
data class GoalEntity(
    @PrimaryKey val id: Long,
    val title: String,
    val category: String,
    val isCompleted: Boolean,
    val recurrence: String,
    val lastCompletedEpochDay: Long?,
    val reminderMinutesOfDay: Int?,
    val reminderEnabled: Boolean,
    val createdAtMillis: Long
)
