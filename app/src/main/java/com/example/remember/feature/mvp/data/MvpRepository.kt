package com.example.remember.feature.mvp.data

import android.content.Context
import android.content.pm.ApplicationInfo
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.withTransaction
import com.example.remember.feature.avatar.data.AvatarCatalog
import com.example.remember.feature.avatar.domain.model.AvatarSlot
import com.example.remember.feature.goals.domain.model.GoalCategory
import com.example.remember.feature.mvp.domain.CategoryHistory
import com.example.remember.feature.mvp.domain.AchievementId
import com.example.remember.feature.mvp.domain.AchievementStats
import com.example.remember.feature.mvp.domain.DailyBonusMission
import com.example.remember.feature.mvp.domain.GamificationRules
import com.example.remember.feature.mvp.domain.GoalRecurrence
import com.example.remember.feature.mvp.domain.MvpGoal
import com.example.remember.feature.mvp.domain.MvpState
import com.example.remember.feature.mvp.domain.PrimaryCategoryRules
import com.example.remember.feature.mvp.domain.RewardRules
import com.example.remember.feature.mvp.data.local.GoalCompletionEntity
import com.example.remember.feature.mvp.data.local.RememberDatabase
import com.example.remember.feature.mvp.data.local.toDomain
import com.example.remember.feature.mvp.data.local.toEntity
import com.example.remember.feature.mvp.data.local.toHistoryItem
import com.example.remember.feature.mvp.domain.CompletionHistoryItem
import com.example.remember.feature.store.data.StoreCatalog
import com.example.remember.feature.store.data.StorePurchaseResult
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

private val Context.mvpDataStore by preferencesDataStore(name = "remember_mvp")

enum class PrimaryCategoryChangeStatus { CHANGED, ALREADY_SELECTED, LOCKED }

data class PrimaryCategoryChangeResult(
    val status: PrimaryCategoryChangeStatus,
    val daysRemaining: Int = 0
)

data class ReminderSnapshot(
    val goal: MvpGoal,
    val soundEnabled: Boolean,
    val vibrationEnabled: Boolean
)

