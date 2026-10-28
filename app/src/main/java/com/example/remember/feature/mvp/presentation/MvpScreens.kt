package com.example.remember.feature.mvp.presentation

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.example.remember.R
import com.example.remember.feature.avatar.data.AvatarCatalog
import com.example.remember.feature.avatar.domain.model.AvatarConfiguration
import com.example.remember.feature.avatar.domain.model.AvatarSlot
import com.example.remember.feature.avatar.presentation.AvatarRenderer
import com.example.remember.feature.goals.domain.model.GoalCategory
import com.example.remember.feature.mvp.data.MvpRepository.AvatarSelection
import com.example.remember.feature.mvp.domain.MvpGoal
import com.example.remember.feature.mvp.domain.MvpState
import com.example.remember.ui.components.neomorphicTextFieldColors
import com.example.remember.ui.theme.NeoButton

enum class MvpPage(val isRoot: Boolean = false) {
    TODAY(true), GOALS(true), AVATAR(true), MORE(true), PROFILE, STORE(true), SETTINGS
}

@Composable
fun LoadingScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator()
            Spacer(Modifier.height(16.dp))
            Text(stringResource(R.string.loading))
        }
    }
}

@Composable
fun OnboardingScreen(
    state: MvpState,
    onContinue: (String, GoalCategory, String) -> Unit
) {
    var name by rememberSaveable { mutableStateOf(state.profileName) }
    var category by rememberSaveable { mutableStateOf(state.primaryCategory) }
    var bodyItemId by rememberSaveable {
        mutableStateOf(state.selectedBodyId.ifBlank { "body_medium" })
    }
    var showError by rememberSaveable { mutableStateOf(false) }
    val previewConfiguration = AvatarConfiguration(
        AvatarCatalog.defaultConfiguration.equippedItemIds + (AvatarSlot.BODY to bodyItemId)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 40.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text(
            text = stringResource(R.string.onboarding_eyebrow),
            color = MaterialTheme.colorScheme.secondary,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = stringResource(R.string.onboarding_title),
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Black
        )
        Text(
            text = stringResource(R.string.onboarding_description),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(8.dp))
        Text(stringResource(R.string.your_name), fontWeight = FontWeight.Bold)
        OutlinedTextField(
            value = name,
            onValueChange = { name = it; showError = false },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            label = { Text(stringResource(R.string.name_placeholder)) },
            isError = showError,
            colors = neomorphicTextFieldColors(),
            supportingText = {
                if (showError) Text(stringResource(R.string.name_required))
            }
        )
        Text(stringResource(R.string.body_type), fontWeight = FontWeight.Bold)
        AvatarRenderer(
            configuration = previewConfiguration,
            contentDescription = stringResource(R.string.avatar_preview),
            modifier = Modifier.fillMaxWidth().height(220.dp)
        )
        ChoiceSection(
            title = R.string.body_type,
            choices = bodyChoices(),
            selected = bodyItemId,
            onSelect = { bodyItemId = it }
        )
        Text(stringResource(R.string.primary_focus), fontWeight = FontWeight.Bold)
        CategorySelector(selected = category, onSelect = { category = it })
        Button(
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
    onAddGoal: (String, GoalCategory) -> Unit,
    onCompleteGoal: (Long) -> Unit,
    onDeleteGoal: (Long) -> Unit
) {
    var title by rememberSaveable { mutableStateOf("") }
    var category by rememberSaveable { mutableStateOf(state.primaryCategory) }
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
            Card {
                Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(stringResource(R.string.new_goal), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(stringResource(R.string.goal_title)) },
                        placeholder = { Text(stringResource(R.string.goal_placeholder)) },
                        singleLine = true,
                        colors = neomorphicTextFieldColors()
                    )
                    Text(stringResource(R.string.category), fontWeight = FontWeight.Bold)
                    CategorySelector(category, onSelect = { category = it })
                    Button(
                        onClick = { onAddGoal(title, category); title = "" },
                        enabled = title.isNotBlank(),
                        modifier = Modifier.fillMaxWidth()
                    ) { Text(stringResource(R.string.add_goal)) }
                }
            }
        }
        item { Text(stringResource(R.string.active_goals), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) }
        if (state.goals.isEmpty()) {
            item { Text(stringResource(R.string.empty_goals), color = MaterialTheme.colorScheme.onSurfaceVariant) }
        } else {
            items(state.goals, key = { it.id }) { goal ->
                GoalCard(goal, onComplete = { onCompleteGoal(goal.id) }, onDelete = { onDeleteGoal(goal.id) })
            }
        }
    }
}

@Composable
private fun GoalCard(goal: MvpGoal, onComplete: () -> Unit, onDelete: (() -> Unit)? = null) {
    Card(colors = CardDefaults.cardColors(containerColor = if (goal.isCompleted) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface)) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                goal.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textDecoration = if (goal.isCompleted) TextDecoration.LineThrough else null
            )
            Text(categoryLabel(goal.category), color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelLarge)
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (goal.isCompleted) {
                    Text(stringResource(R.string.completed), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                } else {
                    Button(onClick = onComplete) { Text(stringResource(R.string.complete)) }
                }
                Spacer(Modifier.weight(1f))
                if (onDelete != null) TextButton(onClick = onDelete) { Text(stringResource(R.string.delete)) }
            }
        }
    }
}

