package com.example.remember.feature.mvp.presentation

import android.Manifest
import android.app.TimePickerDialog
import android.os.Build
import androidx.annotation.StringRes
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.example.remember.R
import com.example.remember.feature.avatar.data.AvatarCatalog
import com.example.remember.feature.avatar.domain.model.AvatarConfiguration
import com.example.remember.feature.avatar.domain.model.AvatarSlot
import com.example.remember.feature.avatar.presentation.AvatarRenderer
import com.example.remember.feature.goals.domain.model.GoalCategory
import com.example.remember.feature.mvp.data.MvpRepository.AvatarSelection
import com.example.remember.feature.mvp.data.MvpRepository.AvatarColorSelection
import com.example.remember.feature.mvp.domain.MvpGoal
import com.example.remember.feature.mvp.domain.MvpState
import com.example.remember.feature.mvp.domain.GoalRecurrence
import com.example.remember.feature.mvp.domain.AchievementId
import com.example.remember.feature.store.data.StoreBundle
import com.example.remember.feature.store.data.StoreCatalog
import com.example.remember.ui.components.neomorphicTextFieldColors
import com.example.remember.ui.components.NeomorphicButton
import com.example.remember.ui.components.NeomorphicCard
import com.example.remember.ui.components.NeomorphicChoice
import com.example.remember.ui.components.NeomorphicProgressBar
import com.example.remember.ui.components.NeomorphicSwitch
import com.example.remember.ui.components.neomorphicInset
import com.example.remember.ui.theme.NeoBackground
import com.example.remember.ui.theme.NeoButton
import com.example.remember.ui.theme.NeoElement

enum class MvpPage(val isRoot: Boolean = false) {
    TODAY(true), GOALS(true), AVATAR(true), MORE(true), PROFILE, HISTORY, STORE(true), SETTINGS
}

@Composable
fun OnboardingScreen(
    state: MvpState,
    onContinue: (String, GoalCategory, String) -> Unit
) {
    var name by rememberSaveable { mutableStateOf(state.profileName) }
    var category by rememberSaveable { mutableStateOf(state.primaryCategory) }
    var bodyItemId by rememberSaveable {
        mutableStateOf(state.selectedBodyId.ifBlank { "body_03" })
    }
    var showError by rememberSaveable { mutableStateOf(false) }
    val previewConfiguration = AvatarConfiguration(
        AvatarCatalog.defaultConfiguration.equippedItemIds + (AvatarSlot.BODY to bodyItemId)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NeoBackground)
            .systemBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 40.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        NeomorphicCard(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(22.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = stringResource(R.string.onboarding_eyebrow),
                    color = NeoButton,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stringResource(R.string.onboarding_title),
                    color = NeoButton,
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = stringResource(R.string.onboarding_description),
                    style = MaterialTheme.typography.bodyLarge,
                    color = NeoButton
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(stringResource(R.string.your_name), fontWeight = FontWeight.Bold)
        OutlinedTextField(
            value = name,
            onValueChange = { name = it; showError = false },
            modifier = Modifier
                .fillMaxWidth()
                .neomorphicInset(RoundedCornerShape(16.dp)),
            singleLine = true,
            label = { Text(stringResource(R.string.name_placeholder)) },
            isError = showError,
            colors = neomorphicTextFieldColors(),
            supportingText = {
                if (showError) Text(stringResource(R.string.name_required))
            }
        )
        Text(stringResource(R.string.body_type), fontWeight = FontWeight.Bold)
        Box(
            modifier = Modifier.fillMaxWidth().height(236.dp).neomorphicInset(RoundedCornerShape(24.dp)),
            contentAlignment = Alignment.Center
        ) {
            AvatarRenderer(
                configuration = previewConfiguration,
                contentDescription = stringResource(R.string.avatar_preview),
                modifier = Modifier.fillMaxWidth().height(220.dp)
            )
        }
        ChoiceSection(
            title = R.string.body_type,
            choices = bodyChoices(),
            selected = bodyItemId,
            onSelect = { bodyItemId = it }
        )
        Text(stringResource(R.string.primary_focus), fontWeight = FontWeight.Bold)
        CategorySelector(selected = category, onSelect = { category = it })
        NeomorphicButton(
            onClick = {
                if (name.isBlank()) showError = true else onContinue(name, category, bodyItemId)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.start_journey))
        }
    }
}

