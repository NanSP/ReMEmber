package com.example.remember.feature.mvp.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.remember.R
import com.example.remember.feature.goals.domain.model.GoalCategory
import com.example.remember.feature.mvp.domain.CompletionHistoryItem
import com.example.remember.feature.mvp.domain.HistoryPeriod
import com.example.remember.feature.mvp.domain.MvpState
import com.example.remember.feature.mvp.domain.filterCompletionHistory
import com.example.remember.ui.components.NeomorphicButton
import com.example.remember.ui.components.NeomorphicCard
import com.example.remember.ui.components.NeomorphicChoice
import com.example.remember.ui.components.neomorphicInset
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun HistoryScreen(
    state: MvpState,
    contentPadding: PaddingValues,
    onBack: () -> Unit
) {
    var period by rememberSaveable { mutableStateOf(HistoryPeriod.LAST_30_DAYS) }
    var category by rememberSaveable { mutableStateOf<GoalCategory?>(null) }
    var goalId by rememberSaveable { mutableStateOf<Long?>(null) }
    val today = LocalDate.now(ZoneId.systemDefault()).toEpochDay()
    val availableGoals = state.completionHistory
        .filter { category == null || it.category == category }
        .distinctBy(CompletionHistoryItem::goalId)
        .sortedBy { it.goalTitle.lowercase() }
    val filtered = filterCompletionHistory(
        items = state.completionHistory,
        period = period,
        category = category,
        goalId = goalId,
        currentEpochDay = today
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(
            start = 20.dp,
            top = contentPadding.calculateTopPadding() + 12.dp,
            end = 20.dp,
            bottom = contentPadding.calculateBottomPadding() + 24.dp
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { HistoryHeader(onBack) }
        item {
            Text(
                stringResource(R.string.history_description),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        item {
            HistoryFilterSection(
                title = stringResource(R.string.history_period),
                choices = HistoryPeriod.entries,
                label = { choice ->
                    stringResource(
                        when (choice) {
                            HistoryPeriod.LAST_7_DAYS -> R.string.history_period_7_days
                            HistoryPeriod.LAST_30_DAYS -> R.string.history_period_30_days
                            HistoryPeriod.ALL -> R.string.history_period_all
                        }
                    )
                },
                selected = period,
                onSelect = { period = it }
            )
        }
        item {
            HistoryFilterSection(
                title = stringResource(R.string.category),
                choices = listOf<GoalCategory?>(null) + GoalCategory.entries,
                label = { choice ->
                    choice?.let { historyCategoryLabel(it) }
                        ?: stringResource(R.string.history_all_categories)
                },
                selected = category,
                onSelect = {
                    category = it
                    goalId = null
                }
            )
        }
        if (availableGoals.isNotEmpty()) {
            item {
                HistoryFilterSection(
                    title = stringResource(R.string.history_goal),
                    choices = listOf<Long?>(null) + availableGoals.map { it.goalId },
                    label = { selectedId ->
                        availableGoals.firstOrNull { it.goalId == selectedId }?.goalTitle
                            ?: stringResource(R.string.history_all_goals)
                    },
                    selected = goalId,
                    onSelect = { goalId = it }
                )
            }
        }
        item { HistorySummary(filtered) }
        if (filtered.isEmpty()) {
            item {
                NeomorphicCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        stringResource(R.string.history_empty),
                        modifier = Modifier.padding(20.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            items(filtered, key = CompletionHistoryItem::id) { completion ->
                HistoryCompletionCard(completion)
            }
        }
    }
}

@Composable
private fun HistoryHeader(onBack: () -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        NeomorphicButton(
            onClick = onBack,
            backgroundColor = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.primary,
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Text(stringResource(R.string.back), fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.width(10.dp))
        Text(
            stringResource(R.string.history_title),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Black,
            modifier = Modifier.semantics { heading() }
        )
    }
}

@Composable
private fun <T> HistoryFilterSection(
    title: String,
    choices: List<T>,
    selected: T,
    onSelect: (T) -> Unit,
    label: @Composable (T) -> String = { it.toString() }
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(title, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        LazyRow(
            modifier = Modifier.selectableGroup(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(vertical = 4.dp)
        ) {
            items(choices) { choice ->
                NeomorphicChoice(
                    selected = choice == selected,
                    onClick = { onSelect(choice) }
                ) {
                    Text(label(choice), fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun HistorySummary(items: List<CompletionHistoryItem>) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        HistoryMetric(
            value = items.size.toString(),
            label = stringResource(R.string.history_filtered_completions),
            modifier = Modifier.weight(1f)
        )
        HistoryMetric(
            value = items.sumOf { it.experienceEarned }.toString(),
            label = "XP",
            modifier = Modifier.weight(1f)
        )
        HistoryMetric(
            value = items.sumOf { it.coinsEarned }.toString(),
            label = stringResource(R.string.coins),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun HistoryMetric(value: String, label: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.neomorphicInset(RoundedCornerShape(18.dp)).padding(12.dp)
    ) {
        Column {
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
private fun HistoryCompletionCard(completion: CompletionHistoryItem) {
    val formatter = DateTimeFormatter.ofPattern(
        "dd/MM/yyyy 'às' HH:mm",
        Locale.forLanguageTag("pt-BR")
    )
    val completedAt = Instant.ofEpochMilli(completion.completedAtMillis)
        .atZone(ZoneId.systemDefault())
        .format(formatter)
    NeomorphicCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                completion.goalTitle,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black
            )
            Text(
                stringResource(
                    R.string.history_completed_at,
                    historyCategoryLabel(completion.category),
                    completedAt
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                pluralStringResource(
                    R.plurals.history_reward,
                    completion.coinsEarned,
                    completion.experienceEarned,
                    completion.coinsEarned
                ),
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun historyCategoryLabel(category: GoalCategory): String = stringResource(
    when (category) {
        GoalCategory.FOOD_AND_HYDRATION -> R.string.category_food
        GoalCategory.EXERCISE -> R.string.category_exercise
        GoalCategory.SIMPLE_TASK -> R.string.category_simple_task
        GoalCategory.STUDY -> R.string.category_study
    }
)
