package com.example.goalhub.presentation.create

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
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.goalhub.domain.model.Status
import com.example.goalhub.ui.components.AddInlineField
import com.example.goalhub.ui.components.CategoryPicker
import com.example.goalhub.ui.components.ChoiceChipRow
import com.example.goalhub.ui.components.CreateItemType
import com.example.goalhub.ui.components.DateField
import com.example.goalhub.ui.components.FormSection
import com.example.goalhub.ui.components.GradientPicker
import com.example.goalhub.ui.components.LabeledDropdown
import com.example.goalhub.ui.components.PrioritySelector
import com.example.goalhub.ui.components.SoftCardPadded
import com.example.goalhub.ui.theme.Ink
import com.example.goalhub.ui.theme.MutedInk
import com.example.goalhub.ui.theme.Primary
import com.example.goalhub.util.formatMedium
import com.example.goalhub.util.fromUtcMillis
import com.example.goalhub.util.toUtcMillis
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateItemScreen(
    type: CreateItemType,
    editId: Long?,
    goalLink: Long?,
    projectLink: Long?,
    targetLink: Long?,
    onBack: () -> Unit,
    onSaved: () -> Unit,
    viewModel: CreateItemViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        key = "create_${type.name}_$editId",
        factory = com.example.goalhub.presentation.AppViewModelProvider.create(type, editId, goalLink, projectLink, targetLink),
    ),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    var pickStart by remember { mutableStateOf(false) }
    var pickEnd by remember { mutableStateOf(false) }
    var pickDue by remember { mutableStateOf(false) }

    LaunchedEffect(state.saved) {
        if (state.saved) onSaved()
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (state.isEditing) "Edit ${state.type.label}" else "New ${state.type.label}",
                        fontWeight = FontWeight.SemiBold,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(onClick = viewModel::save, enabled = state.canSave && !state.loading) {
                        Text("Save", fontWeight = FontWeight.SemiBold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 48.dp),
            verticalArrangement = Arrangement.spacedBy(22.dp),
        ) {
            // Type selector (create mode only)
            if (!state.isEditing) {
                item {
                    ChoiceChipRow(
                        options = CreateItemType.entries.toList(),
                        selected = state.type,
                        onSelect = viewModel::setType,
                        label = { it.label },
                    )
                }
            }

            // Title + description
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = state.title,
                        onValueChange = viewModel::setTitle,
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {
                            Text(
                                text = when (state.type) {
                                    CreateItemType.GOAL -> "e.g. Learn Android Development"
                                    CreateItemType.PROJECT -> "e.g. Expense Tracker App"
                                    CreateItemType.TARGET -> "e.g. Finish Room Database"
                                    CreateItemType.TASK -> "e.g. Push project to GitHub"
                                },
                                style = MaterialTheme.typography.bodyLarge,
                            )
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        textStyle = MaterialTheme.typography.titleMedium,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                        ),
                    )
                    OutlinedTextField(
                        value = state.description,
                        onValueChange = viewModel::setDescription,
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Description (optional)", style = MaterialTheme.typography.bodyMedium) },
                        minLines = 2,
                        maxLines = 4,
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                        ),
                    )
                }
            }

            // Dates
            item {
                SoftCardPadded(modifier = Modifier.fillMaxWidth(), cornerRadius = 22.dp) {
                    FormSection(title = "When") {
                        when (state.type) {
                            CreateItemType.GOAL, CreateItemType.PROJECT, CreateItemType.TARGET -> {
                                DateField(
                                    label = "Start date",
                                    dateText = state.start.formatMedium(),
                                    onClick = { pickStart = true },
                                )
                                DateField(
                                    label = "End date / deadline",
                                    dateText = state.end.formatMedium(),
                                    onClick = { pickEnd = true },
                                )
                            }
                            CreateItemType.TASK -> {
                                DateField(
                                    label = "Due date",
                                    dateText = state.due?.formatMedium(),
                                    onClick = { if (state.hasDue) pickDue = true },
                                    trailing = {
                                        if (state.hasDue) {
                                            IconButton(onClick = viewModel::toggleHasDue, modifier = Modifier.size(28.dp)) {
                                                Icon(Icons.Rounded.Close, contentDescription = "Clear due date", tint = MutedInk, modifier = Modifier.size(18.dp))
                                            }
                                        } else {
                                            TextButton(onClick = viewModel::toggleHasDue) { Text("Set a due date") }
                                        }
                                    },
                                )
                                if (state.hasStart) {
                                    DateField(
                                        label = "Start date (optional)",
                                        dateText = state.start.formatMedium(),
                                        onClick = { pickStart = true },
                                        trailing = {
                                            IconButton(onClick = viewModel::toggleHasStart, modifier = Modifier.size(28.dp)) {
                                                Icon(Icons.Rounded.Close, contentDescription = "Clear start date", tint = MutedInk, modifier = Modifier.size(18.dp))
                                            }
                                        },
                                    )
                                } else {
                                    TextButton(onClick = viewModel::toggleHasStart) { Text("Add a start date") }
                                }
                            }
                        }
                    }
                }
            }

            // Belongs to links
            if (state.type != CreateItemType.GOAL && (state.goals.isNotEmpty() || state.projects.isNotEmpty() || state.targets.isNotEmpty())) {
                item {
                    SoftCardPadded(modifier = Modifier.fillMaxWidth(), cornerRadius = 22.dp) {
                        FormSection(title = "Part of") {
                            if (state.type == CreateItemType.PROJECT || state.type == CreateItemType.TARGET || state.type == CreateItemType.TASK) {
                                LabeledDropdown(
                                    label = "Goal",
                                    value = state.goals.firstOrNull { it.id == state.goalId }?.title,
                                    placeholder = "No goal",
                                    options = state.goals.map { it.id to it.title },
                                    onSelect = viewModel::setGoalLink,
                                )
                            }
                            if (state.type == CreateItemType.TARGET || state.type == CreateItemType.TASK) {
                                LabeledDropdown(
                                    label = "Project",
                                    value = state.projects.firstOrNull { it.id == state.projectId }?.title,
                                    placeholder = "No project",
                                    options = state.projects.map { it.id to it.title },
                                    onSelect = viewModel::setProjectLink,
                                )
                            }
                            if (state.type == CreateItemType.TASK) {
                                LabeledDropdown(
                                    label = "Target",
                                    value = state.targets.firstOrNull { it.id == state.targetId }?.title,
                                    placeholder = "No target",
                                    options = state.targets.map { it.id to it.title },
                                    onSelect = viewModel::setTargetLink,
                                )
                            }
                        }
                    }
                }
            }

            // Category + priority
            item {
                SoftCardPadded(modifier = Modifier.fillMaxWidth(), cornerRadius = 22.dp) {
                    Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
                        FormSection(title = "Category") {
                            CategoryPicker(
                                selected = state.category,
                                onSelect = viewModel::setCategory,
                            )
                        }
                        FormSection(title = "Priority") {
                            PrioritySelector(
                                selected = state.priority,
                                onSelect = viewModel::setPriority,
                            )
                        }
                        if (state.type != CreateItemType.TASK) {
                            FormSection(title = "Card color") {
                                GradientPicker(
                                    selected = state.gradientIndex,
                                    onSelect = viewModel::setGradient,
                                )
                            }
                        }
                    }
                }
            }

            // Status (edit mode only)
            if (state.isEditing) {
                item {
                    SoftCardPadded(modifier = Modifier.fillMaxWidth(), cornerRadius = 22.dp) {
                        FormSection(title = "Status") {
                            ChoiceChipRow(
                                options = listOf(
                                    Status.NOT_STARTED,
                                    Status.IN_PROGRESS,
                                    Status.PAUSED,
                                    Status.COMPLETED,
                                    Status.ARCHIVED,
                                ),
                                selected = state.status,
                                onSelect = viewModel::setStatus,
                                label = { it.label },
                            )
                        }
                    }
                }
            }

            // Quick-add children (create mode only)
            if (!state.isEditing) {
                if (state.type == CreateItemType.GOAL || state.type == CreateItemType.PROJECT) {
                    item {
                        SoftCardPadded(modifier = Modifier.fillMaxWidth(), cornerRadius = 22.dp) {
                            FormSection(title = "Milestones") {
                                AddInlineField(hint = "Add a milestone", onAdd = viewModel::addMilestone)
                                state.milestones.forEachIndexed { index, milestone ->
                                    QuickAddRow(
                                        text = milestone.title,
                                        onRemove = { viewModel.removeMilestone(index) },
                                    )
                                }
                            }
                        }
                    }
                }
                if (state.type == CreateItemType.TARGET) {
                    item {
                        SoftCardPadded(modifier = Modifier.fillMaxWidth(), cornerRadius = 22.dp) {
                            FormSection(title = "Subtasks") {
                                AddInlineField(hint = "Add a subtask", onAdd = viewModel::addSubtask)
                                state.subtasks.forEachIndexed { index, subtask ->
                                    QuickAddRow(
                                        text = subtask.title,
                                        onRemove = { viewModel.removeSubtask(index) },
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Date pickers
    if (pickStart) {
        DatePickerDialog(
            onDismissRequest = { pickStart = false },
            confirmButton = {
                TextButton(onClick = { pickStart = false }) { Text("Cancel") }
            },
        ) {
            val pickerState = rememberDatePickerState(initialSelectedDateMillis = state.start.toUtcMillis())
            DatePicker(state = pickerState, title = { Text("Start date") }, headline = {})
            TextButton(
                onClick = {
                    pickerState.selectedDateMillis?.let { viewModel.setStart(it.fromUtcMillis()) }
                    pickStart = false
                },
                enabled = pickerState.selectedDateMillis != null,
            ) { Text("OK", fontWeight = FontWeight.SemiBold) }
        }
    }
    if (pickEnd) {
        DatePickerDialog(
            onDismissRequest = { pickEnd = false },
            confirmButton = {
                TextButton(onClick = { pickEnd = false }) { Text("Cancel") }
            },
        ) {
            val pickerState = rememberDatePickerState(initialSelectedDateMillis = state.end.toUtcMillis())
            DatePicker(state = pickerState, title = { Text("End date") }, headline = {})
            TextButton(
                onClick = {
                    pickerState.selectedDateMillis?.let { viewModel.setEnd(it.fromUtcMillis()) }
                    pickEnd = false
                },
                enabled = pickerState.selectedDateMillis?.fromUtcMillis()?.let { it >= state.start } == true,
            ) { Text("OK", fontWeight = FontWeight.SemiBold) }
        }
    }
    if (pickDue) {
        DatePickerDialog(
            onDismissRequest = { pickDue = false },
            confirmButton = {
                TextButton(onClick = { pickDue = false }) { Text("Cancel") }
            },
        ) {
            val pickerState = rememberDatePickerState(initialSelectedDateMillis = (state.due ?: LocalDate.now().plusDays(1)).toUtcMillis())
            DatePicker(state = pickerState, title = { Text("Due date") }, headline = {})
            TextButton(
                onClick = {
                    pickerState.selectedDateMillis?.let { viewModel.setDue(it.fromUtcMillis()) }
                    pickDue = false
                },
                enabled = pickerState.selectedDateMillis != null,
            ) { Text("OK", fontWeight = FontWeight.SemiBold) }
        }
    }
}

@Composable
private fun QuickAddRow(
    text: String,
    onRemove: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(RoundedCornerShape(50))
                .background(Primary),
        )
        Spacer(Modifier.width(10.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = Ink,
            modifier = Modifier.weight(1f),
        )
        IconButton(onClick = onRemove, modifier = Modifier.size(30.dp)) {
            Icon(Icons.Rounded.Close, contentDescription = "Remove", tint = MutedInk, modifier = Modifier.size(18.dp))
        }
    }
}