@Composable
fun MvpBottomBar(page: MvpPage, onNavigate: (MvpPage) -> Unit) {
    val destinations = listOf(
        Triple(MvpPage.TODAY, R.string.nav_today, "01"),
        Triple(MvpPage.GOALS, R.string.nav_goals, "02"),
        Triple(MvpPage.AVATAR, R.string.nav_avatar, "03"),
        Triple(MvpPage.MORE, R.string.nav_more, "04")
    )
    NavigationBar {
        destinations.forEach { (destination, label, marker) ->
            NavigationBarItem(
                selected = page == destination,
                onClick = { onNavigate(destination) },
                icon = { Text(marker, style = MaterialTheme.typography.labelSmall) },
                label = { Text(stringResource(label)) }
            )
        }
    }
}

@Composable
fun TodayScreen(
    state: MvpState,
    avatarConfiguration: AvatarConfiguration,
    contentPadding: PaddingValues,
    onCreateGoal: () -> Unit,
    onCompleteGoal: (Long) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 20.dp,
            top = contentPadding.calculateTopPadding() + 24.dp,
            end = 20.dp,
            bottom = contentPadding.calculateBottomPadding() + 24.dp
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                stringResource(R.string.greeting, state.profileName),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Black
            )
            Text(
                stringResource(R.string.today_invitation),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        item { StatsRow(state) }
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(28.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        stringResource(R.string.main_focus),
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.75f),
                        style = MaterialTheme.typography.labelMedium
                    )
                    Text(
                        categoryLabel(state.primaryCategory),
                        color = MaterialTheme.colorScheme.onPrimary,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    AvatarRenderer(
                        configuration = avatarConfiguration,
                        contentDescription = stringResource(R.string.avatar_content_description, state.profileName),
                        modifier = Modifier.size(230.dp)
                    )
                    Text(
                        stringResource(R.string.level_number, state.level),
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    LinearProgressIndicator(
                        progress = { state.levelProgress },
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        stringResource(R.string.xp_progress, state.experience % MvpState.EXPERIENCE_PER_LEVEL),
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }
            }
        }
        item {
            Text(
                stringResource(R.string.today_goals),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }
        if (state.goals.isEmpty()) {
            item {
                EmptyGoals(onCreateGoal)
            }
        } else {
            items(state.goals, key = { it.id }) { goal ->
                GoalCard(goal = goal, onComplete = { onCompleteGoal(goal.id) })
            }
        }
    }
}

@Composable
private fun StatsRow(state: MvpState) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        StatCard(stringResource(R.string.streak), stringResource(R.string.streak_days, state.currentStreak), Modifier.weight(1f))
        StatCard(stringResource(R.string.coins), state.coins.toString(), Modifier.weight(1f))
        StatCard(stringResource(R.string.level), state.level.toString(), Modifier.weight(1f))
    }
}

@Composable
private fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(Modifier.padding(12.dp)) {
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun EmptyGoals(onCreateGoal: () -> Unit) {
    Card {
        Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(stringResource(R.string.no_goals_today))
            Button(onClick = onCreateGoal) { Text(stringResource(R.string.create_first_goal)) }
        }
    }
}

