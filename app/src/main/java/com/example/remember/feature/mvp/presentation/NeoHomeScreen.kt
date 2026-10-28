package com.example.remember.feature.mvp.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.example.remember.R
import com.example.remember.core.assets.AssetLocation
import com.example.remember.feature.avatar.domain.model.AvatarConfiguration
import com.example.remember.feature.avatar.presentation.AvatarRenderer
import com.example.remember.feature.goals.domain.model.GoalCategory
import com.example.remember.feature.mvp.domain.MvpGoal
import com.example.remember.feature.mvp.domain.MvpState
import com.example.remember.ui.components.NeomorphicCard
import com.example.remember.ui.components.NeomorphicIconButton
import com.example.remember.ui.components.NeomorphicProgressBar
import com.example.remember.ui.theme.NeoBackground
import com.example.remember.ui.theme.NeoButton
import com.example.remember.ui.theme.NeoElement
import com.example.remember.ui.theme.NeoText

@Composable
fun NeoTodayScreen(
    state: MvpState,
    avatarConfiguration: AvatarConfiguration,
    contentPadding: PaddingValues,
    onCreateGoal: () -> Unit,
    onSelectPrimaryCategory: (GoalCategory) -> Unit,
    onCompleteGoal: (Long) -> Unit
) {
    var focusMenuExpanded by rememberSaveable { mutableStateOf(false) }
    var notificationsExpanded by rememberSaveable { mutableStateOf(false) }
    val pendingGoals = state.goals.filterNot { it.isCompleted }
    val footerInset = contentPadding.calculateBottomPadding()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NeoBackground)
            .statusBarsPadding()
            .padding(
                start = 16.dp,
                top = contentPadding.calculateTopPadding() + 12.dp,
                end = 16.dp,
                bottom = 0.dp
            ),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        HomeHeader(
            state = state,
            focusMenuExpanded = focusMenuExpanded,
            onOpenFocusMenu = { focusMenuExpanded = true },
            onDismissFocusMenu = { focusMenuExpanded = false },
            onSelectPrimaryCategory = onSelectPrimaryCategory
        )

        NeoXpBar(state)

        NeomorphicCard(
            modifier = Modifier.fillMaxWidth().weight(1f),
            shape = RoundedCornerShape(28.dp),
            backgroundColor = NeoBackground
        ) {
            Box(Modifier.fillMaxSize().clip(RoundedCornerShape(28.dp))) {
                SceneBackdrop(state.primaryCategory)
                AvatarRenderer(
                    configuration = avatarConfiguration,
                    contentDescription = stringResource(
                        R.string.avatar_content_description,
                        state.profileName
                    ),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth(0.72f)
                        .fillMaxHeight(0.92f)
                )

                NotificationsMenu(
                    pendingGoals = pendingGoals,
                    expanded = notificationsExpanded,
                    onOpen = { notificationsExpanded = true },
                    onDismiss = { notificationsExpanded = false },
                    modifier = Modifier.align(Alignment.TopStart).padding(14.dp)
                )
            }
        }

        GoalsPanel(
            modifier = Modifier.fillMaxWidth().height(226.dp + footerInset),
            goals = state.goals,
            onCreateGoal = onCreateGoal,
            onCompleteGoal = onCompleteGoal,
            footerInset = footerInset
        )
    }
}

