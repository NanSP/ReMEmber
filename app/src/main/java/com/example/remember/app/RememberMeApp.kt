package com.example.remember.app

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.material3.Scaffold
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import com.example.remember.R
import com.example.remember.feature.avatar.data.AvatarCatalog
import com.example.remember.feature.avatar.domain.model.AvatarColors
import com.example.remember.feature.avatar.domain.model.AvatarConfiguration
import com.example.remember.feature.avatar.domain.model.AvatarSlot
import com.example.remember.feature.goals.domain.model.GoalCategory
import com.example.remember.feature.mvp.data.MvpRepository
import com.example.remember.feature.mvp.data.MvpRepository.AvatarColorSelection
import com.example.remember.feature.mvp.data.PrivacyDataExporter
import com.example.remember.feature.mvp.data.PrimaryCategoryChangeStatus
import com.example.remember.feature.mvp.domain.BonusMissionCompletionResult
import com.example.remember.feature.mvp.domain.CompletionHistoryItem
import com.example.remember.feature.mvp.presentation.AvatarScreen
import com.example.remember.feature.mvp.presentation.GoalsScreen
import com.example.remember.feature.mvp.presentation.HistoryScreen
import com.example.remember.feature.mvp.presentation.LoadingScreen
import com.example.remember.feature.mvp.presentation.LauncherIconPreviewScreen
import com.example.remember.feature.mvp.presentation.MoreScreen
import com.example.remember.feature.mvp.presentation.NeoBottomBar
import com.example.remember.feature.mvp.presentation.NeoTodayScreen
import com.example.remember.feature.mvp.presentation.MvpPage
import com.example.remember.feature.mvp.presentation.OnboardingScreen
import com.example.remember.feature.mvp.presentation.ProfileScreen
import com.example.remember.feature.mvp.presentation.PrivacyScreen
import com.example.remember.feature.mvp.presentation.SettingsScreen
import com.example.remember.feature.mvp.presentation.StoreScreen
import com.example.remember.feature.mvp.presentation.backDestination
import com.example.remember.feature.store.data.StorePurchaseResult
import com.example.remember.feature.store.data.StoreCatalog
import com.example.remember.ui.theme.AppThemeMode
import kotlinx.coroutines.launch
import java.time.LocalDate