@Composable
fun GoalsScreen(
    state: MvpState,
    contentPadding: PaddingValues,
    onSaveGoal: (Long?, String, GoalCategory, GoalRecurrence, Int?) -> Unit,
    onCompleteGoal: (Long) -> Unit,
    onDeleteGoal: (Long) -> Unit
) {
    var title by rememberSaveable { mutableStateOf("") }
    var category by rememberSaveable { mutableStateOf(state.primaryCategory) }
    var recurrenceName by rememberSaveable { mutableStateOf(GoalRecurrence.NONE.name) }
    var reminderEnabled by rememberSaveable { mutableStateOf(false) }
    var reminderMinutes by rememberSaveable { mutableStateOf(9 * 60) }
    var editingGoalId by rememberSaveable { mutableStateOf<Long?>(null) }
    val recurrence = GoalRecurrence.valueOf(recurrenceName)
    val context = LocalContext.current
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    fun resetEditor() {
        title = ""
        category = state.primaryCategory
        recurrenceName = GoalRecurrence.NONE.name
        reminderEnabled = false
        reminderMinutes = 9 * 60
        editingGoalId = null
    }

    fun edit(goal: MvpGoal) {
        editingGoalId = goal.id
        title = goal.title
        category = goal.category
        recurrenceName = goal.recurrence.name
        reminderEnabled = goal.reminderEnabled
        reminderMinutes = goal.reminderMinutesOfDay ?: 9 * 60
    }

    fun showTimePicker() {
        TimePickerDialog(
            context,
            { _, hour, minute -> reminderMinutes = hour * 60 + minute },
            reminderMinutes / 60,
            reminderMinutes % 60,
            true
        ).show()
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            20.dp,
            contentPadding.calculateTopPadding() + 24.dp,
            20.dp,
            contentPadding.calculateBottomPadding() + 24.dp
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { ScreenHeading(R.string.goals_title, R.string.goals_description) }
        item {
            NeomorphicCard(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        stringResource(
                            if (editingGoalId == null) R.string.new_goal else R.string.edit_goal
                        ),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .neomorphicInset(RoundedCornerShape(16.dp)),
                        label = { Text(stringResource(R.string.goal_title)) },
                        placeholder = { Text(stringResource(R.string.goal_placeholder)) },
                        singleLine = true,
                        colors = neomorphicTextFieldColors()
                    )
                    Text(stringResource(R.string.category), fontWeight = FontWeight.Bold)
                    CategorySelector(category, onSelect = { category = it })
                    Text(stringResource(R.string.goal_recurrence), fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        GoalRecurrence.entries.forEach { option ->
                            NeomorphicChoice(
                                selected = option == recurrence,
                                onClick = { recurrenceName = option.name },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    recurrenceLabel(option),
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(stringResource(R.string.goal_reminder), fontWeight = FontWeight.Bold)
                            Text(
                                stringResource(R.string.goal_reminder_description),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        NeomorphicSwitch(
                            checked = reminderEnabled,
                            onCheckedChange = { enabled ->
                                reminderEnabled = enabled
                                if (enabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                }
                            }
                        )
                    }
                    if (reminderEnabled) {
                        NeomorphicButton(
                            onClick = ::showTimePicker,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(stringResource(R.string.goal_reminder_time, formatMinutes(reminderMinutes)))
                        }
                    }
                    NeomorphicButton(
                        onClick = {
                            onSaveGoal(
                                editingGoalId,
                                title,
                                category,
                                recurrence,
                                reminderMinutes.takeIf { reminderEnabled }
                            )
                            resetEditor()
                        },
                        enabled = title.isNotBlank(),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            stringResource(
                                if (editingGoalId == null) R.string.add_goal else R.string.save_changes
                            )
                        )
                    }
                    if (editingGoalId != null) {
                        TextButton(onClick = ::resetEditor, modifier = Modifier.fillMaxWidth()) {
                            Text(stringResource(R.string.cancel_editing))
                        }
                    }
                }
            }
        }
        item { Text(stringResource(R.string.active_goals), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) }
        if (state.goals.isEmpty()) {
            item { Text(stringResource(R.string.empty_goals), color = MaterialTheme.colorScheme.onSurfaceVariant) }
        } else {
            items(state.goals, key = { it.id }) { goal ->
                GoalCard(
                    goal = goal,
                    onComplete = { onCompleteGoal(goal.id) },
                    onEdit = { edit(goal) },
                    onDelete = { onDeleteGoal(goal.id) }
                )
            }
        }
    }
}

