package com.example.remember.feature.goals.domain.repository

import com.example.remember.feature.goals.domain.model.Goal
import kotlinx.coroutines.flow.Flow

interface GoalRepository {
    fun observeGoals(): Flow<List<Goal>>

    suspend fun save(goal: Goal)

    suspend fun delete(goalId: Long)
}
