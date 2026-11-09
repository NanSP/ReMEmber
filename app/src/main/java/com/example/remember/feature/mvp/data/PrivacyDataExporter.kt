package com.example.remember.feature.mvp.data

import com.example.remember.feature.mvp.domain.MvpState
import java.time.Instant
import org.json.JSONArray
import org.json.JSONObject

object PrivacyDataExporter {
    fun toJson(state: MvpState, generatedAtMillis: Long = System.currentTimeMillis()): String =
        JSONObject().apply {
            put("schemaVersion", 1)
            put("source", "ReMEmber")
            put("generatedAt", Instant.ofEpochMilli(generatedAtMillis).toString())
            put("profile", JSONObject().apply {
                put("name", state.profileName)
                put("primaryCategory", state.primaryCategory.name)
            })
            put("goals", JSONArray().apply {
                state.goals.forEach { goal ->
                    put(JSONObject().apply {
                        put("id", goal.id)
                        put("title", goal.title)
                        put("category", goal.category.name)
                        put("completed", goal.isCompleted)
                        put("recurrence", goal.recurrence.name)
                        put("lastCompletedEpochDay", goal.lastCompletedEpochDay ?: JSONObject.NULL)
                        put("reminderMinutesOfDay", goal.reminderMinutesOfDay ?: JSONObject.NULL)
                        put("reminderEnabled", goal.reminderEnabled)
                    })
                }
            })
            put("completionHistory", JSONArray().apply {
                state.completionHistory.forEach { completion ->
                    put(JSONObject().apply {
                        put("goalId", completion.goalId)
                        put("goalTitle", completion.goalTitle)
                        put("category", completion.category.name)
                        put("completedEpochDay", completion.completedEpochDay)
                        put("completedAtMillis", completion.completedAtMillis)
                        put("experienceEarned", completion.experienceEarned)
                        put("coinsEarned", completion.coinsEarned)
                    })
                }
            })
            put("progress", JSONObject().apply {
                put("experience", state.experience)
                put("coins", state.coins)
                put("currentStreak", state.currentStreak)
                put("longestStreak", state.longestStreak)
                put("totalCompletions", state.totalCompletions)
                put("unlockedAchievements", JSONArray(state.unlockedAchievements.map { it.name }.sorted()))
                put("ownedBundleIds", JSONArray(state.ownedBundleIds.sorted()))
            })
            put("avatar", JSONObject().apply {
                put("body", state.selectedBodyId)
                put("face", state.selectedFaceId)
                put("eyes", state.selectedEyesId)
                put("beard", state.selectedBeardId)
                put("hair", state.selectedHairId)
                put("glasses", state.selectedGlassesId)
                put("shirt", state.selectedShirtId)
                put("hat", state.selectedHatId)
                put("skinColor", state.avatarSkinColor)
                put("hairColor", state.avatarHairColor)
                put("eyeColor", state.avatarEyeColor)
                put("shirtColor", state.avatarShirtColor)
                put("beardColor", state.avatarBeardColor)
                put("glassesColor", state.avatarGlassesColor)
                put("hatColor", state.avatarHatColor)
            })
            put("preferences", JSONObject().apply {
                put("soundEnabled", state.soundEnabled)
                put("vibrationEnabled", state.vibrationEnabled)
                put("privacyNoticeVersion", state.privacyNoticeVersion ?: JSONObject.NULL)
                put(
                    "privacyNoticeAcknowledgedAtMillis",
                    state.privacyNoticeAcknowledgedAtMillis ?: JSONObject.NULL
                )
            })
        }.toString(2)
}