@Composable
fun AvatarScreen(
    state: MvpState,
    avatarConfiguration: AvatarConfiguration,
    contentPadding: PaddingValues,
    onSelectItem: (AvatarSelection, String) -> Unit,
    onToggleGlasses: (Boolean) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp, contentPadding.calculateTopPadding() + 24.dp, 20.dp, contentPadding.calculateBottomPadding() + 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item { ScreenHeading(R.string.avatar_title, R.string.avatar_description) }
        item {
            AvatarRenderer(
                configuration = avatarConfiguration,
                contentDescription = stringResource(R.string.avatar_content_description, state.profileName),
                modifier = Modifier.size(300.dp)
            )
        }
        item { ChoiceSection(R.string.body_type, bodyChoices(), state.selectedBodyId) { onSelectItem(AvatarSelection.BODY, it) } }
        item { ChoiceSection(R.string.hair, hairChoices(), state.selectedHairId) { onSelectItem(AvatarSelection.HAIR, it) } }
        item { ChoiceSection(R.string.clothes, topChoices(), state.selectedTopId) { onSelectItem(AvatarSelection.TOP, it) } }
        item {
            SettingRow(stringResource(R.string.round_glasses), state.roundGlassesEnabled, onToggleGlasses)
        }
    }
}

@Composable
private fun ChoiceSection(@StringRes title: Int, choices: List<Pair<String, Int>>, selected: String, onSelect: (String) -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(stringResource(title), fontWeight = FontWeight.Bold)
            choices.forEach { (id, label) ->
                FilterChip(selected = id == selected, onClick = { onSelect(id) }, label = { Text(stringResource(label)) }, modifier = Modifier.fillMaxWidth())
            }
        }
    }
}

private fun bodyChoices() = listOf("body_slim" to R.string.body_slim, "body_medium" to R.string.body_medium, "body_wide" to R.string.body_wide)
private fun hairChoices() = listOf("hair_curly" to R.string.hair_curly, "hair_bangs" to R.string.hair_bangs, "hair_long" to R.string.hair_long)
private fun topChoices() = listOf("top_tshirt" to R.string.top_tshirt, "top_hoodie" to R.string.top_hoodie, "top_jacket" to R.string.top_jacket)

@Composable
fun MoreScreen(contentPadding: PaddingValues, onNavigate: (MvpPage) -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp, contentPadding.calculateTopPadding() + 24.dp, 20.dp, contentPadding.calculateBottomPadding() + 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item { ScreenHeading(R.string.more_title, R.string.more_description) }
        item { MoreCard(R.string.profile, R.string.profile_description) { onNavigate(MvpPage.PROFILE) } }
        item { MoreCard(R.string.settings, R.string.settings_description) { onNavigate(MvpPage.SETTINGS) } }
    }
}

@Composable
private fun MoreCard(@StringRes title: Int, @StringRes description: Int, onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), onClick = onClick) {
        Column(Modifier.padding(20.dp)) {
            Text(stringResource(title), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(stringResource(description), color = MaterialTheme.colorScheme.onSurfaceVariant)
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
        item { AvatarRenderer(avatarConfiguration, Modifier.size(240.dp), stringResource(R.string.avatar_content_description, state.profileName)) }
        item { Text(state.profileName, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black) }
        item { StatsRow(state) }
        item { ProfileMetric(R.string.total_completions, state.totalCompletions.toString()) }
        item { ProfileMetric(R.string.longest_streak, stringResource(R.string.streak_days, state.longestStreak)) }
        item { ProfileMetric(R.string.primary_focus, categoryLabel(state.primaryCategory)) }
        item { ProfileMetric(R.string.profile_id, state.profileName.lowercase().replace(' ', '-')) }
    }
}

@Composable
private fun ProfileMetric(@StringRes label: Int, value: String) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth().padding(16.dp)) {
            Text(stringResource(label), color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.weight(1f))
            Text(value, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun StoreScreen(contentPadding: PaddingValues, onBack: () -> Unit) {
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
        item { StoreItem(R.string.store_item_hat, "120") }
        item { StoreItem(R.string.store_item_scarf, "180") }
        item { StoreItem(R.string.store_item_multiplier, "250") }
    }
}

@Composable
private fun StoreItem(@StringRes name: Int, price: String) {
    Card {
        Row(Modifier.fillMaxWidth().padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(stringResource(name), fontWeight = FontWeight.Bold)
                Text(
                    "$price ${stringResource(R.string.coins).lowercase()}",
                    color = MaterialTheme.colorScheme.primary
                )
            }
            OutlinedButton(
                onClick = {},
                enabled = false,
                colors = ButtonDefaults.outlinedButtonColors(disabledContentColor = NeoButton),
                border = BorderStroke(1.dp, NeoButton)
            ) { Text(stringResource(R.string.coming_soon)) }
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
        Card {
            Column {
                SettingRow(stringResource(R.string.sound), state.soundEnabled, onSoundChange)
                HorizontalDivider()
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
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun BackHeader(@StringRes title: Int, onBack: () -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        TextButton(onClick = onBack) { Text(stringResource(R.string.back)) }
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
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        GoalCategory.entries.forEach { category ->
            FilterChip(
                selected = category == selected,
                onClick = { onSelect(category) },
                label = { Text(categoryLabel(category)) },
                modifier = Modifier.fillMaxWidth()
            )
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
