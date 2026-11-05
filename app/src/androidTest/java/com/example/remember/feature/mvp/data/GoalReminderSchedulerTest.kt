package com.example.remember.feature.mvp.data

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.remember.feature.goals.domain.model.GoalCategory
import com.example.remember.feature.mvp.domain.GoalRecurrence
import com.example.remember.feature.mvp.domain.MvpGoal
import java.time.LocalTime
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GoalReminderSchedulerTest {
    @Test
    fun newReminderCanBeScheduledAndCancelledWithoutExistingPendingIntent() {
        val scheduler = GoalReminderScheduler(ApplicationProvider.getApplicationContext())
        val goalId = System.currentTimeMillis()
        val nextMinute = LocalTime.now().plusMinutes(2)
        val goal = MvpGoal(
            id = goalId,
            title = "Teste de lembrete",
            category = GoalCategory.STUDY,
            recurrence = GoalRecurrence.DAILY,
            reminderMinutesOfDay = nextMinute.hour * 60 + nextMinute.minute,
            reminderEnabled = true
        )

        scheduler.cancel(goalId)
        scheduler.sync(goal)
        scheduler.cancel(goalId)
    }
}