@Composable
private fun GoalCard(
    goal: MvpGoal,
    onComplete: () -> Unit,
    onEdit: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null
) {
    NeomorphicCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = if (goal.isCompleted) NeoElement else NeoBackground
    ) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                goal.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textDecoration = if (goal.isCompleted) TextDecoration.LineThrough else null
            )
            Text(categoryLabel(goal.category), color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelLarge)
            if (goal.recurrence != GoalRecurrence.NONE) {
                Text(
                    stringResource(R.string.goal_recurrence_value, recurrenceLabel(goal.recurrence)),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            if (goal.reminderEnabled && goal.reminderMinutesOfDay != null) {
                Text(
                    stringResource(
                        R.string.goal_reminder_value,
                        formatMinutes(goal.reminderMinutesOfDay)
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (goal.isCompleted) {
                    Text(stringResource(R.string.completed), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                } else {
                    NeomorphicButton(onClick = onComplete) { Text(stringResource(R.string.complete)) }
                }
                Spacer(Modifier.weight(1f))
                if (onEdit != null) {
                    TextButton(onClick = onEdit) { Text(stringResource(R.string.edit)) }
                }
                if (onDelete != null) TextButton(onClick = onDelete) { Text(stringResource(R.string.delete)) }
            }
        }
    }
}

@Composable
private fun recurrenceLabel(recurrence: GoalRecurrence): String = stringResource(
    when (recurrence) {
        GoalRecurrence.NONE -> R.string.recurrence_none
        GoalRecurrence.DAILY -> R.string.recurrence_daily
        GoalRecurrence.WEEKLY -> R.string.recurrence_weekly
    }
)

private fun formatMinutes(minutes: Int): String =
    "%02d:%02d".format(minutes / 60, minutes % 60)

@Composable
fun AvatarScreen(
    state: MvpState,
    avatarConfiguration: AvatarConfiguration,
    initialGroupName: String? = null,
    contentPadding: PaddingValues,
    onSelectItem: (AvatarSelection, String) -> Unit,
    onSelectColor: (AvatarColorSelection, String) -> Unit
) {
    var selectedGroupName by rememberSaveable {
        mutableStateOf(
            AvatarEditorGroup.entries.firstOrNull { it.name == initialGroupName }?.name
                ?: AvatarEditorGroup.BODY.name
        )
    }
    val selectedGroup = AvatarEditorGroup.valueOf(selectedGroupName)

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp, contentPadding.calculateTopPadding() + 24.dp, 20.dp, contentPadding.calculateBottomPadding() + 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item { ScreenHeading(R.string.avatar_title, R.string.avatar_description_tabs) }
        item {
            Box(
                modifier = Modifier.fillMaxWidth().height(286.dp).neomorphicInset(RoundedCornerShape(28.dp)),
                contentAlignment = Alignment.Center
            ) {
                AvatarRenderer(
                    configuration = avatarConfiguration,
                    contentDescription = stringResource(R.string.avatar_content_description, state.profileName),
                    modifier = Modifier.size(270.dp)
                )
            }
        }
        item {
            AvatarGroupTabs(selectedGroup) { selectedGroupName = it.name }
        }
        item {
            when (selectedGroup) {
                AvatarEditorGroup.BODY -> AvatarGroupPanel(
                    title = R.string.body_type,
                    choices = catalogChoices(AvatarSlot.BODY, state.ownedBundleIds),
                    selected = state.selectedBodyId,
                    colorTitle = R.string.avatar_skin_color,
                    colors = skinColors,
                    selectedColor = state.avatarSkinColor,
                    onSelect = { onSelectItem(AvatarSelection.BODY, it) },
                    onSelectColor = { onSelectColor(AvatarColorSelection.SKIN, it) }
                )
                AvatarEditorGroup.FACE -> AvatarGroupPanel(
                    R.string.avatar_face_shape,
                    catalogChoices(AvatarSlot.FACE, state.ownedBundleIds),
                    state.selectedFaceId,
                    onSelect = { onSelectItem(AvatarSelection.FACE, it) }
                )
                AvatarEditorGroup.EYES -> AvatarGroupPanel(
                    R.string.avatar_expression, catalogChoices(AvatarSlot.EYES, state.ownedBundleIds), state.selectedEyesId,
                    R.string.avatar_eye_color, eyeColors, state.avatarEyeColor,
                    { onSelectItem(AvatarSelection.EYES, it) },
                    { onSelectColor(AvatarColorSelection.EYES, it) }
                )
                AvatarEditorGroup.HAIR -> AvatarGroupPanel(
                    R.string.hair, catalogChoices(AvatarSlot.HAIR, state.ownedBundleIds), state.selectedHairId,
                    R.string.avatar_hair_color, hairColors, state.avatarHairColor,
                    { onSelectItem(AvatarSelection.HAIR, it) },
                    { onSelectColor(AvatarColorSelection.HAIR, it) }
                )
                AvatarEditorGroup.BEARD -> AvatarGroupPanel(
                    R.string.avatar_beard, catalogChoices(AvatarSlot.BEARD, state.ownedBundleIds), state.selectedBeardId,
                    R.string.avatar_beard_color, hairColors, state.avatarBeardColor,
                    { onSelectItem(AvatarSelection.BEARD, it) },
                    { onSelectColor(AvatarColorSelection.BEARD, it) }
                )
                AvatarEditorGroup.GLASSES -> AvatarGroupPanel(
                    R.string.avatar_glasses, catalogChoices(AvatarSlot.GLASSES, state.ownedBundleIds), state.selectedGlassesId,
                    R.string.avatar_glasses_color, accessoryColors, state.avatarGlassesColor,
                    { onSelectItem(AvatarSelection.GLASSES, it) },
                    { onSelectColor(AvatarColorSelection.GLASSES, it) }
                )
                AvatarEditorGroup.SHIRT -> AvatarGroupPanel(
                    R.string.clothes, catalogChoices(AvatarSlot.SHIRT, state.ownedBundleIds), state.selectedShirtId,
                    R.string.avatar_shirt_color, clothesColors, state.avatarShirtColor,
                    { onSelectItem(AvatarSelection.SHIRT, it) },
                    { onSelectColor(AvatarColorSelection.SHIRT, it) }
                )
                AvatarEditorGroup.HAT -> AvatarGroupPanel(
                    R.string.avatar_hat, catalogChoices(AvatarSlot.HAT, state.ownedBundleIds), state.selectedHatId,
                    R.string.avatar_hat_color, clothesColors, state.avatarHatColor,
                    { onSelectItem(AvatarSelection.HAT, it) },
                    { onSelectColor(AvatarColorSelection.HAT, it) }
                )
            }
        }
    }
}

private enum class AvatarEditorGroup(@param:StringRes val label: Int) {
    BODY(R.string.avatar_group_body),
    FACE(R.string.avatar_group_face),
    EYES(R.string.avatar_group_expression),
    HAIR(R.string.avatar_group_hair),
    BEARD(R.string.avatar_group_beard),
    GLASSES(R.string.avatar_group_glasses),
    SHIRT(R.string.avatar_group_clothes),
    HAT(R.string.avatar_group_hat)
}

@Composable
private fun AvatarGroupTabs(selected: AvatarEditorGroup, onSelect: (AvatarEditorGroup) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = stringResource(R.string.avatar_customization_groups),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
        ) {
            items(AvatarEditorGroup.entries, key = { it.name }) { group ->
                NeomorphicChoice(
                    selected = group == selected,
                    onClick = { onSelect(group) }
                ) {
                    Text(
                        text = stringResource(group.label),
                        fontWeight = if (group == selected) FontWeight.Black else FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun AvatarGroupPanel(
    @StringRes title: Int,
    choices: List<Pair<String, Int>>,
    selected: String,
    @StringRes colorTitle: Int? = null,
    colors: List<String> = emptyList(),
    selectedColor: String = "",
    onSelect: (String) -> Unit,
    onSelectColor: (String) -> Unit = {}
) {
    NeomorphicCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(stringResource(title), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
            ) {
                items(choices, key = { it.first }) { (id, label) ->
                    NeomorphicChoice(
                        selected = id == selected,
                        onClick = { onSelect(id) },
                        modifier = Modifier.width(156.dp).height(72.dp)
                    ) {
                        Text(
                            text = stringResource(label),
                            fontWeight = if (id == selected) FontWeight.Black else FontWeight.Medium,
                            textAlign = TextAlign.Center,
                            maxLines = 2,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
            if (colorTitle != null && colors.isNotEmpty()) {
                Text(stringResource(colorTitle), fontWeight = FontWeight.Bold)
                AvatarColorRow(colors, selectedColor, onSelectColor)
            }
        }
    }
}

@Composable
private fun AvatarColorRow(colors: List<String>, selected: String, onSelect: (String) -> Unit) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(4.dp)
    ) {
        items(colors, key = { it }) { hex ->
            val isSelected = selected.equals(hex, ignoreCase = true)
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(Color(android.graphics.Color.parseColor(hex)), CircleShape)
                    .border(
                        width = if (isSelected) 4.dp else 1.dp,
                        color = if (isSelected) NeoButton else NeoElement,
                        shape = CircleShape
                    )
                    .clickable { onSelect(hex) },
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) Text("\u2713", color = Color.White, fontWeight = FontWeight.Black)
            }
        }
    }
}

@Composable
private fun ChoiceSection(
    @StringRes title: Int,
    choices: List<Pair<String, Int>>,
    selected: String,
    optional: Boolean = false,
    onSelect: (String) -> Unit
) {
    NeomorphicCard(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(stringResource(title), fontWeight = FontWeight.Bold)
            val visibleChoices = if (optional) listOf("" to R.string.avatar_none) + choices else choices
            visibleChoices.forEach { (id, label) ->
                NeomorphicChoice(
                    selected = id == selected,
                    onClick = { onSelect(id) },
                    modifier = Modifier.fillMaxWidth()
                ) { Text(stringResource(label), fontWeight = if (id == selected) FontWeight.Bold else FontWeight.Medium) }
            }
        }
    }
}

@Composable
private fun ColorChoiceSection(
    @StringRes title: Int,
    colors: List<String>,
    selected: String,
    onSelect: (String) -> Unit
) {
    NeomorphicCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(stringResource(title), fontWeight = FontWeight.Bold)
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(4.dp)
            ) {
                items(colors, key = { it }) { hex ->
                    val isSelected = selected.equals(hex, ignoreCase = true)
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(Color(android.graphics.Color.parseColor(hex)), CircleShape)
                            .border(
                                width = if (isSelected) 4.dp else 1.dp,
                                color = if (isSelected) NeoButton else NeoElement,
                                shape = CircleShape
                            )
                            .clickable { onSelect(hex) },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Text("✓", color = Color.White, fontWeight = FontWeight.Black)
                        }
                    }
                }
            }
        }
    }
}

