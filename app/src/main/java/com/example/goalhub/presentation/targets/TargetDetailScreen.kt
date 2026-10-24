package com.example.goalhub.presentation.targets

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
import androidx.compose.material.icons.rounded.Checklist
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.StickyNote2
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
import com.example.goalhub.domain.model.Target
import com.example.goalhub.ui.components.AddInlineField
import com.example.goalhub.ui.components.AnimatedProgressBar
import com.example.goalhub.ui.components.CategoryChip
import com.example.goalhub.ui.components.ConfirmDialog
import com.example.goalhub.ui.components.DateRangeLabel
import com.example.goalhub.ui.components.EmptyState
import com.example.goalhub.ui.components.NoteRow
import com.example.goalhub.ui.components.ParentLinkRow
import com.example.goalhub.ui.components.PriorityBadge
import com.example.goalhub.ui.components.SectionHeader
import com.example.goalhub.ui.components.SoftCardPadded
import com.example.goalhub.ui.components.StatusChip
import com.example.goalhub.ui.components.SubtaskRow
import com.example.goalhub.ui.components.TaskRow
import com.example.goalhub.ui.theme.Gradients
import com.example.goalhub.ui.theme.Ink
import com.example.goalhub.ui.theme.MutedInk

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TargetDetailScreen(
    targetId: Long,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onOpenGoal: (Long) -> Unit,
    onOpenProject: (Long) -> Unit,
    onCreateTask: () -> Unit,
    viewModel: TargetDetailViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        key = "target_$targetId",
        factory = com.example.goalhub.presentation.AppViewModelProvider.targetDetail(targetId),
    ),
) {
    val target by viewModel.target.collectAsStateWithLifecycle()
    val parentGoal by viewModel.parentGoal.collectAsStateWithLifecycle()
    val parentProject by viewModel.parentProject.collectAsStateWithLifecycle()
    val linkedTasks by viewModel.linkedTasks.collectAsStateWithLifecycle()
    var showDeleteDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Target") },
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
        if (target == null) {
            Box(Modifier.fillMaxSize().padding(padding))
            return@Scaffold
        }
        TargetDetailContent(
            target = target!!,
            parentGoalTitle = parentGoal?.title,
            parentProjectTitle = parentProject?.title,
            linkedTasks = linkedTasks,
            onOpenGoal = onOpenGoal,
            onOpenProject = onOpenProject,
            onToggleSubtask = viewModel::toggleSubtask,
            onAddSubtask = viewModel::addSubtask,
            onDeleteSubtask = viewModel::deleteSubtask,
            onAddNote = viewModel::addNote,
            onDeleteNote = viewModel::deleteNote,
            onToggleTask = viewModel::toggleTask,
            onProgressChange = viewModel::setProgress,
            onResetAuto = viewModel::resetProgressToAuto,
            onCreateTask = onCreateTask,
            modifier = Modifier.padding(padding),
        )
    }

    if (showDeleteDialog) {
        ConfirmDialog(
            title = "Delete target?",
            message = "\"${target?.title}\" and its subtasks and notes will be removed.",
            onConfirm = {
                viewModel.deleteTarget()
                onDelete()
            },
            onDismiss = { showDeleteDialog = false },
        )
    }
}

