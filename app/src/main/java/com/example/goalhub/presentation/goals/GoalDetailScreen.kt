package com.example.goalhub.presentation.goals

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.goalhub.domain.model.Goal
import com.example.goalhub.domain.model.Status
import com.example.goalhub.ui.components.AddInlineField
import com.example.goalhub.ui.components.AnimatedProgressBar
import com.example.goalhub.ui.components.CategoryChip
import com.example.goalhub.ui.components.ConfirmDialog
import com.example.goalhub.ui.components.DateRangeLabel
import com.example.goalhub.ui.components.EmptyState
import com.example.goalhub.ui.components.FormLabel
import com.example.goalhub.ui.components.MilestoneRow
import com.example.goalhub.ui.components.ParentLinkRow
import com.example.goalhub.ui.components.PriorityBadge
import com.example.goalhub.ui.components.SectionHeader
import com.example.goalhub.ui.components.SoftCardPadded
import com.example.goalhub.ui.components.StatusChip
import com.example.goalhub.ui.components.TaskRow
import com.example.goalhub.ui.theme.Gradients
import com.example.goalhub.ui.theme.Ink
import com.example.goalhub.ui.theme.MutedInk
import com.example.goalhub.ui.theme.Primary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalDetailScreen(
    goalId: Long,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onOpenTarget: (Long) -> Unit,
    onOpenProject: (Long) -> Unit,
    onCreateTarget: () -> Unit,
    onCreateProject: () -> Unit,
    onCreateTask: () -> Unit,
    viewModel: GoalDetailViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        key = "goal_$goalId",
        factory = com.example.goalhub.presentation.AppViewModelProvider.goalDetail(goalId),
    ),
) {
    val goal by viewModel.goal.collectAsStateWithLifecycle()
    var showDeleteDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Goal") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Rounded.Edit, contentDescription = "Edit")
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Rounded.DeleteOutline, contentDescription = "Delete")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
    ) { padding ->
        if (goal == null) {
            Box(Modifier.fillMaxSize().padding(padding))
            return@Scaffold
        }
        GoalDetailContent(
            goal = goal!!,
            onToggleMilestone = viewModel::toggleMilestone,
            onAddMilestone = viewModel::addMilestone,
            onDeleteMilestone = viewModel::deleteMilestone,
            onToggleTask = viewModel::toggleTask,
            onProgressChange = viewModel::setProgress,
            onResetAuto = viewModel::resetProgressToAuto,
            onOpenTarget = onOpenTarget,
            onOpenProject = onOpenProject,
            onCreateTarget = onCreateTarget,
            onCreateProject = onCreateProject,
            onCreateTask = onCreateTask,
            modifier = Modifier.padding(padding),
        )
    }

    if (showDeleteDialog) {
        ConfirmDialog(
            title = "Delete goal?",
            message = "\"${goal?.title}\" and everything linked to it (projects, targets, tasks, milestones) will be removed.",
            onConfirm = {
                viewModel.deleteGoal()
                onDelete()
            },
            onDismiss = { showDeleteDialog = false },
        )
    }
}