private fun bodyChoices() = catalogChoices(AvatarSlot.BODY, emptySet())

private fun catalogChoices(
    slot: AvatarSlot,
    ownedBundleIds: Set<String>
): List<Pair<String, Int>> = AvatarCatalog.itemsFor(slot)
    .filter { StoreCatalog.isItemUnlocked(it.id, ownedBundleIds) }
    .map { it.id to it.labelRes }

private val skinColors = listOf(
    "#4A2A29", "#674036", "#87513A", "#A96347", "#B7744F",
    "#C3845B", "#DA9569", "#E5A27D", "#ECB494", "#F2C4A4",
    "#F2CFB7", "#F7D8BC", "#F6D2C8", "#F4DFD5", "#F9E9DF"
)

private val hairColors = listOf(
    "#29232A", "#403036", "#5B3A31", "#744536", "#9C5E40", "#AE7548",
    "#CB9C5C", "#E4BD75", "#9E3E48", "#C95C59", "#7B6A9C", "#D5CED5"
)

private val eyeColors = listOf(
    "#382E34", "#6B4531", "#8B6B39", "#447B44", "#488C9A", "#4075B9", "#92959C"
)

private val clothesColors = listOf(
    "#B37AC8", "#4FA6D4", "#82AE56", "#F4C944", "#ED9848", "#BC416B",
    "#EAC5D8", "#F4F4EE", "#333D4B", "#BF7D51", "#55C0AB", "#7566B2"
)

