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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowBack
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
import com.example.goalhub.domain.model.Status
import com.example.goalhub.ui.components.AddInlineField
import com.example.goalhub.ui.components.AnimatedProgressBar
import com.example.goalhub.ui.components.CategoryChip
import com.example.goalhub.ui.components.ConfirmDialog
import com.example.goalhub.ui.components.DateRangeLabel
import com.example.goalhub.ui.components.EmptyState
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectDetailScreen(
    projectId: Long,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onOpenGoal: (Long) -> Unit,
    onOpenTarget: (Long) -> Unit,
    onCreateTarget: () -> Unit,
    onCreateTask: () -> Unit,
    viewModel: ProjectDetailViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        key = "project_$projectId",
        factory = com.example.goalhub.presentation.AppViewModelProvider.projectDetail(projectId),
    ),
) {
    val project by viewModel.project.collectAsStateWithLifecycle()
    val parentGoal by viewModel.parentGoal.collectAsStateWithLifecycle()
    var showDeleteDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Project") },
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
        if (project == null) {
            Box(Modifier.fillMaxSize().padding(padding))
            return@Scaffold
        }
        ProjectDetailContent(
            project = project!!,
            parentGoalTitle = parentGoal?.title,
            onOpenGoal = onOpenGoal,
            onToggleMilestone = viewModel::toggleMilestone,
            onAddMilestone = viewModel::addMilestone,
            onDeleteMilestone = viewModel::deleteMilestone,
            onToggleTask = viewModel::toggleTask,
            onProgressChange = viewModel::setProgress,
            onResetAuto = viewModel::resetProgressToAuto,
            onOpenTarget = onOpenTarget,
            onCreateTarget = onCreateTarget,
            onCreateTask = onCreateTask,
            modifier = Modifier.padding(padding),
        )
    }

    if (showDeleteDialog) {
        ConfirmDialog(
            title = "Delete project?",
            message = "\"${project?.title}\" and its linked tasks, targets and milestones will be removed.",
            onConfirm = {
                viewModel.deleteProject()
                onDelete()
            },
            onDismiss = { showDeleteDialog = false },
        )
    }
}

@Composable
private fun ProjectDetailContent(
    project: com.example.goalhub.domain.model.Project,
    parentGoalTitle: String?,
    onOpenGoal: (Long) -> Unit,
    onToggleMilestone: (Long) -> Unit,
    onAddMilestone: (String) -> Unit,
    onDeleteMilestone: (Long) -> Unit,
    onToggleTask: (Long) -> Unit,
    onProgressChange: (Int) -> Unit,
    onResetAuto: () -> Unit,
    onOpenTarget: (Long) -> Unit,
    onCreateTarget: () -> Unit,
    onCreateTask: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = Gradients.forGoal(project.gradientIndex, project.status == Status.COMPLETED)
    val doneMilestones = project.milestones.count { it.completed }
    val doneTasks = project.tasks.count { it.completed }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 40.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(28.dp))
                    .background(Brush.linearGradient(colors))
                    .padding(24.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        StatusChip(project.status, onLightSurface = false)
                        CategoryChip(project.category.label, Color.White, onLightSurface = false)
                        PriorityBadge(project.priority)
                    }
                    Text(
                        text = project.title,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                    )
                    if (project.description.isNotBlank()) {
                        Text(
                            text = project.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.9f),
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    DateRangeLabel(
                        start = project.start,
                        end = project.end,
                        tint = Color.White.copy(alpha = 0.85f),
                        showRelative = true,
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        AnimatedProgressBar(
                            progress = project.progress,
                            color = Color.White,
                            trackColor = Color.White.copy(alpha = 0.3f),
                            height = 12.dp,
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(
                                text = "${project.progress}%",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                            )
                            if (project.tasks.isNotEmpty()) {
                                Text(
                                    text = "$doneTasks/${project.tasks.size} tasks",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = Color.White.copy(alpha = 0.85f),
                                )
                            }
                        }
                    }
                }
            }
        }

        if (project.goalId != null) {
            item {
                ParentLinkRow(
                    label = "Part of goal",
                    title = parentGoalTitle ?: "Loading…",
                    onClick = { onOpenGoal(project.goalId) },
                )
            }
        }

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
                                text = if (project.progressMode.label == "Auto") "Auto" else "Manual",
                                style = MaterialTheme.typography.labelMedium,
                                color = MutedInk,
                            )
                            if (project.progressMode.label != "Auto") {
                                Spacer(Modifier.width(6.dp))
                                TextButton(onClick = onResetAuto) {
                                    Text("Reset to auto")
                                }
                            }
                        }
                    }
                    Slider(
                        value = project.progress.toFloat(),
                        onValueChange = { onProgressChange(it.toInt()) },
                        valueRange = 0f..100f,
                    )
                }
            }
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SectionHeader(
                    title = if (project.milestones.isEmpty()) "Milestones" else "Milestones ($doneMilestones/${project.milestones.size})",
                )
                AddInlineField(hint = "Add a milestone", onAdd = onAddMilestone)
            }
        }
        if (project.milestones.isEmpty()) {
            item {
                Text(
                    text = "No milestones yet.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MutedInk,
                    modifier = Modifier.padding(horizontal = 4.dp),
                )
            }
        } else {
            items(project.milestones.size, key = { project.milestones[it].id }) { index ->
                val milestone = project.milestones[index]
                MilestoneRow(
                    milestone = milestone,
                    onToggle = { onToggleMilestone(milestone.id) },
                    onDelete = { onDeleteMilestone(milestone.id) },
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SectionHeader(title = "Targets")
                TextButton(onClick = onCreateTarget) {
                    Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Add")
                }
            }
        }
        if (project.targets.isEmpty()) {
            item {
                Text(
                    text = "No targets in this project yet.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MutedInk,
                    modifier = Modifier.padding(horizontal = 4.dp),
                )
            }
        } else {
            items(project.targets.size, key = { project.targets[it].id }) { index ->
                val target = project.targets[index]
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
        if (project.tasks.isEmpty()) {
            item {
                Text(
                    text = "No tasks in this project yet.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MutedInk,
                    modifier = Modifier.padding(horizontal = 4.dp),
                )
            }
        } else {
            items(project.tasks.size, key = { project.tasks[it].id }) { index ->
                val task = project.tasks[index]
                TaskRow(task = task, onToggle = { onToggleTask(task.id) }, onClick = {})
            }
        }
    }
}
