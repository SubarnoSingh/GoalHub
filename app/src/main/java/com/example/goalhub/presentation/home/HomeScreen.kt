package com.example.goalhub.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Flag
import androidx.compose.material.icons.rounded.TaskAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.goalhub.ui.components.EmptyState
import com.example.goalhub.ui.components.GoalHeroCard
import com.example.goalhub.ui.components.SectionHeader
import com.example.goalhub.ui.components.SoftCardPadded
import com.example.goalhub.ui.components.TargetCard
import com.example.goalhub.ui.components.TaskRow
import com.example.goalhub.ui.theme.Gradients
import com.example.goalhub.ui.theme.Ink
import com.example.goalhub.ui.theme.MutedInk
import com.example.goalhub.ui.theme.Primary
import com.example.goalhub.util.formatFull
import java.time.LocalDate

private fun greeting(): String {
    val hour = java.time.LocalTime.now().hour
    return when (hour) {
        in 5..11 -> "Good morning"
        in 12..16 -> "Good afternoon"
        in 17..21 -> "Good evening"
        else -> "Good night"
    }
}

@Composable
fun HomeScreen(
    onOpenGoal: (Long) -> Unit,
    onOpenTarget: (Long) -> Unit,
    onOpenCalendar: () -> Unit,
    onSeeAllGoals: () -> Unit,
    onSeeAllTasks: () -> Unit,
    viewModel: HomeViewModel = androidx.lifecycle.viewmodel.compose.viewModel(factory = com.example.goalhub.presentation.AppViewModelProvider.Factory),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val today = LocalDate.now()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 100.dp),
    ) {
        item {
            HomeHeader(
                userName = uiState.userName,
                onOpenCalendar = onOpenCalendar,
            )
        }

        // ----- Long-term goals -----
        item {
            SectionHeader(
                title = "Your Goals",
                modifier = Modifier.padding(horizontal = 20.dp),
                actionLabel = "See all",
                onAction = onSeeAllGoals,
            )
        }
        if (uiState.dashboard.activeGoals.isEmpty()) {
            item {
                EmptyState(
                    icon = androidx.compose.material.icons.Icons.Rounded.Flag,
                    title = "No active goals",
                    subtitle = "Long-term goals are the reason you do the small stuff.",
                )
            }
        } else {
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    items(uiState.dashboard.activeGoals, key = { it.id }) { goal ->
                        GoalHeroCard(goal = goal, onClick = { onOpenGoal(goal.id) })
                    }
                }
            }
        }

        item { Spacer(Modifier.height(26.dp)) }

        // ----- Active targets -----
        item {
            SectionHeader(
                title = "Active Targets",
                modifier = Modifier.padding(horizontal = 20.dp),
            )
        }
        if (uiState.dashboard.activeTargets.isEmpty()) {
            item {
                EmptyState(
                    icon = androidx.compose.material.icons.Icons.Rounded.CheckCircle,
                    title = "Nothing in flight",
                    subtitle = "Break a goal down into short targets with a start and an end date.",
                )
            }
        } else {
            items(uiState.dashboard.activeTargets, key = { it.id }) { target ->
                Column(Modifier.padding(horizontal = 20.dp)) {
                    TargetCard(target = target, onClick = { onOpenTarget(target.id) })
                }
            }
        }

        item { Spacer(Modifier.height(26.dp)) }

        // ----- Today's tasks -----
        item {
            SectionHeader(
                title = "Today's Tasks",
                modifier = Modifier.padding(horizontal = 20.dp),
                actionLabel = "See all",
                onAction = onSeeAllTasks,
            )
        }
        if (uiState.dashboard.todayTasks.isEmpty()) {
            item {
                EmptyState(
                    icon = androidx.compose.material.icons.Icons.Rounded.TaskAlt,
                    title = "All clear for today",
                    subtitle = "Nothing due. Perfect time to make progress on a target.",
                )
            }
        } else {
            item {
                Column(
                    modifier = Modifier.padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    uiState.dashboard.todayTasks.forEach { task ->
                        TaskRow(
                            task = task,
                            onToggle = { viewModel.toggleTask(task.id) },
                            onClick = {},
                        )
                    }
                }
            }
        }

        // ----- Upcoming deadlines -----
        if (uiState.dashboard.weekDeadlines.isNotEmpty()) {
            item { Spacer(Modifier.height(26.dp)) }
            item {
                SectionHeader(
                    title = "Upcoming Deadlines",
                    modifier = Modifier.padding(horizontal = 20.dp),
                )
            }
            items(uiState.dashboard.weekDeadlines, key = { "dl_${it.id}" }) { deadline ->
                Column(Modifier.padding(horizontal = 20.dp)) {
                    TargetCard(target = deadline, onClick = { onOpenTarget(deadline.id) })
                }
            }
        }
    }
}

@Composable
private fun HomeHeader(
    userName: String,
    onOpenCalendar: () -> Unit,
) {
    val today = LocalDate.now()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 12.dp, top = 18.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = greeting(),
                style = MaterialTheme.typography.bodyLarge,
                color = MutedInk,
            )
            Text(
                text = userName,
                style = MaterialTheme.typography.displaySmall,
                color = Ink,
            )
            Text(
                text = today.formatFull(),
                style = MaterialTheme.typography.labelLarge,
                color = MutedInk,
            )
        }
        IconButton(onClick = onOpenCalendar) {
            Icon(
                imageVector = androidx.compose.material.icons.Icons.Rounded.CalendarMonth,
                contentDescription = "Calendar",
                tint = Primary,
            )
        }
        Box(
            modifier = Modifier
                .padding(end = 8.dp)
                .size(44.dp)
                .clip(CircleShape)
                .background(Brush.linearGradient(Gradients.Personal)),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = userName.take(1).uppercase(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = androidx.compose.ui.graphics.Color.White,
            )
        }
    }
}