class MvpRepository internal constructor(
    private val context: Context,
    private val currentEpochDay: () -> Long = DeviceDayProvider::currentEpochDay,
    private val currentLocalHour: () -> Int = DeviceDayProvider::currentLocalHour,
    private val database: RememberDatabase = RememberDatabase.getInstance(context),
    private val dataStore: DataStore<Preferences> = context.mvpDataStore,
    private val reminderScheduler: ReminderScheduler = GoalReminderScheduler(context),
    private val onCompletionStored: suspend () -> Unit = {}
) {
    private val goalDao = database.goalDao()
    private val completionDao = database.goalCompletionDao()
    private val preferences = dataStore.data.catch { error ->
        if (error is IOException) emit(androidx.datastore.preferences.core.emptyPreferences())
        else throw error
    }

    val state: Flow<MvpState> = flow {
        migrateLegacyGoalsIfNeeded()
        reconcilePendingCompletions()
        emitAll(
            combine(
                preferences,
                goalDao.observeAll(),
                completionDao.observeAll()
            ) { storedPreferences, entities, completions ->
                decodeState(
                    storedPreferences,
                    entities.mapNotNull { it.toDomain() },
                    completions.mapNotNull { it.toHistoryItem() }
                )
            }
        )
    }

    suspend fun saveProfile(
        name: String,
        primaryCategory: GoalCategory,
        bodyItemId: String
    ) {
        dataStore.edit { preferences ->
            preferences[Keys.PROFILE_NAME] = name.trim()
            preferences[Keys.PRIMARY_CATEGORY] = primaryCategory.name
            preferences[Keys.SELECTED_BODY] = bodyItemId
            if (preferences[Keys.PRIMARY_CATEGORY_CHANGED_DAY] == null) {
                preferences[Keys.PRIMARY_CATEGORY_CHANGED_DAY] = currentEpochDay()
            }
            if (preferences[Keys.CATEGORY_HISTORY] == null) {
                preferences[Keys.CATEGORY_HISTORY] = encodeCategoryHistories(
                    GoalCategory.entries.associateWith(::CategoryHistory)
                )
            }
        }
    }

    suspend fun addGoal(
        title: String,
        category: GoalCategory,
        recurrence: GoalRecurrence = GoalRecurrence.NONE,
        reminderMinutesOfDay: Int? = null
    ) {
        if (title.isBlank()) return
        val createdGoal = MvpGoal(
            id = System.currentTimeMillis(),
            title = title.trim(),
            category = category,
            recurrence = recurrence,
            reminderMinutesOfDay = reminderMinutesOfDay,
            reminderEnabled = reminderMinutesOfDay != null
        )
        goalDao.upsert(createdGoal.toEntity())
        reminderScheduler.sync(createdGoal)
    }

    suspend fun updateGoal(
        goalId: Long,
        title: String,
        category: GoalCategory,
        recurrence: GoalRecurrence,
        reminderMinutesOfDay: Int?
    ) {
        if (title.isBlank()) return
        val entity = goalDao.getById(goalId) ?: return
        val existing = entity.toDomain() ?: return
        val updatedGoal = existing.copy(
            title = title.trim(),
            category = category,
            recurrence = recurrence,
            reminderMinutesOfDay = reminderMinutesOfDay,
            reminderEnabled = reminderMinutesOfDay != null
        )
        goalDao.update(updatedGoal.toEntity(entity.createdAtMillis))
        reminderScheduler.sync(updatedGoal)
    }

    suspend fun setPrimaryCategory(category: GoalCategory): PrimaryCategoryChangeResult {
        var result = PrimaryCategoryChangeResult(PrimaryCategoryChangeStatus.ALREADY_SELECTED)
        dataStore.edit { preferences ->
            val currentCategory = preferences.category(Keys.PRIMARY_CATEGORY)
            if (currentCategory == category) return@edit
            val today = currentEpochDay()
            val daysRemaining = PrimaryCategoryRules.daysUntilChange(
                lastChangeDay = preferences[Keys.PRIMARY_CATEGORY_CHANGED_DAY],
                currentDay = today
            )
            if (daysRemaining > 0) {
                result = PrimaryCategoryChangeResult(
                    status = PrimaryCategoryChangeStatus.LOCKED,
                    daysRemaining = daysRemaining
                )
                return@edit
            }
            val histories = decodeCategoryHistories(preferences)
            val history = histories.getValue(category)
            preferences[Keys.CATEGORY_HISTORY] = encodeCategoryHistories(histories)
            preferences[Keys.PRIMARY_CATEGORY] = category.name
            preferences[Keys.PRIMARY_CATEGORY_CHANGED_DAY] = today
            preferences[Keys.CURRENT_STREAK] = history.currentStreak
            preferences[Keys.LONGEST_STREAK] = history.longestStreak
            preferences[Keys.LAST_PRIMARY_COMPLETION_DAY] = history.lastPrimaryCompletionDay
            result = PrimaryCategoryChangeResult(PrimaryCategoryChangeStatus.CHANGED)
        }
        return result
    }

    suspend fun completeGoal(goalId: Long) {
        completionMutex.withLock {
            reconcilePendingCompletionsLocked()
            val today = currentEpochDay()
            val goal = goalDao.getById(goalId)?.toDomain() ?: return@withLock
            if (goal.isCompletedAt(today)) return@withLock

            val event = preparePendingCompletion(goal, preferences.first(), today)
            dataStore.edit { stored ->
                val pending = PendingCompletionCodec.decode(stored[Keys.PENDING_COMPLETIONS])
                if (pending.none { it.eventId == event.eventId }) {
                    stored[Keys.PENDING_COMPLETIONS] = PendingCompletionCodec.encode(pending + event)
                }
            }
            if (!storeCompletion(event)) {
                removePendingCompletion(event.eventId)
                return@withLock
            }
            onCompletionStored()
            applyPendingCompletion(event)
            goalDao.getById(goalId)?.toDomain()?.let(reminderScheduler::sync)
        }
    }

    suspend fun completeBonusMission(): Boolean {
        var completed = false
        dataStore.edit { preferences ->
            val today = currentEpochDay()
            if (preferences[Keys.LAST_BONUS_MISSION_DAY] == today) return@edit
            preferences[Keys.LAST_BONUS_MISSION_DAY] = today
            preferences[Keys.BONUS_MISSIONS_COMPLETED] =
                (preferences[Keys.BONUS_MISSIONS_COMPLETED] ?: 0) + 1
            preferences[Keys.EXPERIENCE] =
                (preferences[Keys.EXPERIENCE] ?: 0) + GamificationRules.BONUS_EXPERIENCE
            preferences[Keys.COINS] =
                (preferences[Keys.COINS] ?: 0) + GamificationRules.BONUS_COINS
            preferences[Keys.TOTAL_COINS_EARNED] =
                (preferences[Keys.TOTAL_COINS_EARNED]
                    ?: ((preferences[Keys.COINS] ?: 0) - GamificationRules.BONUS_COINS)
                        .coerceAtLeast(0)) + GamificationRules.BONUS_COINS
            grantNewAchievements(preferences, decodeCategoryHistories(preferences))
            completed = true
        }
        return completed
    }

    suspend fun recoverStreak(): Boolean {
        var recovered = false
        dataStore.edit { preferences ->
            val today = currentEpochDay()
            val month = DeviceDayProvider.monthKey(today)
            val used = if (preferences[Keys.RECOVERY_MONTH] == month) {
                preferences[Keys.RECOVERIES_USED] ?: 0
            } else {
                0
            }
            val primaryCategory = preferences.category(Keys.PRIMARY_CATEGORY)
            val histories = decodeCategoryHistories(preferences).toMutableMap()
            val history = histories.getValue(primaryCategory)
            if (!GamificationRules.canRecoverStreak(
                    currentStreak = history.currentStreak,
                    lastPrimaryCompletionDay = history.lastPrimaryCompletionDay,
                    currentDay = today,
                    recoveriesUsedThisMonth = used
                )
            ) return@edit

            val recoveredHistory = history.copy(lastPrimaryCompletionDay = today - 1)
            histories[primaryCategory] = recoveredHistory
            preferences[Keys.CATEGORY_HISTORY] = encodeCategoryHistories(histories)
            preferences[Keys.LAST_PRIMARY_COMPLETION_DAY] = today - 1
            preferences[Keys.RECOVERY_MONTH] = month
            preferences[Keys.RECOVERIES_USED] = used + 1
            recovered = true
        }
        return recovered
    }

    suspend fun deleteGoal(goalId: Long) {
        goalDao.deleteById(goalId)
        reminderScheduler.cancel(goalId)
    }

    suspend fun purchaseBundle(bundleId: String): StorePurchaseResult {
        val bundle = StoreCatalog.find(bundleId) ?: return StorePurchaseResult.UNKNOWN_BUNDLE
        var result = StorePurchaseResult.UNKNOWN_BUNDLE
        dataStore.edit { preferences ->
            val owned = decodeIdSet(preferences[Keys.OWNED_BUNDLES])
            val coins = preferences[Keys.COINS] ?: 0
            result = when {
                bundle.id in owned -> StorePurchaseResult.ALREADY_OWNED
                coins < bundle.price -> StorePurchaseResult.INSUFFICIENT_COINS
                else -> {
                    preferences[Keys.COINS] = coins - bundle.price
                    preferences[Keys.OWNED_BUNDLES] = encodeIdSet(owned + bundle.id)
                    grantNewAchievements(preferences, decodeCategoryHistories(preferences))
                    StorePurchaseResult.PURCHASED
                }
            }
        }
        return result
    }

    suspend fun equipBundle(bundleId: String): Boolean {
        val bundle = StoreCatalog.find(bundleId) ?: return false
        var equipped = false
        dataStore.edit { preferences ->
            if (bundle.id !in decodeIdSet(preferences[Keys.OWNED_BUNDLES])) return@edit
            bundle.equippedItemIds[AvatarSlot.HAIR]?.let { preferences[Keys.SELECTED_HAIR] = it }
            bundle.equippedItemIds[AvatarSlot.GLASSES]?.let { preferences[Keys.SELECTED_GLASSES] = it }
            bundle.equippedItemIds[AvatarSlot.SHIRT]?.let { preferences[Keys.SELECTED_SHIRT] = it }
            bundle.equippedItemIds[AvatarSlot.HAT]?.let { preferences[Keys.SELECTED_HAT] = it }
            preferences[Keys.AVATAR_HAIR_COLOR] = bundle.hairColor
            preferences[Keys.AVATAR_SHIRT_COLOR] = bundle.shirtColor
            preferences[Keys.AVATAR_GLASSES_COLOR] = bundle.glassesColor
            preferences[Keys.AVATAR_HAT_COLOR] = bundle.hatColor
            equipped = true
        }
        return equipped
    }

    suspend fun beginStorePurchaseTest() {
        if (!context.isDebuggable()) return
        dataStore.edit { preferences ->
            if (preferences[Keys.DEBUG_STORE_TEST_ACTIVE] == true) return@edit
            preferences[Keys.DEBUG_STORE_TEST_ACTIVE] = true
            preferences[Keys.DEBUG_ORIGINAL_COINS] = preferences[Keys.COINS] ?: 0
            preferences[Keys.DEBUG_ORIGINAL_OWNED_BUNDLES] = preferences[Keys.OWNED_BUNDLES].orEmpty()
            preferences[Keys.DEBUG_ORIGINAL_SELECTED_HAIR] = preferences[Keys.SELECTED_HAIR] ?: "hair_01"
            preferences[Keys.DEBUG_ORIGINAL_SELECTED_GLASSES] = preferences[Keys.SELECTED_GLASSES] ?: "glasses_01"
            preferences[Keys.DEBUG_ORIGINAL_SELECTED_SHIRT] = preferences[Keys.SELECTED_SHIRT] ?: "shirt_01"
            preferences[Keys.DEBUG_ORIGINAL_SELECTED_HAT] = preferences[Keys.SELECTED_HAT] ?: "hat_01"
            preferences[Keys.DEBUG_ORIGINAL_HAIR_COLOR] = preferences[Keys.AVATAR_HAIR_COLOR] ?: "#29232A"
            preferences[Keys.DEBUG_ORIGINAL_SHIRT_COLOR] = preferences[Keys.AVATAR_SHIRT_COLOR] ?: "#4FA6D4"
            preferences[Keys.DEBUG_ORIGINAL_GLASSES_COLOR] = preferences[Keys.AVATAR_GLASSES_COLOR] ?: "#30333E"
            preferences[Keys.DEBUG_ORIGINAL_HAT_COLOR] = preferences[Keys.AVATAR_HAT_COLOR] ?: "#4B5367"
            preferences[Keys.COINS] = STORE_TEST_COINS
            preferences[Keys.OWNED_BUNDLES] = ""
        }
    }

    suspend fun finishStorePurchaseTest() {
        if (!context.isDebuggable()) return
        dataStore.edit { preferences ->
            if (preferences[Keys.DEBUG_STORE_TEST_ACTIVE] != true) return@edit
            preferences[Keys.COINS] = preferences[Keys.DEBUG_ORIGINAL_COINS] ?: 0
            preferences[Keys.OWNED_BUNDLES] = preferences[Keys.DEBUG_ORIGINAL_OWNED_BUNDLES].orEmpty()
            preferences[Keys.SELECTED_HAIR] = preferences[Keys.DEBUG_ORIGINAL_SELECTED_HAIR] ?: "hair_01"
            preferences[Keys.SELECTED_GLASSES] = preferences[Keys.DEBUG_ORIGINAL_SELECTED_GLASSES] ?: "glasses_01"
            preferences[Keys.SELECTED_SHIRT] = preferences[Keys.DEBUG_ORIGINAL_SELECTED_SHIRT] ?: "shirt_01"
            preferences[Keys.SELECTED_HAT] = preferences[Keys.DEBUG_ORIGINAL_SELECTED_HAT] ?: "hat_01"
            preferences[Keys.AVATAR_HAIR_COLOR] = preferences[Keys.DEBUG_ORIGINAL_HAIR_COLOR] ?: "#29232A"
            preferences[Keys.AVATAR_SHIRT_COLOR] = preferences[Keys.DEBUG_ORIGINAL_SHIRT_COLOR] ?: "#4FA6D4"
            preferences[Keys.AVATAR_GLASSES_COLOR] = preferences[Keys.DEBUG_ORIGINAL_GLASSES_COLOR] ?: "#30333E"
            preferences[Keys.AVATAR_HAT_COLOR] = preferences[Keys.DEBUG_ORIGINAL_HAT_COLOR] ?: "#4B5367"
            preferences.remove(Keys.DEBUG_STORE_TEST_ACTIVE)
            preferences.remove(Keys.DEBUG_ORIGINAL_COINS)
            preferences.remove(Keys.DEBUG_ORIGINAL_OWNED_BUNDLES)
            preferences.remove(Keys.DEBUG_ORIGINAL_SELECTED_HAIR)
            preferences.remove(Keys.DEBUG_ORIGINAL_SELECTED_GLASSES)
            preferences.remove(Keys.DEBUG_ORIGINAL_SELECTED_SHIRT)
            preferences.remove(Keys.DEBUG_ORIGINAL_SELECTED_HAT)
            preferences.remove(Keys.DEBUG_ORIGINAL_HAIR_COLOR)
            preferences.remove(Keys.DEBUG_ORIGINAL_SHIRT_COLOR)
            preferences.remove(Keys.DEBUG_ORIGINAL_GLASSES_COLOR)
            preferences.remove(Keys.DEBUG_ORIGINAL_HAT_COLOR)
        }
    }

    suspend fun selectAvatarItem(slot: AvatarSelection, itemId: String) {
        dataStore.edit { preferences ->
            val avatarSlot = slot.toAvatarSlot()
            val ownedBundles = decodeIdSet(preferences[Keys.OWNED_BUNDLES])
            if (!AvatarCatalog.contains(avatarSlot, itemId)) return@edit
            if (!StoreCatalog.isItemUnlocked(itemId, ownedBundles)) return@edit
            preferences[when (slot) {
                AvatarSelection.BODY -> Keys.SELECTED_BODY
                AvatarSelection.FACE -> Keys.SELECTED_FACE
                AvatarSelection.EYES -> Keys.SELECTED_EYES
                AvatarSelection.BEARD -> Keys.SELECTED_BEARD
                AvatarSelection.HAIR -> Keys.SELECTED_HAIR
                AvatarSelection.GLASSES -> Keys.SELECTED_GLASSES
                AvatarSelection.SHIRT -> Keys.SELECTED_SHIRT
                AvatarSelection.HAT -> Keys.SELECTED_HAT
            }] = itemId
        }
    }

    suspend fun selectAvatarColor(target: AvatarColorSelection, color: String) {
        if (!HEX_COLOR.matches(color)) return
        dataStore.edit { preferences ->
            preferences[when (target) {
                AvatarColorSelection.SKIN -> Keys.AVATAR_SKIN_COLOR
                AvatarColorSelection.HAIR -> Keys.AVATAR_HAIR_COLOR
                AvatarColorSelection.EYES -> Keys.AVATAR_EYE_COLOR
                AvatarColorSelection.SHIRT -> Keys.AVATAR_SHIRT_COLOR
                AvatarColorSelection.BEARD -> Keys.AVATAR_BEARD_COLOR
                AvatarColorSelection.GLASSES -> Keys.AVATAR_GLASSES_COLOR
                AvatarColorSelection.HAT -> Keys.AVATAR_HAT_COLOR
            }] = color.uppercase()
        }
    }

    suspend fun setSoundEnabled(enabled: Boolean) {
        dataStore.edit { it[Keys.SOUND_ENABLED] = enabled }
    }

    suspend fun setVibrationEnabled(enabled: Boolean) {
        dataStore.edit { it[Keys.VIBRATION_ENABLED] = enabled }
    }

    suspend fun rescheduleReminders() {
        migrateLegacyGoalsIfNeeded()
        goalDao.getAll().mapNotNull { it.toDomain() }.forEach(reminderScheduler::sync)
    }

    suspend fun reminderSnapshot(goalId: Long): ReminderSnapshot? {
        migrateLegacyGoalsIfNeeded()
        val preferences = dataStore.data.first()
        val goal = goalDao.getById(goalId)?.toDomain()
            ?.takeIf { it.reminderEnabled }
            ?: return null
        if (goal.isCompletedAt(currentEpochDay())) return null
        return ReminderSnapshot(
            goal = goal,
            soundEnabled = preferences[Keys.SOUND_ENABLED] ?: true,
            vibrationEnabled = preferences[Keys.VIBRATION_ENABLED] ?: true
        )
    }

    private suspend fun migrateLegacyGoalsIfNeeded() {
        val storedPreferences = preferences.first()
        if (storedPreferences[Keys.ROOM_GOALS_MIGRATED] == true) return

        val legacyGoals = decodeGoals(storedPreferences[Keys.GOALS])
        val legacyHistories = decodeCategoryHistories(storedPreferences, legacyGoals)
        database.withTransaction {
            if (goalDao.count() == 0 && legacyGoals.isNotEmpty()) {
                goalDao.insertAll(legacyGoals.map { it.toEntity() })
            }
            if (completionDao.count() == 0) {
                legacyGoals.mapNotNull { goal ->
                    val completionDay = goal.lastCompletedEpochDay
                        ?: currentEpochDay().takeIf { goal.isCompleted }
                        ?: return@mapNotNull null
                    GoalCompletionEntity(
                        goalId = goal.id,
                        goalTitle = goal.title,
                        category = goal.category.name,
                        completedEpochDay = completionDay,
                        completedAtMillis = DeviceDayProvider.startOfDayMillis(completionDay),
                        experienceEarned = 0,
                        coinsEarned = 0
                    )
                }.forEach { completionDao.insert(it) }
            }
        }
        dataStore.edit { mutablePreferences ->
            if (mutablePreferences[Keys.CATEGORY_HISTORY] == null) {
                mutablePreferences[Keys.CATEGORY_HISTORY] = encodeCategoryHistories(legacyHistories)
            }
            mutablePreferences[Keys.ROOM_GOALS_MIGRATED] = true
            mutablePreferences.remove(Keys.GOALS)
        }
    }

    private suspend fun reconcilePendingCompletions() {
        completionMutex.withLock { reconcilePendingCompletionsLocked() }
    }

    private suspend fun reconcilePendingCompletionsLocked() {
        PendingCompletionCodec.decode(preferences.first()[Keys.PENDING_COMPLETIONS])
            .forEach { event ->
                if (storeCompletion(event)) {
                    applyPendingCompletion(event)
                    goalDao.getById(event.goalId)?.toDomain()?.let(reminderScheduler::sync)
                } else {
                    removePendingCompletion(event.eventId)
                }
            }
    }

    private fun preparePendingCompletion(
        goal: MvpGoal,
        storedPreferences: Preferences,
        completedEpochDay: Long
    ): PendingCompletion {
        val histories = decodeCategoryHistories(storedPreferences)
        val primaryCategory = storedPreferences.category(Keys.PRIMARY_CATEGORY)
        val primaryHistory = histories.getValue(primaryCategory)
        val advancesPrimaryStreak = RewardRules.isFirstPrimaryCompletionToday(
            category = goal.category,
            primaryCategory = primaryCategory,
            lastPrimaryCompletionDay = primaryHistory.lastPrimaryCompletionDay,
            currentDay = completedEpochDay
        )
        val reward = RewardRules.completionReward(
            category = goal.category,
            primaryCategory = primaryCategory,
            isFirstPrimaryCompletionToday = advancesPrimaryStreak
        )
        var experienceEarned = reward.experience
        var coinsEarned = reward.coins
        if (advancesPrimaryStreak) {
            val nextStreak = RewardRules.nextStreak(
                current = primaryHistory.currentStreak,
                lastCompletionDay = primaryHistory.lastPrimaryCompletionDay,
                currentDay = completedEpochDay
            )
            val seasonReward = RewardRules.seasonReward(
                previousLongestStreak = primaryHistory.longestStreak,
                newLongestStreak = maxOf(primaryHistory.longestStreak, nextStreak)
            )
            experienceEarned += seasonReward.experience
            coinsEarned += seasonReward.coins
        }
        return PendingCompletion(
            eventId = "${goal.id}:$completedEpochDay",
            goalId = goal.id,
            goalTitle = goal.title,
            category = goal.category,
            completedEpochDay = completedEpochDay,
            completedAtMillis = System.currentTimeMillis(),
            experienceEarned = experienceEarned,
            coinsEarned = coinsEarned,
            advancesPrimaryStreak = advancesPrimaryStreak,
            localHour = currentLocalHour()
        )
    }

    private suspend fun storeCompletion(event: PendingCompletion): Boolean =
        database.withTransaction {
            if (completionDao.find(event.goalId, event.completedEpochDay) != null) {
                return@withTransaction true
            }
            val liveEntity = goalDao.getById(event.goalId) ?: return@withTransaction false
            val liveGoal = liveEntity.toDomain() ?: return@withTransaction false
            val completedGoal = liveGoal.copy(
                isCompleted = true,
                lastCompletedEpochDay = event.completedEpochDay
            )
            goalDao.update(completedGoal.toEntity(liveEntity.createdAtMillis))
            completionDao.insert(
                GoalCompletionEntity(
                    goalId = event.goalId,
                    goalTitle = event.goalTitle,
                    category = event.category.name,
                    completedEpochDay = event.completedEpochDay,
                    completedAtMillis = event.completedAtMillis,
                    experienceEarned = event.experienceEarned,
                    coinsEarned = event.coinsEarned
                )
            )
            true
        }

    private suspend fun applyPendingCompletion(event: PendingCompletion) {
        dataStore.edit { stored ->
            val pending = PendingCompletionCodec.decode(stored[Keys.PENDING_COMPLETIONS])
            if (pending.none { it.eventId == event.eventId }) return@edit

            val histories = decodeCategoryHistories(stored).toMutableMap()
            var updatedHistory = histories.getValue(event.category).let { history ->
                history.copy(totalCompletions = history.totalCompletions + 1)
            }
            if (event.advancesPrimaryStreak) {
                val nextStreak = RewardRules.nextStreak(
                    current = updatedHistory.currentStreak,
                    lastCompletionDay = updatedHistory.lastPrimaryCompletionDay,
                    currentDay = event.completedEpochDay
                )
                updatedHistory = updatedHistory.copy(
                    currentStreak = nextStreak,
                    longestStreak = maxOf(updatedHistory.longestStreak, nextStreak),
                    lastPrimaryCompletionDay = event.completedEpochDay
                )
                if (stored.category(Keys.PRIMARY_CATEGORY) == event.category) {
                    stored[Keys.CURRENT_STREAK] = updatedHistory.currentStreak
                    stored[Keys.LONGEST_STREAK] = updatedHistory.longestStreak
                    stored[Keys.LAST_PRIMARY_COMPLETION_DAY] = event.completedEpochDay
                }
            }
            histories[event.category] = updatedHistory

            val coinsBeforeCompletion = stored[Keys.COINS] ?: 0
            stored[Keys.TOTAL_COMPLETIONS] = (stored[Keys.TOTAL_COMPLETIONS] ?: 0) + 1
            stored[Keys.CATEGORY_HISTORY] = encodeCategoryHistories(histories)
            stored[Keys.EXPERIENCE] = (stored[Keys.EXPERIENCE] ?: 0) + event.experienceEarned
            stored[Keys.COINS] = coinsBeforeCompletion + event.coinsEarned
            stored[Keys.TOTAL_COINS_EARNED] =
                (stored[Keys.TOTAL_COINS_EARNED] ?: coinsBeforeCompletion) + event.coinsEarned
            recordCompletionStats(stored, event.completedEpochDay, event.localHour)
            grantNewAchievements(stored, histories)
            stored[Keys.PENDING_COMPLETIONS] = PendingCompletionCodec.encode(
                pending.filterNot { it.eventId == event.eventId }
            )
        }
    }

    private suspend fun removePendingCompletion(eventId: String) {
        dataStore.edit { stored ->
            val pending = PendingCompletionCodec.decode(stored[Keys.PENDING_COMPLETIONS])
            stored[Keys.PENDING_COMPLETIONS] = PendingCompletionCodec.encode(
                pending.filterNot { it.eventId == eventId }
            )
        }
    }

    private fun recordCompletionStats(
        preferences: androidx.datastore.preferences.core.MutablePreferences,
        currentDay: Long,
        localHour: Int
    ) {
        val countToday = if (preferences[Keys.COMPLETION_COUNT_DAY] == currentDay) {
            (preferences[Keys.COMPLETION_COUNT_TODAY] ?: 0) + 1
        } else {
            1
        }
        preferences[Keys.COMPLETION_COUNT_DAY] = currentDay
        preferences[Keys.COMPLETION_COUNT_TODAY] = countToday
        preferences[Keys.MAX_DAILY_COMPLETIONS] = maxOf(
            preferences[Keys.MAX_DAILY_COMPLETIONS] ?: 0,
            countToday
        )
        if (localHour < 6 || localHour >= 18) {
            preferences[Keys.NIGHT_COMPLETIONS] =
                (preferences[Keys.NIGHT_COMPLETIONS] ?: 0) + 1
        } else {
            preferences[Keys.DAY_COMPLETIONS] =
                (preferences[Keys.DAY_COMPLETIONS] ?: 0) + 1
        }
    }

    private fun grantNewAchievements(
        preferences: androidx.datastore.preferences.core.MutablePreferences,
        histories: Map<GoalCategory, CategoryHistory>
    ) {
        val existing = decodeAchievementIds(preferences[Keys.UNLOCKED_ACHIEVEMENTS])
        val ownedBundles = decodeIdSet(preferences[Keys.OWNED_BUNDLES])
        val accessoriesOwned = StoreCatalog.bundles
            .filter { it.id in ownedBundles }
            .flatMap { it.equippedItemIds.values }
            .distinct()
            .size
        val stats = AchievementStats(
            totalCompletions = preferences[Keys.TOTAL_COMPLETIONS] ?: 0,
            highestCategoryCompletions = histories.values.maxOfOrNull {
                it.totalCompletions
            } ?: 0,
            highestDailyCompletions = preferences[Keys.MAX_DAILY_COMPLETIONS] ?: 0,
            nightCompletions = preferences[Keys.NIGHT_COMPLETIONS] ?: 0,
            bonusMissionsCompleted = preferences[Keys.BONUS_MISSIONS_COMPLETED] ?: 0,
            longestStreak = histories.values.maxOfOrNull { it.longestStreak } ?: 0,
            activeCategories = histories.values.count { it.totalCompletions > 0 },
            totalCoinsEarned = preferences[Keys.TOTAL_COINS_EARNED]
                ?: preferences[Keys.COINS]
                ?: 0,
            totalExperience = preferences[Keys.EXPERIENCE] ?: 0,
            accessoriesOwned = accessoriesOwned
        )
        val newlyUnlocked = GamificationRules.unlockedAchievements(stats) - existing
        if (newlyUnlocked.isEmpty()) return
        val achievementExperience = newlyUnlocked.size * GamificationRules.ACHIEVEMENT_EXPERIENCE
        val achievementCoins = newlyUnlocked.size * GamificationRules.ACHIEVEMENT_COINS
        preferences[Keys.UNLOCKED_ACHIEVEMENTS] = encodeAchievementIds(existing + newlyUnlocked)
        preferences[Keys.EXPERIENCE] =
            (preferences[Keys.EXPERIENCE] ?: 0) + achievementExperience
        preferences[Keys.COINS] = (preferences[Keys.COINS] ?: 0) + achievementCoins
        preferences[Keys.TOTAL_COINS_EARNED] =
            (preferences[Keys.TOTAL_COINS_EARNED] ?: 0) + achievementCoins
    }

    private fun decodeState(
        preferences: Preferences,
        goals: List<MvpGoal>,
        completions: List<CompletionHistoryItem>
    ): MvpState {
        val today = currentEpochDay()
        val primaryCategory = preferences.category(Keys.PRIMARY_CATEGORY)
        val storedHistories = decodeCategoryHistories(preferences, goals)
        val histories = storedHistories.mapValues { (_, history) ->
            history.copy(
                currentStreak = RewardRules.activeStreak(
                    current = history.currentStreak,
                    lastCompletionDay = history.lastPrimaryCompletionDay,
                    currentDay = today
                )
            )
        }
        val primaryHistory = histories.getValue(primaryCategory)
        val storedPrimaryHistory = storedHistories.getValue(primaryCategory)
        val month = DeviceDayProvider.monthKey(today)
        val recoveriesUsed = if (preferences[Keys.RECOVERY_MONTH] == month) {
            preferences[Keys.RECOVERIES_USED] ?: 0
        } else {
            0
        }
        return MvpState(
        profileName = preferences[Keys.PROFILE_NAME].orEmpty(),
        primaryCategory = primaryCategory,
        goals = goals.map { it.normalizedAt(today) },
        completionHistory = completions,
        experience = preferences[Keys.EXPERIENCE] ?: 0,
        coins = preferences[Keys.COINS] ?: 0,
        currentStreak = primaryHistory.currentStreak,
        longestStreak = primaryHistory.longestStreak,
        totalCompletions = preferences[Keys.TOTAL_COMPLETIONS] ?: 0,
        categoryHistory = histories,
        primaryCategoryChangeDaysRemaining = PrimaryCategoryRules.daysUntilChange(
            lastChangeDay = preferences[Keys.PRIMARY_CATEGORY_CHANGED_DAY],
            currentDay = today
        ),
        bonusMission = DailyBonusMission(
            category = primaryCategory,
            completed = preferences[Keys.LAST_BONUS_MISSION_DAY] == today
        ),
        unlockedAchievements = decodeAchievementIds(
            preferences[Keys.UNLOCKED_ACHIEVEMENTS]
        ),
        streakRecoveriesRemaining =
            (GamificationRules.MONTHLY_RECOVERIES - recoveriesUsed).coerceAtLeast(0),
        canRecoverStreak = GamificationRules.canRecoverStreak(
            currentStreak = storedPrimaryHistory.currentStreak,
            lastPrimaryCompletionDay = storedPrimaryHistory.lastPrimaryCompletionDay,
            currentDay = today,
            recoveriesUsedThisMonth = recoveriesUsed
        ),
        selectedBodyId = selection(
            preferences,
            Keys.SELECTED_BODY,
            AvatarSlot.BODY,
            "body_03",
            mapOf(
                "body_slim" to "body_01",
                "body_medium" to "body_03",
                "body_average" to "body_03",
                "body_wide" to "body_03",
                "body_plus" to "body_03"
            )
        ),
        selectedFaceId = selection(preferences, Keys.SELECTED_FACE, AvatarSlot.FACE, "face_01"),
        selectedEyesId = selection(
            preferences,
            Keys.SELECTED_EYES,
            AvatarSlot.EYES,
            if (preferences[Keys.SELECTED_EXPRESSION]?.contains("surpris", ignoreCase = true) == true) {
                "eyes_03"
            } else {
                "eyes_01"
            }
        ),
        selectedBeardId = selection(preferences, Keys.SELECTED_BEARD, AvatarSlot.BEARD, "beard_02"),
        selectedHairId = selection(
            preferences,
            Keys.SELECTED_HAIR,
            AvatarSlot.HAIR,
            "hair_01",
            mapOf(
                "hair_curly" to "hair_01",
                "hair_bangs" to "hair_08",
                "hair_long" to "hair_04",
                "hair_spiky_01" to "hair_03",
                "hair_short_curly_02" to "hair_06",
                "hair_messy_spiky" to "hair_03",
                "hair_dreads" to "hair_06"
            )
        ).takeIf {
            StoreCatalog.isItemUnlocked(it, decodeIdSet(preferences[Keys.OWNED_BUNDLES]))
        } ?: "hair_01",
        selectedGlassesId = selection(
            preferences,
            Keys.SELECTED_GLASSES,
            AvatarSlot.GLASSES,
            if (preferences[Keys.ROUND_GLASSES] == true) "glasses_02" else "glasses_01",
            mapOf("glasses_round" to "glasses_02")
        ),
        selectedShirtId = selection(preferences, Keys.SELECTED_SHIRT, AvatarSlot.SHIRT, "shirt_01"),
        selectedHatId = selection(
            preferences,
            Keys.SELECTED_HAT,
            AvatarSlot.HAT,
            "hat_01",
            mapOf(
                "hat_beanie_01" to "hat_02",
                "hat_cap_02" to "hat_02",
                "cap_blue" to "hat_02"
            )
        ).takeIf {
            StoreCatalog.isItemUnlocked(it, decodeIdSet(preferences[Keys.OWNED_BUNDLES]))
        } ?: "hat_01",
        avatarSkinColor = preferences[Keys.AVATAR_SKIN_COLOR] ?: "#87513A",
        avatarHairColor = preferences[Keys.AVATAR_HAIR_COLOR] ?: "#29232A",
        avatarEyeColor = preferences[Keys.AVATAR_EYE_COLOR] ?: "#382E34",
        avatarShirtColor = preferences[Keys.AVATAR_SHIRT_COLOR] ?: "#4FA6D4",
        avatarBeardColor = preferences[Keys.AVATAR_BEARD_COLOR] ?: "#29232A",
        avatarGlassesColor = preferences[Keys.AVATAR_GLASSES_COLOR] ?: "#30333E",
        avatarHatColor = preferences[Keys.AVATAR_HAT_COLOR] ?: "#4B5367",
        ownedBundleIds = decodeIdSet(preferences[Keys.OWNED_BUNDLES]),
        soundEnabled = preferences[Keys.SOUND_ENABLED] ?: true,
        vibrationEnabled = preferences[Keys.VIBRATION_ENABLED] ?: true
        )
    }

    private fun Preferences.category(key: Preferences.Key<String>): GoalCategory =
        this[key]?.let { value ->
            runCatching { GoalCategory.valueOf(value) }.getOrNull()
        } ?: GoalCategory.STUDY

    private fun decodeGoals(value: String?): List<MvpGoal> = GoalCodec.decode(value)

    private fun encodeCategoryHistories(histories: Map<GoalCategory, CategoryHistory>): String =
        CategoryHistoryCodec.encode(histories)

    private fun decodeCategoryHistories(
        preferences: Preferences,
        goals: List<MvpGoal> = decodeGoals(preferences[Keys.GOALS])
    ): Map<GoalCategory, CategoryHistory> {
        val decoded = CategoryHistoryCodec.decode(preferences[Keys.CATEGORY_HISTORY])
        if (decoded.isNotEmpty()) {
            return GoalCategory.entries.associateWith { category ->
                decoded[category] ?: CategoryHistory(category)
            }
        }

        val primaryCategory = preferences.category(Keys.PRIMARY_CATEGORY)
        val completedByCategory = goals
            .filter(MvpGoal::isCompleted)
            .groupingBy(MvpGoal::category)
            .eachCount()
        return GoalCategory.entries.associateWith { category ->
            CategoryHistory(
                category = category,
                currentStreak = if (category == primaryCategory) {
                    preferences[Keys.CURRENT_STREAK] ?: 0
                } else {
                    0
                },
                longestStreak = if (category == primaryCategory) {
                    preferences[Keys.LONGEST_STREAK] ?: 0
                } else {
                    0
                },
                totalCompletions = completedByCategory[category] ?: 0,
                lastPrimaryCompletionDay = if (category == primaryCategory) {
                    preferences[Keys.LAST_PRIMARY_COMPLETION_DAY]
                        ?: preferences[Keys.LAST_COMPLETION_DAY]
                        ?: Long.MIN_VALUE
                } else {
                    Long.MIN_VALUE
                }
            )
        }
    }

    private fun encodeIdSet(ids: Set<String>): String = ids.sorted().joinToString(",")

    private fun decodeIdSet(value: String?): Set<String> = value
        ?.split(',')
        ?.filter(String::isNotBlank)
        ?.toSet()
        .orEmpty()

    private fun encodeAchievementIds(ids: Set<AchievementId>): String =
        ids.map(AchievementId::name).sorted().joinToString(",")

    private fun decodeAchievementIds(value: String?): Set<AchievementId> = value
        ?.split(',')
        ?.mapNotNull { encoded ->
            runCatching { AchievementId.valueOf(encoded) }.getOrNull()
        }
        ?.toSet()
        .orEmpty()

    private fun Context.isDebuggable(): Boolean =
        applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0

    enum class AvatarSelection { BODY, FACE, EYES, BEARD, HAIR, GLASSES, SHIRT, HAT }

    enum class AvatarColorSelection { SKIN, HAIR, EYES, SHIRT, BEARD, GLASSES, HAT }

    private fun AvatarSelection.toAvatarSlot(): AvatarSlot = when (this) {
        AvatarSelection.BODY -> AvatarSlot.BODY
        AvatarSelection.FACE -> AvatarSlot.FACE
        AvatarSelection.EYES -> AvatarSlot.EYES
        AvatarSelection.BEARD -> AvatarSlot.BEARD
        AvatarSelection.HAIR -> AvatarSlot.HAIR
        AvatarSelection.GLASSES -> AvatarSlot.GLASSES
        AvatarSelection.SHIRT -> AvatarSlot.SHIRT
        AvatarSelection.HAT -> AvatarSlot.HAT
    }

    private fun selection(
        preferences: Preferences,
        key: Preferences.Key<String>,
        slot: AvatarSlot,
        default: String,
        legacyIds: Map<String, String> = emptyMap()
    ): String {
        val stored = preferences[key] ?: return default
        val migrated = legacyIds[stored] ?: stored
        return if (AvatarCatalog.contains(slot, migrated)) migrated else default
    }

    private object Keys {
        val PROFILE_NAME = stringPreferencesKey("profile_name")
        val PRIMARY_CATEGORY = stringPreferencesKey("primary_category")
        val GOALS = stringPreferencesKey("goals")
        val EXPERIENCE = intPreferencesKey("experience")
        val COINS = intPreferencesKey("coins")
        val CURRENT_STREAK = intPreferencesKey("current_streak")
        val LONGEST_STREAK = intPreferencesKey("longest_streak")
        val TOTAL_COMPLETIONS = intPreferencesKey("total_completions")
        val CATEGORY_HISTORY = stringPreferencesKey("category_history")
        val PRIMARY_CATEGORY_CHANGED_DAY = longPreferencesKey("primary_category_changed_day")
        val LAST_BONUS_MISSION_DAY = longPreferencesKey("last_bonus_mission_day")
        val BONUS_MISSIONS_COMPLETED = intPreferencesKey("bonus_missions_completed")
        val UNLOCKED_ACHIEVEMENTS = stringPreferencesKey("unlocked_achievements")
        val TOTAL_COINS_EARNED = intPreferencesKey("total_coins_earned")
        val COMPLETION_COUNT_DAY = longPreferencesKey("completion_count_day")
        val COMPLETION_COUNT_TODAY = intPreferencesKey("completion_count_today")
        val MAX_DAILY_COMPLETIONS = intPreferencesKey("max_daily_completions")
        val DAY_COMPLETIONS = intPreferencesKey("day_completions")
        val NIGHT_COMPLETIONS = intPreferencesKey("night_completions")
        val RECOVERY_MONTH = stringPreferencesKey("recovery_month")
        val RECOVERIES_USED = intPreferencesKey("recoveries_used")
        val LAST_COMPLETION_DAY = longPreferencesKey("last_completion_day")
        val LAST_PRIMARY_COMPLETION_DAY = longPreferencesKey("last_primary_completion_day")
        val SELECTED_BODY = stringPreferencesKey("selected_body")
        val SELECTED_FACE = stringPreferencesKey("selected_face")
        val SELECTED_EYES = stringPreferencesKey("selected_eyes")
        val SELECTED_BEARD = stringPreferencesKey("selected_beard")
        val SELECTED_HAIR = stringPreferencesKey("selected_hair")
        val SELECTED_SHIRT = stringPreferencesKey("selected_shirt")
        val SELECTED_TOP = stringPreferencesKey("selected_top")
        val SELECTED_EXPRESSION = stringPreferencesKey("selected_expression")
        val SELECTED_PANTS = stringPreferencesKey("selected_pants")
        val SELECTED_SHOES = stringPreferencesKey("selected_shoes")
        val SELECTED_FACIAL_DETAIL = stringPreferencesKey("selected_facial_detail")
        val SELECTED_GLASSES = stringPreferencesKey("selected_glasses")
        val SELECTED_HAT = stringPreferencesKey("selected_hat")
        val SELECTED_SCARF = stringPreferencesKey("selected_scarf")
        val SELECTED_EARRINGS = stringPreferencesKey("selected_earrings")
        val SELECTED_WATCH = stringPreferencesKey("selected_watch")
        val AVATAR_SKIN_COLOR = stringPreferencesKey("avatar_skin_color")
        val AVATAR_HAIR_COLOR = stringPreferencesKey("avatar_hair_color")
        val AVATAR_EYE_COLOR = stringPreferencesKey("avatar_eye_color")
        val AVATAR_SHIRT_COLOR = stringPreferencesKey("avatar_shirt_color")
        val AVATAR_BEARD_COLOR = stringPreferencesKey("avatar_beard_color")
        val AVATAR_GLASSES_COLOR = stringPreferencesKey("avatar_glasses_color")
        val AVATAR_HAT_COLOR = stringPreferencesKey("avatar_hat_color")
        val OWNED_BUNDLES = stringPreferencesKey("owned_bundles")
        val ROUND_GLASSES = booleanPreferencesKey("round_glasses")
        val SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
        val VIBRATION_ENABLED = booleanPreferencesKey("vibration_enabled")
        val ROOM_GOALS_MIGRATED = booleanPreferencesKey("room_goals_migrated")
        val PENDING_COMPLETIONS = stringPreferencesKey("pending_completions")
        val DEBUG_STORE_TEST_ACTIVE = booleanPreferencesKey("debug_store_test_active")
        val DEBUG_ORIGINAL_COINS = intPreferencesKey("debug_original_coins")
        val DEBUG_ORIGINAL_OWNED_BUNDLES = stringPreferencesKey("debug_original_owned_bundles")
        val DEBUG_ORIGINAL_SELECTED_HAIR = stringPreferencesKey("debug_original_selected_hair")
        val DEBUG_ORIGINAL_SELECTED_GLASSES = stringPreferencesKey("debug_original_selected_glasses")
        val DEBUG_ORIGINAL_SELECTED_SHIRT = stringPreferencesKey("debug_original_selected_shirt")
        val DEBUG_ORIGINAL_SELECTED_HAT = stringPreferencesKey("debug_original_selected_hat")
        val DEBUG_ORIGINAL_HAIR_COLOR = stringPreferencesKey("debug_original_hair_color")
        val DEBUG_ORIGINAL_SHIRT_COLOR = stringPreferencesKey("debug_original_shirt_color")
        val DEBUG_ORIGINAL_GLASSES_COLOR = stringPreferencesKey("debug_original_glasses_color")
        val DEBUG_ORIGINAL_HAT_COLOR = stringPreferencesKey("debug_original_hat_color")
    }

    private companion object {
        val HEX_COLOR = Regex("^#[0-9A-Fa-f]{6}$")
        const val STORE_TEST_COINS = 500
        val completionMutex = Mutex()
    }
}