private val accessoryColors = listOf(
    "#2C3443", "#3362A3", "#698D45", "#F18D23", "#E95151", "#E38AC4"
)

@Composable
fun MoreScreen(contentPadding: PaddingValues, onNavigate: (MvpPage) -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp, contentPadding.calculateTopPadding() + 24.dp, 20.dp, contentPadding.calculateBottomPadding() + 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item { ScreenHeading(R.string.more_title, R.string.more_description) }
        item { MoreCard(R.string.profile, R.string.profile_description) { onNavigate(MvpPage.PROFILE) } }
        item { MoreCard(R.string.history_title, R.string.history_description) { onNavigate(MvpPage.HISTORY) } }
        item { MoreCard(R.string.settings, R.string.settings_description) { onNavigate(MvpPage.SETTINGS) } }
    }
}

@Composable
private fun MoreCard(@StringRes title: Int, @StringRes description: Int, onClick: () -> Unit) {
    NeomorphicCard(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)
    ) {
        Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
            Text(stringResource(title), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(stringResource(description), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text("›", style = MaterialTheme.typography.headlineMedium, color = NeoButton)
        }
    }
}

@Composable
fun ProfileScreen(
    state: MvpState,
    avatarConfiguration: AvatarConfiguration,
    contentPadding: PaddingValues,
    onBack: () -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(
            20.dp,
            contentPadding.calculateTopPadding() + 12.dp,
            20.dp,
            contentPadding.calculateBottomPadding() + 20.dp
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item { BackHeader(R.string.profile, onBack) }
        item {
            Box(
                modifier = Modifier.size(260.dp).neomorphicInset(RoundedCornerShape(28.dp)),
                contentAlignment = Alignment.Center
            ) {
                AvatarRenderer(avatarConfiguration, Modifier.size(240.dp), stringResource(R.string.avatar_content_description, state.profileName))
            }
        }
        item { Text(state.profileName, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black) }
        item { StatsRow(state) }
        item { ProfileMetric(R.string.total_completions, state.totalCompletions.toString()) }
        item { ProfileMetric(R.string.longest_streak, stringResource(R.string.streak_days, state.longestStreak)) }
        item { ProfileMetric(R.string.primary_focus, categoryLabel(state.primaryCategory)) }
        item {
            Text(
                stringResource(R.string.category_history_title),
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black
            )
        }
        GoalCategory.entries.forEach { category ->
            item {
                val history = state.historyFor(category)
                CategoryHistoryCard(
                    category = categoryLabel(category),
                    summary = stringResource(
                        R.string.category_history_summary,
                        history.totalCompletions,
                        history.currentStreak,
                        history.longestStreak
                    )
                )
            }
        }
        item {
            Text(
                stringResource(
                    R.string.achievements_title,
                    state.unlockedAchievements.size,
                    AchievementId.entries.size
                ),
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black
            )
        }
        if (state.unlockedAchievements.isEmpty()) {
            item {
                Text(
                    stringResource(R.string.achievements_empty),
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            state.unlockedAchievements.sortedBy(AchievementId::ordinal).forEach { achievement ->
                item {
                    CategoryHistoryCard(
                        category = achievementTitle(achievement),
                        summary = stringResource(R.string.achievement_reward)
                    )
                }
            }
        }
        item { ProfileMetric(R.string.profile_id, state.profileName.lowercase().replace(' ', '-')) }
    }
}

@Composable
private fun CategoryHistoryCard(category: String, summary: String) {
    NeomorphicCard(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.fillMaxWidth().padding(16.dp)) {
            Text(category, fontWeight = FontWeight.Bold)
            Text(
                summary,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun achievementTitle(achievement: AchievementId): String = stringResource(
    when (achievement) {
        AchievementId.FIRST_COMPLETION -> R.string.achievement_first_completion
        AchievementId.CATEGORY_SPECIALIST -> R.string.achievement_category_specialist
        AchievementId.PRODUCTIVE_DAY -> R.string.achievement_productive_day
        AchievementId.NIGHT_OWL -> R.string.achievement_night_owl
        AchievementId.BONUS_HUNTER -> R.string.achievement_bonus_hunter
        AchievementId.SEVEN_DAY_STREAK -> R.string.achievement_seven_day_streak
        AchievementId.CATEGORY_EXPLORER -> R.string.achievement_category_explorer
        AchievementId.COIN_COLLECTOR -> R.string.achievement_coin_collector
        AchievementId.XP_VETERAN -> R.string.achievement_xp_veteran
        AchievementId.STYLE_COLLECTOR -> R.string.achievement_style_collector
    }
)

@Composable
private fun ProfileMetric(@StringRes label: Int, value: String) {
    NeomorphicCard(modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth().padding(16.dp)) {
            Text(stringResource(label), color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.weight(1f))
            Text(value, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun StoreScreen(
    state: MvpState,
    avatarConfiguration: AvatarConfiguration,
    contentPadding: PaddingValues,
    onBack: () -> Unit,
    onPurchase: (String) -> Unit,
    onEquip: (String) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(
            20.dp,
            contentPadding.calculateTopPadding() + 12.dp,
            20.dp,
            contentPadding.calculateBottomPadding() + 20.dp
        ),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item { BackHeader(R.string.store_title, onBack) }
        item { Text(stringResource(R.string.store_notice), color = MaterialTheme.colorScheme.onSurfaceVariant) }
        item {
            NeomorphicCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        stringResource(R.string.store_balance),
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "◇ ${state.coins}",
                        color = NeoButton,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }
        items(StoreCatalog.bundles, key = StoreBundle::id) { bundle ->
            StoreBundleCard(
                bundle = bundle,
                previewConfiguration = bundle.applyTo(avatarConfiguration),
                coins = state.coins,
                owned = bundle.id in state.ownedBundleIds,
                onPurchase = { onPurchase(bundle.id) },
                onEquip = { onEquip(bundle.id) }
            )
        }
    }
}

@Composable
private fun StoreBundleCard(
    bundle: StoreBundle,
    previewConfiguration: AvatarConfiguration,
    coins: Int,
    owned: Boolean,
    onPurchase: () -> Unit,
    onEquip: () -> Unit
) {
    val missingCoins = (bundle.price - coins).coerceAtLeast(0)
    NeomorphicCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(138.dp).neomorphicInset(RoundedCornerShape(22.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    AvatarRenderer(
                        configuration = previewConfiguration,
                        contentDescription = stringResource(bundle.titleRes),
                        modifier = Modifier.size(132.dp)
                    )
                }
                Spacer(Modifier.width(16.dp))
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Text(
                        stringResource(bundle.titleRes),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        stringResource(bundle.descriptionRes),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        "◇ ${bundle.price}",
                        color = NeoButton,
                        fontWeight = FontWeight.Black
                    )
                }
            }
            Text(
                stringResource(bundle.contentsRes),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall
            )
            if (owned) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        stringResource(R.string.store_owned),
                        modifier = Modifier.weight(1f),
                        color = NeoButton,
                        fontWeight = FontWeight.Bold
                    )
                    NeomorphicButton(onClick = onEquip) {
                        Text(stringResource(R.string.store_use), fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                NeomorphicButton(
                    onClick = onPurchase,
                    enabled = coins >= bundle.price,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.store_buy, bundle.price), fontWeight = FontWeight.Bold)
                }
                if (missingCoins > 0) {
                    Text(
                        stringResource(R.string.store_missing_coins, missingCoins),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }
    }
}

@Composable
fun SettingsScreen(
    state: MvpState,
    contentPadding: PaddingValues,
    onBack: () -> Unit,
    onSoundChange: (Boolean) -> Unit,
    onVibrationChange: (Boolean) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        BackHeader(R.string.settings, onBack)
        Text(stringResource(R.string.preferences), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        NeomorphicCard(modifier = Modifier.fillMaxWidth()) {
            Column {
                SettingRow(stringResource(R.string.sound), state.soundEnabled, onSoundChange)
                Box(Modifier.fillMaxWidth().height(2.dp).padding(horizontal = 16.dp).background(NeoElement))
                SettingRow(stringResource(R.string.vibration), state.vibrationEnabled, onVibrationChange)
            }
        }
        Text(stringResource(R.string.notifications_notice), color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(stringResource(R.string.app_version), style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
private fun SettingRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(label, modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
        NeomorphicSwitch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun BackHeader(@StringRes title: Int, onBack: () -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        NeomorphicButton(
            onClick = onBack,
            backgroundColor = NeoBackground,
            contentColor = NeoButton,
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp)
        ) { Text(stringResource(R.string.back), fontWeight = FontWeight.Bold) }
        Spacer(Modifier.width(8.dp))
        Text(stringResource(title), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
    }
}

@Composable
private fun ScreenHeading(@StringRes title: Int, @StringRes description: Int) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(stringResource(title), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
        Text(stringResource(description), color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun CategorySelector(selected: GoalCategory, onSelect: (GoalCategory) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        GoalCategory.entries.forEach { category ->
            NeomorphicChoice(
                selected = category == selected,
                onClick = { onSelect(category) },
                modifier = Modifier.fillMaxWidth()
            ) { Text(categoryLabel(category), fontWeight = if (category == selected) FontWeight.Bold else FontWeight.Medium) }
        }
    }
}

@Composable
private fun categoryLabel(category: GoalCategory): String = stringResource(
    when (category) {
        GoalCategory.FOOD_AND_HYDRATION -> R.string.category_food
        GoalCategory.EXERCISE -> R.string.category_exercise
        GoalCategory.SIMPLE_TASK -> R.string.category_simple_task
        GoalCategory.STUDY -> R.string.category_study
    }
)
