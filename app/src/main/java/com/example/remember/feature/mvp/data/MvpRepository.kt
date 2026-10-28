package com.example.remember.feature.mvp.data

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.remember.feature.goals.domain.model.GoalCategory
import com.example.remember.feature.mvp.domain.MvpGoal
import com.example.remember.feature.mvp.domain.MvpState
import com.example.remember.feature.mvp.domain.RewardRules
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.time.LocalDate
import java.util.Base64
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

private val Context.mvpDataStore by preferencesDataStore(name = "remember_mvp")

class MvpRepository(private val context: Context) {
    val state: Flow<MvpState> = context.mvpDataStore.data
        .catch { error ->
            if (error is IOException) emit(androidx.datastore.preferences.core.emptyPreferences())
            else throw error
        }
        .map(::decodeState)

    suspend fun saveProfile(
        name: String,
        primaryCategory: GoalCategory,
        bodyItemId: String
    ) {
        context.mvpDataStore.edit { preferences ->
            preferences[Keys.PROFILE_NAME] = name.trim()
            preferences[Keys.PRIMARY_CATEGORY] = primaryCategory.name
            preferences[Keys.SELECTED_BODY] = bodyItemId
        }
    }

    suspend fun addGoal(title: String, category: GoalCategory) {
        if (title.isBlank()) return
        context.mvpDataStore.edit { preferences ->
            val goals = decodeGoals(preferences[Keys.GOALS]).toMutableList()
            goals += MvpGoal(
                id = System.currentTimeMillis(),
                title = title.trim(),
                category = category
            )
            preferences[Keys.GOALS] = encodeGoals(goals)
        }
    }

    suspend fun setPrimaryCategory(category: GoalCategory) {
        context.mvpDataStore.edit { preferences ->
            preferences[Keys.PRIMARY_CATEGORY] = category.name
        }
    }

    suspend fun completeGoal(goalId: Long) {
        context.mvpDataStore.edit { preferences ->
            val goals = decodeGoals(preferences[Keys.GOALS])
            val goal = goals.firstOrNull { it.id == goalId } ?: return@edit
            if (goal.isCompleted) return@edit

            val primaryCategory = preferences.category(Keys.PRIMARY_CATEGORY)
            val reward = RewardRules.completionReward(goal.category, primaryCategory)
            val today = LocalDate.now().toEpochDay()
            val currentStreak = preferences[Keys.CURRENT_STREAK] ?: 0
            val nextStreak = RewardRules.nextStreak(
                current = currentStreak,
                lastCompletionDay = preferences[Keys.LAST_COMPLETION_DAY] ?: Long.MIN_VALUE,
                currentDay = today
            )

            preferences[Keys.GOALS] = encodeGoals(
                goals.map { if (it.id == goalId) it.copy(isCompleted = true) else it }
            )
            preferences[Keys.EXPERIENCE] = (preferences[Keys.EXPERIENCE] ?: 0) + reward.experience
            preferences[Keys.COINS] = (preferences[Keys.COINS] ?: 0) + reward.coins
            preferences[Keys.TOTAL_COMPLETIONS] = (preferences[Keys.TOTAL_COMPLETIONS] ?: 0) + 1
            preferences[Keys.CURRENT_STREAK] = nextStreak
            preferences[Keys.LONGEST_STREAK] = maxOf(
                preferences[Keys.LONGEST_STREAK] ?: 0,
                nextStreak
            )
            preferences[Keys.LAST_COMPLETION_DAY] = today
        }
    }

    suspend fun deleteGoal(goalId: Long) {
        context.mvpDataStore.edit { preferences ->
            val goals = decodeGoals(preferences[Keys.GOALS]).filterNot { it.id == goalId }
            preferences[Keys.GOALS] = encodeGoals(goals)
        }
    }

    suspend fun selectAvatarItem(slot: AvatarSelection, itemId: String) {
        context.mvpDataStore.edit { preferences ->
            preferences[when (slot) {
                AvatarSelection.BODY -> Keys.SELECTED_BODY
                AvatarSelection.HAIR -> Keys.SELECTED_HAIR
                AvatarSelection.TOP -> Keys.SELECTED_TOP
            }] = itemId
        }
    }