@Composable
private fun TargetDetailContent(
    target: Target,
    parentGoalTitle: String?,
    parentProjectTitle: String?,
    linkedTasks: List<com.example.goalhub.domain.model.Task>,
    onOpenGoal: (Long) -> Unit,
    onOpenProject: (Long) -> Unit,
    onToggleSubtask: (Long) -> Unit,
    onAddSubtask: (String) -> Unit,
    onDeleteSubtask: (Long) -> Unit,
    onAddNote: (String) -> Unit,
    onDeleteNote: (Long) -> Unit,
    onToggleTask: (Long) -> Unit,
    onProgressChange: (Int) -> Unit,
    onResetAuto: () -> Unit,
    onCreateTask: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = Gradients.forGoal(target.gradientIndex, target.status == Status.COMPLETED)
    val doneSubtasks = target.subtasks.count { it.completed }

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
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        StatusChip(target.status, onLightSurface = false)
                        CategoryChip(target.category.label, Color.White, onLightSurface = false)
                        PriorityBadge(target.priority)
                    }
                    Text(
                        text = target.title,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                    )
                    if (target.description.isNotBlank()) {
                        Text(
                            text = target.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.9f),
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    DateRangeLabel(
                        start = target.start,
                        end = target.end,
                        tint = Color.White.copy(alpha = 0.85f),
                        showRelative = true,
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        AnimatedProgressBar(
                            progress = target.progress,
                            color = Color.White,
                            trackColor = Color.White.copy(alpha = 0.3f),
                            height = 12.dp,
                        )
                        Text(
                            text = "${target.progress}%",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                        )
                    }
                }
            }
        }

        // Parent links
        if (parentGoalTitle != null) {
            item {
                ParentLinkRow(label = "Part of goal", title = parentGoalTitle, onClick = { onOpenGoal(target.goalId!!) })
            }
        }
        if (parentProjectTitle != null) {
            item {
                ParentLinkRow(label = "Part of project", title = parentProjectTitle, onClick = { onOpenProject(target.projectId!!) })
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
                                text = if (target.progressMode.label == "Auto") "Auto" else "Manual",
                                style = MaterialTheme.typography.labelMedium,
                                color = MutedInk,
                            )
                            if (target.progressMode.label != "Auto") {
                                Spacer(Modifier.width(6.dp))
                                TextButton(onClick = onResetAuto) {
                                    Text("Reset to auto")
                                }
                            }
                        }
                    }
                    Slider(
                        value = target.progress.toFloat(),
                        onValueChange = { onProgressChange(it.toInt()) },
                        valueRange = 0f..100f,
                    )
                    Text(
                        text = if (target.subtasks.isNotEmpty()) {
                            "Auto: $doneSubtasks of ${target.subtasks.size} subtasks done."
                        } else {
                            "Add subtasks and progress will be computed automatically."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MutedInk,
                    )
                }
            }
        }

        // Subtasks
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SectionHeader(
                    title = if (target.subtasks.isEmpty()) "Subtasks" else "Subtasks ($doneSubtasks/${target.subtasks.size})",
                )
                AddInlineField(hint = "Add a subtask", onAdd = onAddSubtask)
            }
        }
        if (target.subtasks.isEmpty()) {
            item {
                EmptyState(
                    icon = androidx.compose.material.icons.Icons.Rounded.Checklist,
                    title = "No subtasks yet",
                    subtitle = "Subtasks are the concrete steps inside this target.",
                )
            }
        } else {
            items(target.subtasks.size, key = { target.subtasks[it].id }) { index ->
                val subtask = target.subtasks[index]
                SubtaskRow(
                    subtask = subtask,
                    onToggle = { onToggleSubtask(subtask.id) },
                    onDelete = { onDeleteSubtask(subtask.id) },
                )
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
        if (linkedTasks.isEmpty()) {
            item {
                Text(
                    text = "No tasks attached to this target yet.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MutedInk,
                    modifier = Modifier.padding(horizontal = 4.dp),
                )
            }
        } else {
            items(linkedTasks.size, key = { linkedTasks[it].id }) { index ->
                val task = linkedTasks[index]
                TaskRow(task = task, onToggle = { onToggleTask(task.id) }, onClick = {})
            }
        }

        // Notes
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SectionHeader(title = "Notes")
                AddInlineField(hint = "Jot something down…", onAdd = onAddNote)
            }
        }
        if (target.notes.isEmpty()) {
            item {
                Text(
                    text = "No notes yet.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MutedInk,
                    modifier = Modifier.padding(horizontal = 4.dp),
                )
            }
        } else {
            items(target.notes.size, key = { target.notes[it].id }) { index ->
                val note = target.notes[index]
                NoteRow(note = note, onDelete = { onDeleteNote(note.id) })
            }
        }
    }
}