@Composable
private fun NotificationsMenu(
    pendingGoals: List<MvpGoal>,
    expanded: Boolean,
    onOpen: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val countDescription = stringResource(
        R.string.home_notifications_count,
        pendingGoals.size
    )
    Box(modifier) {
        NeomorphicIconButton(
            onClick = onOpen,
            contentDescription = countDescription,
            size = 52.dp,
            shape = CircleShape,
            backgroundColor = NeoButton,
            iconColor = Color.White
        ) {
            Text("!", fontSize = 25.sp, fontWeight = FontWeight.Black)
            if (pendingGoals.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(4.dp, (-4).dp)
                        .size(21.dp)
                        .background(NeoElement, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        pendingGoals.size.coerceAtMost(99).toString(),
                        color = NeoButton,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = onDismiss,
            modifier = Modifier.width(286.dp).background(NeoBackground)
        ) {
            Text(
                stringResource(R.string.home_notifications_title),
                color = NeoText,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
            )
            if (pendingGoals.isEmpty()) {
                Text(
                    stringResource(R.string.home_notifications_empty),
                    color = NeoText,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                )
            } else {
                pendingGoals.forEach { goal ->
                    DropdownMenuItem(
                        text = {
                            Column {
                                Text(
                                    goal.title,
                                    color = NeoText,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    neoCategoryLabel(goal.category),
                                    color = NeoText,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        },
                        onClick = onDismiss
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeHeader(
    state: MvpState,
    focusMenuExpanded: Boolean,
    onOpenFocusMenu: () -> Unit,
    onDismissFocusMenu: () -> Unit,
    onSelectPrimaryCategory: (GoalCategory) -> Unit
) {
    NeomorphicCard(
        modifier = Modifier.fillMaxWidth().height(76.dp),
        shape = RoundedCornerShape(22.dp),
        backgroundColor = NeoBackground
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(Modifier.weight(1.2f)) {
                HeaderMetric(
                    icon = {
                        CategoryHeaderIcon(
                            category = state.primaryCategory,
                            modifier = Modifier.size(30.dp)
                        )
                    },
                    value = null,
                    label = stringResource(R.string.home_focus_label),
                    description = stringResource(R.string.home_focus_description),
                    onClick = onOpenFocusMenu
                )
                DropdownMenu(
                    expanded = focusMenuExpanded,
                    onDismissRequest = onDismissFocusMenu,
                    modifier = Modifier.background(NeoBackground)
                ) {
                    GoalCategory.entries.forEach { category ->
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    CategoryHeaderIcon(
                                        category = category,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(Modifier.width(12.dp))
                                    Text(neoCategoryLabel(category), color = NeoText)
                                }
                            },
                            onClick = {
                                onSelectPrimaryCategory(category)
                                onDismissFocusMenu()
                            }
                        )
                    }
                }
            }
            HeaderMetric(
                modifier = Modifier.weight(1f),
                icon = {
                    Text(
                        "\u2726",
                        color = NeoButton,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Black
                    )
                },
                value = state.currentStreak.toString(),
                label = "",
                description = stringResource(R.string.home_streak_description),
                glow = true,
                emphasizeValue = true
            )
            HeaderMetric(
                modifier = Modifier.weight(1f),
                icon = {
                    Text(
                        "\u25C8",
                        color = NeoButton,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Black
                    )
                },
                value = state.coins.toString(),
                label = stringResource(R.string.coins),
                description = stringResource(R.string.home_coins_description)
            )
        }
    }
}

@Composable
private fun HeaderMetric(
    icon: @Composable () -> Unit,
    value: String?,
    label: String,
    description: String,
    modifier: Modifier = Modifier,
    glow: Boolean = false,
    emphasizeValue: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    val glowModifier = if (glow) {
        Modifier.dropShadow(
            shape = CircleShape,
            shadow = Shadow(
                radius = 13.dp,
                spread = 3.dp,
                offset = DpOffset(0.dp, 0.dp),
                color = Color(0xFF3D8BFF).copy(alpha = 0.82f)
            )
        )
    } else {
        Modifier
    }
    val interactionModifier = if (onClick != null) {
        Modifier.clickable(role = Role.Button, onClick = onClick)
    } else {
        Modifier
    }

    Row(
        modifier = modifier
            .fillMaxHeight()
            .then(interactionModifier)
            .semantics { contentDescription = description }
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .then(glowModifier),
            contentAlignment = Alignment.Center
        ) {
            icon()
        }
        Spacer(Modifier.width(7.dp))
        Column(Modifier.weight(1f)) {
            if (value != null) {
                Text(
                    value,
                    color = if (emphasizeValue) NeoButton else NeoText,
                    style = if (emphasizeValue) {
                        MaterialTheme.typography.headlineMedium
                    } else {
                        MaterialTheme.typography.titleSmall
                    },
                    fontWeight = FontWeight.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (label.isNotBlank()) {
                Text(
                    label,
                    color = NeoText,
                    style = if (value == null) {
                        MaterialTheme.typography.titleSmall
                    } else {
                        MaterialTheme.typography.labelSmall
                    },
                    fontWeight = if (value == null) FontWeight.Bold else FontWeight.Normal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun CategoryHeaderIcon(category: GoalCategory, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val line = 2.2.dp.toPx()
        val iconColor = NeoButton
        when (category) {
            GoalCategory.EXERCISE -> {
                drawLine(
                    color = iconColor,
                    start = androidx.compose.ui.geometry.Offset(size.width * 0.18f, size.height * 0.5f),
                    end = androidx.compose.ui.geometry.Offset(size.width * 0.82f, size.height * 0.5f),
                    strokeWidth = line,
                    cap = StrokeCap.Round
                )
                val weightWidth = size.width * 0.14f
                val weightHeight = size.height * 0.48f
                listOf(0.08f, 0.22f, 0.64f, 0.78f).forEach { x ->
                    drawRoundRect(
                        color = iconColor,
                        topLeft = androidx.compose.ui.geometry.Offset(
                            size.width * x,
                            (size.height - weightHeight) / 2f
                        ),
                        size = androidx.compose.ui.geometry.Size(weightWidth, weightHeight),
                        cornerRadius = CornerRadius(line, line)
                    )
                }
            }

            GoalCategory.FOOD_AND_HYDRATION -> {
                val forkX = size.width * 0.3f
                drawLine(
                    iconColor,
                    androidx.compose.ui.geometry.Offset(forkX, size.height * 0.18f),
                    androidx.compose.ui.geometry.Offset(forkX, size.height * 0.84f),
                    line,
                    StrokeCap.Round
                )
                listOf(0.2f, 0.3f, 0.4f).forEach { x ->
                    drawLine(
                        iconColor,
                        androidx.compose.ui.geometry.Offset(size.width * x, size.height * 0.18f),
                        androidx.compose.ui.geometry.Offset(size.width * x, size.height * 0.4f),
                        line,
                        StrokeCap.Round
                    )
                }
                drawLine(
                    iconColor,
                    androidx.compose.ui.geometry.Offset(size.width * 0.2f, size.height * 0.4f),
                    androidx.compose.ui.geometry.Offset(size.width * 0.4f, size.height * 0.4f),
                    line,
                    StrokeCap.Round
                )
                val knife = Path().apply {
                    moveTo(size.width * 0.64f, size.height * 0.18f)
                    lineTo(size.width * 0.8f, size.height * 0.18f)
                    lineTo(size.width * 0.75f, size.height * 0.5f)
                    lineTo(size.width * 0.66f, size.height * 0.5f)
                    close()
                }
                drawPath(knife, iconColor)
                drawLine(
                    iconColor,
                    androidx.compose.ui.geometry.Offset(size.width * 0.7f, size.height * 0.48f),
                    androidx.compose.ui.geometry.Offset(size.width * 0.7f, size.height * 0.84f),
                    line,
                    StrokeCap.Round
                )
            }

            GoalCategory.STUDY -> {
                val book = Path().apply {
                    moveTo(size.width * 0.5f, size.height * 0.78f)
                    lineTo(size.width * 0.12f, size.height * 0.66f)
                    lineTo(size.width * 0.12f, size.height * 0.22f)
                    quadraticTo(
                        size.width * 0.32f,
                        size.height * 0.2f,
                        size.width * 0.5f,
                        size.height * 0.34f
                    )
                    quadraticTo(
                        size.width * 0.68f,
                        size.height * 0.2f,
                        size.width * 0.88f,
                        size.height * 0.22f
                    )
                    lineTo(size.width * 0.88f, size.height * 0.66f)
                    close()
                }
                drawPath(book, iconColor, style = Stroke(width = line, cap = StrokeCap.Round))
                drawLine(
                    iconColor,
                    androidx.compose.ui.geometry.Offset(size.width * 0.5f, size.height * 0.34f),
                    androidx.compose.ui.geometry.Offset(size.width * 0.5f, size.height * 0.78f),
                    line,
                    StrokeCap.Round
                )
            }

            GoalCategory.SIMPLE_TASK -> {
                drawRoundRect(
                    color = iconColor,
                    topLeft = androidx.compose.ui.geometry.Offset(size.width * 0.14f, size.height * 0.14f),
                    size = androidx.compose.ui.geometry.Size(size.width * 0.72f, size.height * 0.72f),
                    cornerRadius = CornerRadius(line * 1.5f, line * 1.5f),
                    style = Stroke(width = line)
                )
                drawLine(
                    iconColor,
                    androidx.compose.ui.geometry.Offset(size.width * 0.28f, size.height * 0.52f),
                    androidx.compose.ui.geometry.Offset(size.width * 0.43f, size.height * 0.68f),
                    line,
                    StrokeCap.Round
                )
                drawLine(
                    iconColor,
                    androidx.compose.ui.geometry.Offset(size.width * 0.43f, size.height * 0.68f),
                    androidx.compose.ui.geometry.Offset(size.width * 0.72f, size.height * 0.34f),
                    line,
                    StrokeCap.Round
                )
            }
        }
    }
}

@Composable
private fun NeoXpBar(state: MvpState) {
    var helpExpanded by rememberSaveable { mutableStateOf(false) }
    val xpDescription = stringResource(
        R.string.home_xp_description,
        state.experience % MvpState.EXPERIENCE_PER_LEVEL
    )
    val helpDescription = stringResource(R.string.home_help_open_description)
    NeomorphicCard(
        modifier = Modifier.fillMaxWidth().height(48.dp),
        shape = RoundedCornerShape(16.dp),
        backgroundColor = NeoBackground
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                stringResource(R.string.home_level_short, state.level),
                color = NeoButton,
                fontWeight = FontWeight.Black
            )
            Spacer(Modifier.width(12.dp))
            NeomorphicProgressBar(
                progress = state.levelProgress,
                modifier = Modifier.weight(1f),
                progressColor = NeoButton,
                trackColor = NeoBackground
            )
            Spacer(Modifier.width(10.dp))
            Text(
                text = "${state.experience % MvpState.EXPERIENCE_PER_LEVEL}/100",
                color = NeoText,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.semantics { contentDescription = xpDescription }
            )
            Spacer(Modifier.width(8.dp))
            Box {
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .background(NeoElement, CircleShape)
                        .clickable(
                            role = Role.Button,
                            onClick = { helpExpanded = true }
                        )
                        .semantics {
                            contentDescription = helpDescription
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "?",
                        color = NeoButton,
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp
                    )
                }
                HomeHelpMenu(
                    expanded = helpExpanded,
                    onDismiss = { helpExpanded = false }
                )
            }
        }
    }
}

@Composable
private fun HomeHelpMenu(expanded: Boolean, onDismiss: () -> Unit) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
        modifier = Modifier.width(304.dp).background(NeoBackground)
    ) {
        Text(
            stringResource(R.string.home_help_title),
            color = NeoText,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Black,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
        )
        HomeHelpItem(R.string.home_help_focus_title, R.string.home_help_focus_description)
        HomeHelpItem(R.string.home_help_streak_title, R.string.home_help_streak_description)
        HomeHelpItem(R.string.home_help_coins_title, R.string.home_help_coins_description)
        HomeHelpItem(R.string.home_help_xp_title, R.string.home_help_xp_description)
        HomeHelpItem(
            R.string.home_help_notifications_title,
            R.string.home_help_notifications_description
        )
        HomeHelpItem(R.string.home_help_scene_title, R.string.home_help_scene_description)
        HomeHelpItem(R.string.home_help_goals_title, R.string.home_help_goals_description)
        HomeHelpItem(R.string.home_help_footer_title, R.string.home_help_footer_description)
    }
}

@Composable
private fun HomeHelpItem(title: Int, description: Int) {
    Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 7.dp)) {
        Text(
            stringResource(title),
            color = NeoButton,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            stringResource(description),
            color = NeoText,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
private fun SceneBackdrop(category: GoalCategory) {
    val assetPath = when (category) {
        GoalCategory.STUDY -> "cenarios/biblioteca/biblioteca_estante_cheia.svg"
        GoalCategory.FOOD_AND_HYDRATION -> "cenarios/horta/horta_misto.svg"
        GoalCategory.EXERCISE -> "cenarios/academia/academia_esteira.svg"
        GoalCategory.SIMPLE_TASK -> "cenarios/quarto/quarto_quarto_organizado.svg"
    }
    Box(Modifier.fillMaxSize()) {
        Box(
            Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(72.dp)
                .background(NeoElement.copy(alpha = 0.35f))
        )
        AsyncImage(
            model = AssetLocation.uri(assetPath),
            contentDescription = stringResource(R.string.home_scene_description),
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .fillMaxWidth(0.62f)
                .fillMaxHeight()
                .alpha(0.56f)
        )
    }
}

@Composable
private fun GoalsPanel(
    modifier: Modifier = Modifier,
    goals: List<MvpGoal>,
    onCreateGoal: () -> Unit,
    onCompleteGoal: (Long) -> Unit,
    footerInset: Dp = 0.dp
) {
    val completed = goals.count { it.isCompleted }
    val addGoalDescription = stringResource(R.string.home_add_goal)
    val goalsListState = rememberLazyListState()
    NeomorphicCard(
        modifier = modifier,
        shape = RoundedCornerShape(
            topStart = 28.dp,
            topEnd = 28.dp,
            bottomStart = 0.dp,
            bottomEnd = 0.dp
        ),
        backgroundColor = NeoElement
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(start = 16.dp, top = 16.dp, end = 16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(
                        stringResource(R.string.today_goals),
                        color = NeoText,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        stringResource(R.string.home_goals_progress, completed, goals.size),
                        color = NeoText,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
                Text(
                    text = "+",
                    color = Color.White,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(NeoButton)
                        .clickable(role = Role.Button, onClick = onCreateGoal)
                        .semantics { contentDescription = addGoalDescription }
                        .padding(horizontal = 14.dp, vertical = 5.dp)
                )
            }
            Spacer(Modifier.height(10.dp))
            if (goals.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        stringResource(R.string.home_empty_goals),
                        color = NeoText,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            } else {
                Box(Modifier.weight(1f)) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        state = goalsListState,
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        contentPadding = PaddingValues(bottom = footerInset + 8.dp)
                    ) {
                        items(goals.sortedBy { it.isCompleted }, key = { it.id }) { goal ->
                            CompactGoal(goal = goal, onComplete = { onCompleteGoal(goal.id) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CompactGoal(goal: MvpGoal, onComplete: () -> Unit) {
    val completeGoalDescription = stringResource(R.string.home_complete_goal, goal.title)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(NeoBackground)
            .clickable(enabled = !goal.isCompleted, role = Role.Button, onClick = onComplete)
            .semantics { contentDescription = completeGoalDescription }
            .padding(horizontal = 12.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(categorySymbol(goal.category), color = NeoButton, fontSize = 18.sp)
        Spacer(Modifier.width(10.dp))
        Text(
            goal.title,
            color = NeoText,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        Text(
            if (goal.isCompleted) "\u2713" else "\u25CB",
            color = NeoButton,
            fontSize = 20.sp
        )
    }
}

@Composable
fun NeoBottomBar(page: MvpPage, onNavigate: (MvpPage) -> Unit) {
    val destinations = listOf(
        Triple(MvpPage.TODAY, R.string.home_nav_home, "\u2302"),
        Triple(MvpPage.AVATAR, R.string.home_nav_avatar, "\u2659"),
        Triple(MvpPage.STORE, R.string.home_nav_store, "\u25C7"),
        Triple(MvpPage.MORE, R.string.home_nav_more, "\u2630")
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(NeoBackground)
            .height(96.dp)
            .navigationBarsPadding()
            .padding(start = 18.dp, top = 8.dp, end = 18.dp, bottom = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        destinations.forEach { (destination, label, glyph) ->
            val selected = page == destination ||
                (page == MvpPage.GOALS && destination == MvpPage.TODAY)
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                NeomorphicIconButton(
                    onClick = { onNavigate(destination) },
                    contentDescription = stringResource(label),
                    active = selected,
                    size = 48.dp,
                    shape = RoundedCornerShape(14.dp),
                ) {
                    Text(text = glyph, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                }
                Text(
                    stringResource(label),
                    color = NeoText,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}

private fun categorySymbol(category: GoalCategory): String = when (category) {
    GoalCategory.STUDY -> "\u25A4"
    GoalCategory.FOOD_AND_HYDRATION -> "\u25C9"
    GoalCategory.EXERCISE -> "\u219F"
    GoalCategory.SIMPLE_TASK -> "\u2713"
}

@Composable
private fun neoCategoryLabel(category: GoalCategory): String = stringResource(
    when (category) {
        GoalCategory.STUDY -> R.string.category_study
        GoalCategory.FOOD_AND_HYDRATION -> R.string.category_food
        GoalCategory.EXERCISE -> R.string.category_exercise
        GoalCategory.SIMPLE_TASK -> R.string.category_simple_task
    }
)
