package com.example.remember.app

import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.example.remember.R
import com.example.remember.feature.avatar.data.AvatarCatalog
import com.example.remember.feature.avatar.domain.model.AvatarConfiguration
import com.example.remember.feature.avatar.domain.model.AvatarSlot
import com.example.remember.feature.mvp.data.MvpRepository
import com.example.remember.feature.mvp.presentation.AvatarScreen
import com.example.remember.feature.mvp.presentation.GoalsScreen
import com.example.remember.feature.mvp.presentation.LoadingScreen
import com.example.remember.feature.mvp.presentation.MoreScreen
import com.example.remember.feature.mvp.presentation.NeoBottomBar
import com.example.remember.feature.mvp.presentation.NeoTodayScreen
import com.example.remember.feature.mvp.presentation.MvpPage
import com.example.remember.feature.mvp.presentation.OnboardingScreen
import com.example.remember.feature.mvp.presentation.ProfileScreen
import com.example.remember.feature.mvp.presentation.SettingsScreen
import com.example.remember.feature.mvp.presentation.StoreScreen
import kotlinx.coroutines.launch

@Composable
fun RememberMeApp() {
    val context = LocalContext.current
    val repository = remember { MvpRepository(context.applicationContext) }
    val state by repository.state.collectAsState(initial = null)
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var page by rememberSaveable { mutableStateOf(MvpPage.TODAY) }
    val currentState = state

    if (currentState == null) {
        LoadingScreen()
        return
    }

    if (!currentState.isOnboardingComplete) {
        OnboardingScreen(state = currentState) { name, category, bodyItemId ->
            scope.launch { repository.saveProfile(name, category, bodyItemId) }
        }
        return
    }

    val goalCompletedMessage = stringResource(R.string.goal_completed_feedback)
    val goalAddedMessage = stringResource(R.string.goal_added_feedback)
    val avatarConfiguration = remember(
        currentState.selectedBodyId,
        currentState.selectedHairId,
        currentState.selectedTopId,
        currentState.roundGlassesEnabled
    ) {
        val equipped = AvatarCatalog.defaultConfiguration.equippedItemIds.toMutableMap()
        equipped[AvatarSlot.BODY] = currentState.selectedBodyId
        equipped[AvatarSlot.HAIR] = currentState.selectedHairId
        equipped[AvatarSlot.TOP] = currentState.selectedTopId
        if (currentState.roundGlassesEnabled) {
            equipped[AvatarSlot.GLASSES] = "glasses_round"
        } else {
            equipped.remove(AvatarSlot.GLASSES)
        }
        AvatarConfiguration(equipped)
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (page.isRoot) NeoBottomBar(page = page, onNavigate = { page = it })
        }
    ) { contentPadding ->
        when (page) {
            MvpPage.TODAY -> NeoTodayScreen(
                state = currentState,
                avatarConfiguration = avatarConfiguration,
                contentPadding = contentPadding,
                onCreateGoal = { page = MvpPage.GOALS },
                onSelectPrimaryCategory = { category ->
                    scope.launch { repository.setPrimaryCategory(category) }
                },
                onCompleteGoal = { goalId ->
                    scope.launch {
                        repository.completeGoal(goalId)
                        snackbarHostState.showSnackbar(goalCompletedMessage)
                    }
                }
            )
            MvpPage.GOALS -> GoalsScreen(
                state = currentState,
                contentPadding = contentPadding,
                onAddGoal = { title, category ->
                    scope.launch {
                        repository.addGoal(title, category)
                        snackbarHostState.showSnackbar(goalAddedMessage)
                    }
                },
                onCompleteGoal = { scope.launch { repository.completeGoal(it) } },
                onDeleteGoal = { scope.launch { repository.deleteGoal(it) } }
            )
            MvpPage.AVATAR -> AvatarScreen(
                state = currentState,
                avatarConfiguration = avatarConfiguration,
                contentPadding = contentPadding,
                onSelectItem = { slot, itemId ->
                    scope.launch { repository.selectAvatarItem(slot, itemId) }
                },
                onToggleGlasses = { scope.launch { repository.setRoundGlasses(it) } }
            )
            MvpPage.MORE -> MoreScreen(contentPadding = contentPadding, onNavigate = { page = it })
            MvpPage.PROFILE -> ProfileScreen(
                state = currentState,
                avatarConfiguration = avatarConfiguration,
                contentPadding = contentPadding,
                onBack = { page = MvpPage.MORE }
            )
            MvpPage.STORE -> StoreScreen(
                contentPadding = contentPadding,
                onBack = { page = MvpPage.MORE }
            )
            MvpPage.SETTINGS -> SettingsScreen(
                state = currentState,
                contentPadding = contentPadding,
                onBack = { page = MvpPage.MORE },
                onSoundChange = { scope.launch { repository.setSoundEnabled(it) } },
                onVibrationChange = { scope.launch { repository.setVibrationEnabled(it) } }
            )
        }
    }
}