@Composable
fun RememberMeApp(
    scenePreviewStreak: Int? = null,
    scenePreviewCategory: GoalCategory? = null,
    initialPageName: String? = null,
    initialAvatarGroupName: String? = null,
    storeTestAction: String? = null,
    historyPreview: Boolean = false,
    loadingPreviewStage: Int? = null,
    launcherIconPreview: Boolean = false,
    themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    onThemeModeChange: (AppThemeMode) -> Unit = {}
) {
    if (launcherIconPreview) {
        LauncherIconPreviewScreen()
        return
    }
    val context = LocalContext.current
    val resources = LocalResources.current
    val repository = remember { MvpRepository(context.applicationContext) }
    val state by repository.state.collectAsState(initial = null)
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var initialLoadingFinished by rememberSaveable { mutableStateOf(false) }
    var page by rememberSaveable {
        mutableStateOf(MvpPage.entries.firstOrNull { it.name == initialPageName } ?: MvpPage.TODAY)
    }
    val currentState = state?.let { storedState ->
        if (!historyPreview) storedState else {
            val today = LocalDate.now().toEpochDay()
            storedState.copy(
                profileName = storedState.profileName.ifBlank { "Teste" },
                selectedBodyId = storedState.selectedBodyId.ifBlank { "body_03" },
                completionHistory = listOf(
                    CompletionHistoryItem(1, 101, "Estudar Kotlin", GoalCategory.STUDY, today,
                        System.currentTimeMillis(), 20, 5),
                    CompletionHistoryItem(2, 102, "Beber água", GoalCategory.FOOD_AND_HYDRATION,
                        today - 1, System.currentTimeMillis() - 86_400_000, 10, 2),
                    CompletionHistoryItem(3, 103, "Caminhar 20 minutos", GoalCategory.EXERCISE,
                        today - 8, System.currentTimeMillis() - 8 * 86_400_000, 15, 3)
                )
            )
        }
    }

    LaunchedEffect(storeTestAction) {
        when (storeTestAction) {
            "BEGIN" -> repository.beginStorePurchaseTest()
            "BUY_PUNK" -> if (
                repository.purchaseBundle(StoreCatalog.PUNK_ID) == StorePurchaseResult.PURCHASED
            ) {
                repository.equipBundle(StoreCatalog.PUNK_ID)
            }
            "BUY_WITCH" -> if (
                repository.purchaseBundle(StoreCatalog.WITCH_ID) == StorePurchaseResult.PURCHASED
            ) {
                repository.equipBundle(StoreCatalog.WITCH_ID)
            }
            "RESET" -> repository.finishStorePurchaseTest()
        }
    }

    if (!initialLoadingFinished) {
        LoadingScreen(
            readyToContinue = currentState != null,
            previewStage = loadingPreviewStage,
            onFinished = { initialLoadingFinished = true }
        )
        return
    }

    if (currentState == null) return

    if (!currentState.isOnboardingComplete) {
        OnboardingScreen(state = currentState) { name, category, bodyItemId ->
            scope.launch { repository.saveProfile(name, category, bodyItemId) }
        }
        return
    }

    val goalCompletedMessage = stringResource(R.string.goal_completed_feedback)
    val goalAddedMessage = stringResource(R.string.goal_added_feedback)
    val goalUpdatedMessage = stringResource(R.string.goal_updated_feedback)
    val bonusMissionCompletedMessage = stringResource(R.string.bonus_mission_completed)
    val bonusMissionAlreadyCompletedMessage =
        stringResource(R.string.bonus_mission_already_completed)
    val bonusMissionNotStartedMessage = stringResource(R.string.bonus_mission_not_started)
    val bonusMissionTimerRunningMessage = stringResource(R.string.bonus_mission_timer_running)
    val privacyNameUpdatedMessage = stringResource(R.string.privacy_name_updated)
    val privacyExportChooser = stringResource(R.string.privacy_export_chooser)
    val privacyExportSubject = stringResource(R.string.privacy_export_subject)
    val privacyContactUrl = stringResource(R.string.privacy_contact_url_full)
    val streakRecoveredMessage = stringResource(R.string.streak_recovered)
    val streakRecoveryUnavailableMessage = stringResource(R.string.streak_recovery_unavailable)
    val primaryCategoryChangedMessage = stringResource(R.string.primary_category_changed)
    val storePurchaseSuccess = stringResource(R.string.store_purchase_success)
    val storeAlreadyOwned = stringResource(R.string.store_purchase_already_owned)
    val storeInsufficientCoins = stringResource(R.string.store_purchase_insufficient)
    val storePurchaseError = stringResource(R.string.store_purchase_error)
    val storeEquipSuccess = stringResource(R.string.store_equip_success)
    val avatarConfiguration = remember(
        currentState.selectedBodyId,
        currentState.selectedFaceId,
        currentState.selectedEyesId,
        currentState.selectedBeardId,
        currentState.selectedHairId,
        currentState.selectedGlassesId,
        currentState.selectedShirtId,
        currentState.selectedHatId,
        currentState.avatarSkinColor,
        currentState.avatarHairColor,
        currentState.avatarEyeColor,
        currentState.avatarShirtColor,
        currentState.avatarBeardColor,
        currentState.avatarGlassesColor,
        currentState.avatarHatColor
    ) {
        val equipped = AvatarCatalog.defaultConfiguration.equippedItemIds.toMutableMap()
        equipped[AvatarSlot.BODY] = currentState.selectedBodyId
        equipped[AvatarSlot.FACE] = currentState.selectedFaceId
        equipped[AvatarSlot.EYES] = currentState.selectedEyesId
        equipped[AvatarSlot.BEARD] = currentState.selectedBeardId
        equipped[AvatarSlot.HAIR] = currentState.selectedHairId
        equipped[AvatarSlot.GLASSES] = currentState.selectedGlassesId
        equipped[AvatarSlot.SHIRT] = currentState.selectedShirtId
        equipped[AvatarSlot.HAT] = currentState.selectedHatId
        AvatarConfiguration(
            equippedItemIds = equipped,
            colors = AvatarColors(
                skin = currentState.avatarSkinColor,
                hair = currentState.avatarHairColor,
                eyes = currentState.avatarEyeColor,
                shirt = currentState.avatarShirtColor,
                beard = currentState.avatarBeardColor,
                glasses = currentState.avatarGlassesColor,
                hat = currentState.avatarHatColor
            )
        )
    }

    LaunchedEffect(Unit) {
        repository.rescheduleReminders()
    }

    val backDestination = page.backDestination()
    BackHandler(enabled = backDestination != null) {
        backDestination?.let { page = it }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (page.isRoot) NeoBottomBar(page = page, onNavigate = { page = it })
        }
    ) { contentPadding ->
        when (page) {
            MvpPage.TODAY -> NeoTodayScreen(
                state = currentState,
                avatarConfiguration = avatarConfiguration,
                scenePreviewStreak = scenePreviewStreak,
                scenePreviewCategory = scenePreviewCategory,
                contentPadding = contentPadding,
                onCreateGoal = { page = MvpPage.GOALS },
                onSelectPrimaryCategory = { category ->
                    scope.launch {
                        val result = repository.setPrimaryCategory(category)
                        when (result.status) {
                            PrimaryCategoryChangeStatus.CHANGED ->
                                snackbarHostState.showSnackbar(primaryCategoryChangedMessage)
                            PrimaryCategoryChangeStatus.LOCKED ->
                                snackbarHostState.showSnackbar(
                                    resources.getQuantityString(
                                        R.plurals.primary_category_locked,
                                        result.daysRemaining,
                                        result.daysRemaining
                                    )
                                )
                            PrimaryCategoryChangeStatus.ALREADY_SELECTED -> Unit
                        }
                    }
                },
                onCompleteGoal = { goalId ->
                    scope.launch {
                        repository.completeGoal(goalId)
                        snackbarHostState.showSnackbar(goalCompletedMessage)
                    }
                },
                onCompleteBonusMission = {
                    scope.launch {
                        val result = repository.completeBonusMission()
                        snackbarHostState.showSnackbar(
                            when (result) {
                                BonusMissionCompletionResult.COMPLETED ->
                                    bonusMissionCompletedMessage
                                BonusMissionCompletionResult.ALREADY_COMPLETED ->
                                    bonusMissionAlreadyCompletedMessage
                                BonusMissionCompletionResult.NOT_STARTED ->
                                    bonusMissionNotStartedMessage
                                BonusMissionCompletionResult.TIMER_RUNNING ->
                                    bonusMissionTimerRunningMessage
                            }
                        )
                    }
                },
                onStartBonusMission = {
                    scope.launch { repository.startBonusMission() }
                },
                onRecoverStreak = {
                    scope.launch {
                        val recovered = repository.recoverStreak()
                        snackbarHostState.showSnackbar(
                            if (recovered) streakRecoveredMessage
                            else streakRecoveryUnavailableMessage
                        )
                    }
                }
            )
            MvpPage.GOALS -> GoalsScreen(
                state = currentState,
                contentPadding = contentPadding,
                onSaveGoal = { goalId, title, recurrence, reminderMinutes ->
                    scope.launch {
                        if (goalId == null) {
                            repository.addGoal(
                                title,
                                recurrence,
                                reminderMinutes
                            )
                            snackbarHostState.showSnackbar(goalAddedMessage)
                        } else {
                            repository.updateGoal(
                                goalId,
                                title,
                                recurrence,
                                reminderMinutes
                            )
                            snackbarHostState.showSnackbar(goalUpdatedMessage)
                        }
                    }
                },
                onCompleteGoal = { scope.launch { repository.completeGoal(it) } },
                onDeleteGoal = { scope.launch { repository.deleteGoal(it) } }
            )
            MvpPage.AVATAR -> AvatarScreen(
                state = currentState,
                avatarConfiguration = avatarConfiguration,
                initialGroupName = initialAvatarGroupName,
                contentPadding = contentPadding,
                onSelectItem = { slot, itemId ->
                    scope.launch { repository.selectAvatarItem(slot, itemId) }
                },
                onSelectColor = { target: AvatarColorSelection, color: String ->
                    scope.launch { repository.selectAvatarColor(target, color) }
                }
            )
            MvpPage.MORE -> MoreScreen(contentPadding = contentPadding, onNavigate = { page = it })
            MvpPage.PROFILE -> ProfileScreen(
                state = currentState,
                avatarConfiguration = avatarConfiguration,
                contentPadding = contentPadding,
                onBack = { page = MvpPage.MORE }
            )
            MvpPage.HISTORY -> HistoryScreen(
                state = currentState,
                contentPadding = contentPadding,
                onBack = { page = MvpPage.MORE }
            )
            MvpPage.STORE -> StoreScreen(
                state = currentState,
                avatarConfiguration = avatarConfiguration,
                contentPadding = contentPadding,
                onBack = { page = MvpPage.MORE },
                onPurchase = { bundleId ->
                    scope.launch {
                        val message = when (repository.purchaseBundle(bundleId)) {
                            StorePurchaseResult.PURCHASED -> storePurchaseSuccess
                            StorePurchaseResult.ALREADY_OWNED -> storeAlreadyOwned
                            StorePurchaseResult.INSUFFICIENT_COINS -> storeInsufficientCoins
                            StorePurchaseResult.UNKNOWN_BUNDLE -> storePurchaseError
                        }
                        snackbarHostState.showSnackbar(message)
                    }
                },
                onEquip = { bundleId ->
                    scope.launch {
                        if (repository.equipBundle(bundleId)) {
                            snackbarHostState.showSnackbar(storeEquipSuccess)
                        } else {
                            snackbarHostState.showSnackbar(storePurchaseError)
                        }
                    }
                }
            )
            MvpPage.SETTINGS -> SettingsScreen(
                state = currentState,
                contentPadding = contentPadding,
                onBack = { page = MvpPage.MORE },
                themeMode = themeMode,
                onThemeModeChange = onThemeModeChange,
                onSoundChange = { scope.launch { repository.setSoundEnabled(it) } },
                onVibrationChange = { scope.launch { repository.setVibrationEnabled(it) } }
            )
            MvpPage.PRIVACY -> PrivacyScreen(
                state = currentState,
                contentPadding = contentPadding,
                onBack = { page = MvpPage.MORE },
                onUpdateProfileName = { name ->
                    scope.launch {
                        repository.updateProfileName(name)
                        snackbarHostState.showSnackbar(privacyNameUpdatedMessage)
                    }
                },
                onExportData = {
                    val exportIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "application/json"
                        putExtra(Intent.EXTRA_SUBJECT, privacyExportSubject)
                        putExtra(Intent.EXTRA_TEXT, PrivacyDataExporter.toJson(currentState))
                    }
                    context.startActivity(Intent.createChooser(exportIntent, privacyExportChooser))
                },
                onOpenContact = {
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(privacyContactUrl)))
                },
                onDeleteAllData = {
                    scope.launch {
                        repository.deleteAllUserData()
                        onThemeModeChange(AppThemeMode.SYSTEM)
                        page = MvpPage.TODAY
                    }
                }
            )
        }
    }
}