@Composable
private fun GoalDetailContent(
    goal: Goal,
    onToggleMilestone: (Long) -> Unit,
    onAddMilestone: (String) -> Unit,
    onDeleteMilestone: (Long) -> Unit,
    onToggleTask: (Long) -> Unit,
    onProgressChange: (Int) -> Unit,
    onResetAuto: () -> Unit,
    onOpenTarget: (Long) -> Unit,
    onOpenProject: (Long) -> Unit,
    onCreateTarget: () -> Unit,
    onCreateProject: () -> Unit,
    onCreateTask: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = Gradients.forGoal(goal.gradientIndex, goal.status == Status.COMPLETED)
    val doneMilestones = goal.milestones.count { it.completed }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 40.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        // Hero card
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(28.dp))
                    .background(Brush.linearGradient(colors))
                    .padding(24.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        StatusChip(goal.status, onLightSurface = false)
                        CategoryChip(goal.category.label, Color.White, onLightSurface = false)
                        PriorityBadge(goal.priority)
                    }
                    Text(
                        text = goal.title,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                    )
                    if (goal.description.isNotBlank()) {
                        Text(
                            text = goal.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.9f),
                            maxLines = 4,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    DateRangeLabel(
                        start = goal.start,
                        end = goal.end,
                        tint = Color.White.copy(alpha = 0.85f),
                        showRelative = true,
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        AnimatedProgressBar(
                            progress = goal.progress,
                            color = Color.White,
                            trackColor = Color.White.copy(alpha = 0.3f),
                            height = 12.dp,
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(
                                text = "${goal.progress}%",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                            )
                            if (goal.milestones.isNotEmpty()) {
                                Text(
                                    text = "$doneMilestones/${goal.milestones.size} milestones",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = Color.White.copy(alpha = 0.85f),
                                )
                            }
                        }
                    }
                }
            }
        }

        // Progress controls
        item {
            SoftCardPadded(modifier = Modifier.fillMaxWidth(), cornerRadius = 22.dp) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "Progress",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = Ink,
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (goal.progressMode.label == "Auto") "Auto" else "Manual",
                                style = MaterialTheme.typography.labelMedium,
                                color = MutedInk,
                            )
                            if (goal.progressMode.label != "Auto") {
                                Spacer(Modifier.width(6.dp))
                                TextButton(onClick = onResetAuto) {
                                    Text("Reset to auto")
                                }
                            }
                        }
                    }
                    Slider(
                        value = goal.progress.toFloat(),
                        onValueChange = { onProgressChange(it.toInt()) },
                        valueRange = 0f..100f,
                    )
                    Text(
                        text = if (goal.milestones.isNotEmpty()) {
                            "Automatically computed from completed milestones."
                        } else {
                            "Set the percentage by hand — or add milestones and subtasks to compute it automatically."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MutedInk,
                    )
                }
            }
        }

        // Milestones
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SectionHeader(
                    title = if (goal.milestones.isEmpty()) "Milestones" else "Milestones ($doneMilestones/${goal.milestones.size})",
                )
                AddInlineField(hint = "Add a milestone, e.g. \u201cRoom integration\u201d", onAdd = onAddMilestone)
            }
        }
        if (goal.milestones.isEmpty()) {
            item {
                EmptyState(
                    icon = androidx.compose.material.icons.Icons.Rounded.AutoAwesome,
                    title = "No milestones yet",
                    subtitle = "Milestones turn a big goal into checkpoints you can actually finish.",
                )
            }
        } else {
            items(goal.milestones.size, key = { goal.milestones[it].id }) { index ->
                val milestone = goal.milestones[index]
                MilestoneRow(
                    milestone = milestone,
                    onToggle = { onToggleMilestone(milestone.id) },
                    onDelete = { onDeleteMilestone(milestone.id) },
                )
            }
        }

        // Linked targets
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SectionHeader(title = "Short-term Targets")
                TextButton(onClick = onCreateTarget) {
                    Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Add")
                }
            }
        }
        if (goal.targets.isEmpty()) {
            item {
                Text(
                    text = "No targets linked to this goal yet.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MutedInk,
                    modifier = Modifier.padding(horizontal = 4.dp),
                )
            }
        } else {
            items(goal.targets.size, key = { goal.targets[it].id }) { index ->
                val target = goal.targets[index]
                SoftCardPadded(modifier = Modifier.fillMaxWidth(), cornerRadius = 18.dp, onClick = { onOpenTarget(target.id) }) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = target.title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = Ink,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            DateRangeLabel(start = target.start, end = target.end, showRelative = true)
                            Spacer(Modifier.width(10.dp))
                            Text(
                                text = "${target.progress}%",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = Gradients.forGoal(target.gradientIndex, false).first(),
                            )
                        }
                    }
                }
            }
        }

        // Linked projects
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SectionHeader(title = "Projects")
                TextButton(onClick = onCreateProject) {
                    Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Add")
                }
            }
        }
        if (goal.projects.isEmpty()) {
            item {
                Text(
                    text = "No projects linked to this goal yet.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MutedInk,
                    modifier = Modifier.padding(horizontal = 4.dp),
                )
            }
        } else {
            items(goal.projects.size, key = { goal.projects[it].id }) { index ->
                val project = goal.projects[index]
                SoftCardPadded(modifier = Modifier.fillMaxWidth(), cornerRadius = 18.dp, onClick = { onOpenProject(project.id) }) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = project.title,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = Ink,
                            )
                            DateRangeLabel(start = project.start, end = project.end)
                        }
                        Text(
                            text = "${project.progress}%",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = Gradients.forGoal(project.gradientIndex, false).first(),
                        )
                    }
                }
            }
        }

        // Linked tasks
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SectionHeader(title = "Tasks")
                TextButton(onClick = onCreateTask) {
                    Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Add")
                }
            }
        }
        if (goal.tasks.isEmpty()) {
            item {
                Text(
                    text = "No tasks linked to this goal yet.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MutedInk,
                    modifier = Modifier.padding(horizontal = 4.dp),
                )
            }
        } else {
            items(goal.tasks.size, key = { goal.tasks[it].id }) { index ->
                val task = goal.tasks[index]
                TaskRow(task = task, onToggle = { onToggleTask(task.id) }, onClick = {})
            }
        }
    }
}
