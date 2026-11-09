package com.example.remember.feature.mvp.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface GoalCompletionDao {
    @Insert
    suspend fun insert(completion: GoalCompletionEntity): Long

    @Query("SELECT * FROM goal_completions ORDER BY completedAtMillis DESC, id DESC")
    fun observeAll(): Flow<List<GoalCompletionEntity>>

    @Query("SELECT * FROM goal_completions WHERE goalId = :goalId ORDER BY completedAtMillis DESC")
    suspend fun getForGoal(goalId: Long): List<GoalCompletionEntity>

    @Query("SELECT * FROM goal_completions WHERE goalId = :goalId AND completedEpochDay = :epochDay LIMIT 1")
    suspend fun find(goalId: Long, epochDay: Long): GoalCompletionEntity?

    @Query("SELECT COUNT(*) FROM goal_completions")
    suspend fun count(): Int

    @Query("DELETE FROM goal_completions")
    suspend fun deleteAll()
}
