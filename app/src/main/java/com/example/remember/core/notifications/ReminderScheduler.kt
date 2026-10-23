package com.example.remember.core.notifications

interface ReminderScheduler {
    fun schedule(goalId: Long, triggerAtMillis: Long)

    fun cancel(goalId: Long)
}
