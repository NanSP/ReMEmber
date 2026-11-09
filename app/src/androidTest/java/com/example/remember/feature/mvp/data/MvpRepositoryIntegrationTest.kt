package com.example.remember.feature.mvp.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.remember.feature.goals.domain.model.GoalCategory
import com.example.remember.feature.mvp.data.local.RememberDatabase
import com.example.remember.feature.mvp.domain.AchievementId
import com.example.remember.feature.mvp.domain.BonusMissionCompletionResult
import com.example.remember.feature.mvp.domain.GamificationRules
import com.example.remember.feature.mvp.domain.GoalRecurrence
import com.example.remember.feature.mvp.domain.MvpGoal
import java.io.File
import java.util.UUID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.json.JSONObject

@RunWith(AndroidJUnit4::class)
class MvpRepositoryIntegrationTest {
    private lateinit var context: Context
    private lateinit var database: RememberDatabase
    private lateinit var dataStore: DataStore<Preferences>
    private lateinit var dataStoreScope: CoroutineScope
    private lateinit var dataStoreFile: File
    private lateinit var reminders: FakeReminderScheduler
    private var today = 20_100L
    private var nowMillis = 1_800_000_000_000L

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        database = Room.inMemoryDatabaseBuilder(context, RememberDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dataStoreScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        dataStoreFile = context.preferencesDataStoreFile("repository-${UUID.randomUUID()}")
        dataStore = PreferenceDataStoreFactory.create(
            corruptionHandler = ReplaceFileCorruptionHandler { emptyPreferences() },
            scope = dataStoreScope,
            produceFile = { dataStoreFile }
        )
        reminders = FakeReminderScheduler()
    }

    @After
    fun tearDown() {
        database.close()
        dataStoreScope.cancel()
        dataStoreFile.delete()
    }

    @Test
    fun createEditCompletePersistsRewardsStreakHistoryAndReminderState() = runBlocking {
        val repository = repository()
        repository.saveProfile("Integração", GoalCategory.STUDY, "body_03")
        repository.addGoal(
            title = "Estudar Kotlin",
            recurrence = GoalRecurrence.DAILY,
            reminderMinutesOfDay = 8 * 60
        )
        val created = repository.state.first { it.goals.size == 1 }.goals.single()
        assertEquals(GoalCategory.STUDY, created.category)

        repository.updateGoal(
            goalId = created.id,
            title = "Estudar Kotlin e Room",
            recurrence = GoalRecurrence.DAILY,
            reminderMinutesOfDay = 9 * 60
        )
        repository.completeGoal(created.id)

        val completed = repository.state.first { it.totalCompletions == 1 }
        assertEquals("Estudar Kotlin e Room", completed.goals.single().title)
        assertTrue(completed.goals.single().isCompleted)
        assertEquals(1, completed.currentStreak)
        assertEquals(1, completed.longestStreak)
        assertEquals(90, completed.experience)
        assertEquals(35, completed.coins)
        assertTrue(AchievementId.FIRST_COMPLETION in completed.unlockedAchievements)
        assertEquals(1, completed.completionHistory.size)
        assertEquals(40, completed.completionHistory.single().experienceEarned)
        assertEquals(15, completed.completionHistory.single().coinsEarned)
        assertEquals(9 * 60, completed.goals.single().reminderMinutesOfDay)
        assertTrue(reminders.synced.count { it.id == created.id } >= 3)

        repository.completeGoal(created.id)
        val unchanged = repository.state.first()
        assertEquals(1, unchanged.totalCompletions)
        assertEquals(1, unchanged.completionHistory.size)

        repository.deleteGoal(created.id)
        assertTrue(repository.state.first { it.goals.isEmpty() }.completionHistory.isNotEmpty())
        assertTrue(created.id in reminders.cancelled)
    }

