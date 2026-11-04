package com.example.remember.feature.mvp.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.remember.feature.goals.domain.model.GoalCategory
import com.example.remember.feature.mvp.domain.GoalRecurrence
import com.example.remember.feature.mvp.domain.MvpGoal
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RememberDatabaseTest {
    private lateinit var database: RememberDatabase

    @Before
    fun createDatabase() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RememberDatabase::class.java
        ).allowMainThreadQueries().build()
    }

    @After
    fun closeDatabase() {
        database.close()
    }

    @Test
    fun goalCanBeInsertedUpdatedAndObserved() = runBlocking {
        val goal = MvpGoal(
            id = 10,
            title = "Estudar Room",
            category = GoalCategory.STUDY,
            recurrence = GoalRecurrence.WEEKLY
        )
        database.goalDao().upsert(goal.toEntity())

        val updated = goal.copy(title = "Estudar Room e SQLite")
        database.goalDao().update(updated.toEntity())

        assertEquals(listOf(updated), database.goalDao().observeAll().first().mapNotNull {
            it.toDomain()
        })
    }

    @Test
    fun completionHistorySurvivesGoalDeletion() = runBlocking {
        val goal = MvpGoal(11, "Caminhar", GoalCategory.EXERCISE)
        database.goalDao().upsert(goal.toEntity())
        database.goalCompletionDao().insert(
            GoalCompletionEntity(
                goalId = goal.id,
                goalTitle = goal.title,
                category = goal.category.name,
                completedEpochDay = 20_001,
                completedAtMillis = 1_700_000_000_000,
                experienceEarned = 10,
                coinsEarned = 2
            )
        )

        database.goalDao().deleteById(goal.id)

        assertTrue(database.goalDao().getAll().isEmpty())
        assertEquals(1, database.goalCompletionDao().getForGoal(goal.id).size)
    }

    @Test
    fun recurringGoalKeepsEachCompletionAsASeparateRecord() = runBlocking {
        val goalId = 12L
        repeat(2) { offset ->
            database.goalCompletionDao().insert(
                GoalCompletionEntity(
                    goalId = goalId,
                    goalTitle = "Ler",
                    category = GoalCategory.STUDY.name,
                    completedEpochDay = 20_010L + offset,
                    completedAtMillis = 1_700_000_000_000L + offset,
                    experienceEarned = 10,
                    coinsEarned = 2
                )
            )
        }

        assertEquals(2, database.goalCompletionDao().getForGoal(goalId).size)
    }
}