    suspend fun setRoundGlasses(enabled: Boolean) {
        context.mvpDataStore.edit { it[Keys.ROUND_GLASSES] = enabled }
    }

    suspend fun setSoundEnabled(enabled: Boolean) {
        context.mvpDataStore.edit { it[Keys.SOUND_ENABLED] = enabled }
    }

    suspend fun setVibrationEnabled(enabled: Boolean) {
        context.mvpDataStore.edit { it[Keys.VIBRATION_ENABLED] = enabled }
    }

    private fun decodeState(preferences: Preferences) = MvpState(
        profileName = preferences[Keys.PROFILE_NAME].orEmpty(),
        primaryCategory = preferences.category(Keys.PRIMARY_CATEGORY),
        goals = decodeGoals(preferences[Keys.GOALS]),
        experience = preferences[Keys.EXPERIENCE] ?: 0,
        coins = preferences[Keys.COINS] ?: 0,
        currentStreak = preferences[Keys.CURRENT_STREAK] ?: 0,
        longestStreak = preferences[Keys.LONGEST_STREAK] ?: 0,
        totalCompletions = preferences[Keys.TOTAL_COMPLETIONS] ?: 0,
        selectedBodyId = preferences[Keys.SELECTED_BODY].orEmpty(),
        selectedHairId = preferences[Keys.SELECTED_HAIR] ?: "hair_curly",
        selectedTopId = preferences[Keys.SELECTED_TOP] ?: "top_tshirt",
        roundGlassesEnabled = preferences[Keys.ROUND_GLASSES] ?: false,
        soundEnabled = preferences[Keys.SOUND_ENABLED] ?: true,
        vibrationEnabled = preferences[Keys.VIBRATION_ENABLED] ?: true
    )

    private fun Preferences.category(key: Preferences.Key<String>): GoalCategory =
        this[key]?.let { value ->
            runCatching { GoalCategory.valueOf(value) }.getOrNull()
        } ?: GoalCategory.STUDY

    private fun encodeGoals(goals: List<MvpGoal>): String = goals.joinToString(";") { goal ->
        listOf(
            goal.id.toString(),
            encodeText(goal.title),
            goal.category.name,
            goal.isCompleted.toString()
        ).joinToString(",")
    }

    private fun decodeGoals(value: String?): List<MvpGoal> = value
        ?.split(';')
        ?.mapNotNull { encodedGoal ->
            val parts = encodedGoal.split(',')
            if (parts.size != 4) return@mapNotNull null
            runCatching {
                MvpGoal(
                    id = parts[0].toLong(),
                    title = decodeText(parts[1]),
                    category = GoalCategory.valueOf(parts[2]),
                    isCompleted = parts[3].toBooleanStrict()
                )
            }.getOrNull()
        }
        .orEmpty()

    private fun encodeText(value: String): String = Base64.getUrlEncoder()
        .withoutPadding()
        .encodeToString(value.toByteArray(StandardCharsets.UTF_8))

    private fun decodeText(value: String): String = String(
        Base64.getUrlDecoder().decode(value),
        StandardCharsets.UTF_8
    )

    enum class AvatarSelection { BODY, HAIR, TOP }

    private object Keys {
        val PROFILE_NAME = stringPreferencesKey("profile_name")
        val PRIMARY_CATEGORY = stringPreferencesKey("primary_category")
        val GOALS = stringPreferencesKey("goals")
        val EXPERIENCE = intPreferencesKey("experience")
        val COINS = intPreferencesKey("coins")
        val CURRENT_STREAK = intPreferencesKey("current_streak")
        val LONGEST_STREAK = intPreferencesKey("longest_streak")
        val TOTAL_COMPLETIONS = intPreferencesKey("total_completions")
        val LAST_COMPLETION_DAY = longPreferencesKey("last_completion_day")
        val SELECTED_BODY = stringPreferencesKey("selected_body")
        val SELECTED_HAIR = stringPreferencesKey("selected_hair")
        val SELECTED_TOP = stringPreferencesKey("selected_top")
        val ROUND_GLASSES = booleanPreferencesKey("round_glasses")
        val SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
        val VIBRATION_ENABLED = booleanPreferencesKey("vibration_enabled")
    }
}