    @Test
    fun pendingCompletionIsRecoveredOnceAfterFailureBetweenRoomAndDataStore() = runBlocking {
        var failOnce = true
        val interruptedRepository = repository(
            onCompletionStored = {
                if (failOnce) {
                    failOnce = false
                    error("Simulated process interruption")
                }
            }
        )
        interruptedRepository.saveProfile("Recuperação", GoalCategory.STUDY, "body_03")
        interruptedRepository.addGoal("Ler")
        val goal = interruptedRepository.state.first { it.goals.isNotEmpty() }.goals.single()

        assertTrue(runCatching { interruptedRepository.completeGoal(goal.id) }.isFailure)
        assertEquals(1, database.goalCompletionDao().count())

        val recoveredRepository = repository()
        val recovered = recoveredRepository.state.first { it.totalCompletions == 1 }
        assertEquals(1, recovered.completionHistory.size)
        assertEquals(90, recovered.experience)
        assertEquals(35, recovered.coins)
        assertEquals(1, recovered.currentStreak)

        recoveredRepository.completeGoal(goal.id)
        val afterRetry = recoveredRepository.state.first()
        assertEquals(1, afterRetry.totalCompletions)
        assertEquals(1, afterRetry.completionHistory.size)
        assertEquals(90, afterRetry.experience)
        assertEquals(35, afterRetry.coins)
    }

    @Test
    fun timedBonusMissionMustStartAndReachItsDurationBeforeCompletion() = runBlocking {
        val repository = repository()
        repository.saveProfile("Cronômetro", GoalCategory.STUDY, "body_03")

        assertEquals(
            BonusMissionCompletionResult.NOT_STARTED,
            repository.completeBonusMission()
        )
        assertTrue(repository.startBonusMission())
        val running = repository.state.first { it.bonusMission.isStarted }.bonusMission
        assertEquals(nowMillis, running.startedAtMillis)
        assertEquals(
            BonusMissionCompletionResult.TIMER_RUNNING,
            repository.completeBonusMission()
        )

        nowMillis += GamificationRules.STUDY_BONUS_DURATION_MILLIS
        assertEquals(
            BonusMissionCompletionResult.COMPLETED,
            repository.completeBonusMission()
        )
        val completed = repository.state.first { it.bonusMission.completed }
        assertEquals(20, completed.experience)
        assertEquals(10, completed.coins)
        assertEquals(
            BonusMissionCompletionResult.ALREADY_COMPLETED,
            repository.completeBonusMission()
        )
    }

    @Test
    fun privacyControlsExportCorrectAndDeleteAllLocalData() = runBlocking {
        val repository = repository()
        repository.saveProfile("Nome original", GoalCategory.STUDY, "body_03")
        repository.addGoal("Objetivo privado")
        val populated = repository.state.first { it.goals.isNotEmpty() }

        assertEquals("1.0", populated.privacyNoticeVersion)
        assertTrue(populated.privacyNoticeAcknowledgedAtMillis != null)
        val exported = JSONObject(PrivacyDataExporter.toJson(populated, nowMillis))
        assertEquals("Nome original", exported.getJSONObject("profile").getString("name"))
        assertEquals(1, exported.getJSONArray("goals").length())

        repository.updateProfileName("Nome corrigido")
        assertEquals(
            "Nome corrigido",
            repository.state.first { it.profileName == "Nome corrigido" }.profileName
        )

        val goalId = populated.goals.single().id
        repository.deleteAllUserData()
        val deleted = repository.state.first { !it.isOnboardingComplete }
        assertTrue(deleted.profileName.isBlank())
        assertTrue(deleted.goals.isEmpty())
        assertTrue(deleted.completionHistory.isEmpty())
        assertFalse(deleted.privacyNoticeAcknowledgedAtMillis != null)
        assertTrue(goalId in reminders.cancelled)
    }

    private fun repository(
        onCompletionStored: suspend () -> Unit = {}
    ) = MvpRepository(
        context = context,
        currentEpochDay = { today },
        currentLocalHour = { 10 },
        currentTimeMillis = { nowMillis },
        database = database,
        dataStore = dataStore,
        reminderScheduler = reminders,
        onCompletionStored = onCompletionStored
    )

    private class FakeReminderScheduler : ReminderScheduler {
        val synced = mutableListOf<MvpGoal>()
        val cancelled = mutableListOf<Long>()

        override fun sync(goal: MvpGoal) {
            synced += goal
        }

        override fun cancel(goalId: Long) {
            cancelled += goalId
        }
    }
}
