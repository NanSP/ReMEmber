package com.example.remember

import android.app.ActivityManager
import android.content.pm.ApplicationInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.graphics.drawable.toBitmap
import com.example.remember.app.RememberMeApp
import com.example.remember.feature.goals.domain.model.GoalCategory
import com.example.remember.ui.theme.ReMEmberTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        applyTaskDescription()
        enableEdgeToEdge()
        val isDebuggable = applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0
        val scenePreviewStreak = if (isDebuggable) {
            intent.getIntExtra(EXTRA_SCENE_PREVIEW_STREAK, -1).takeIf { it >= 0 }
        } else {
            null
        }
        val scenePreviewCategory = if (isDebuggable) {
            intent.getStringExtra(EXTRA_SCENE_PREVIEW_CATEGORY)?.let { value ->
                runCatching { GoalCategory.valueOf(value) }.getOrNull()
            }
        } else {
            null
        }
        val initialPageName = if (isDebuggable) {
            intent.getStringExtra(EXTRA_INITIAL_PAGE)
        } else {
            null
        }
        val initialAvatarGroupName = if (isDebuggable) {
            intent.getStringExtra(EXTRA_INITIAL_AVATAR_GROUP)
        } else {
            null
        }
        val storeTestAction = if (isDebuggable) {
            intent.getStringExtra(EXTRA_STORE_TEST_ACTION)
        } else {
            null
        }
        val historyPreview = isDebuggable && intent.getBooleanExtra(EXTRA_HISTORY_PREVIEW, false)
        val loadingPreviewStage = if (isDebuggable) {
            intent.getIntExtra(EXTRA_LOADING_PREVIEW_STAGE, -1).takeIf { it in 0..3 }
        } else {
            null
        }
        val launcherIconPreview =
            isDebuggable && intent.getBooleanExtra(EXTRA_LAUNCHER_ICON_PREVIEW, false)
        setContent {
            ReMEmberTheme {
                RememberMeApp(
                    scenePreviewStreak = scenePreviewStreak,
                    scenePreviewCategory = scenePreviewCategory,
                    initialPageName = initialPageName,
                    initialAvatarGroupName = initialAvatarGroupName,
                    storeTestAction = storeTestAction,
                    historyPreview = historyPreview,
                    loadingPreviewStage = loadingPreviewStage,
                    launcherIconPreview = launcherIconPreview
                )
            }
        }
    }

    @Suppress("DEPRECATION")
    private fun applyTaskDescription() {
        val taskIcon = applicationInfo.loadIcon(packageManager).toBitmap(
            width = TASK_ICON_SIZE,
            height = TASK_ICON_SIZE
        )
        setTaskDescription(
            ActivityManager.TaskDescription(
                getString(R.string.app_name),
                taskIcon,
                getColor(R.color.neo_background)
            )
        )
    }

    private companion object {
        const val TASK_ICON_SIZE = 192
        const val EXTRA_SCENE_PREVIEW_STREAK = "scene_preview_streak"
        const val EXTRA_SCENE_PREVIEW_CATEGORY = "scene_preview_category"
        const val EXTRA_INITIAL_PAGE = "initial_page"
        const val EXTRA_INITIAL_AVATAR_GROUP = "initial_avatar_group"
        const val EXTRA_STORE_TEST_ACTION = "store_test_action"
        const val EXTRA_HISTORY_PREVIEW = "history_preview"
        const val EXTRA_LOADING_PREVIEW_STAGE = "loading_preview_stage"
        const val EXTRA_LAUNCHER_ICON_PREVIEW = "launcher_icon_preview"
    }
}
