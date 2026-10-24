package com.example.goalhub.presentation.more

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Restore
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.goalhub.domain.model.Category
import com.example.goalhub.ui.components.ConfirmDialog
import com.example.goalhub.ui.components.SectionHeader
import com.example.goalhub.ui.components.SoftCardPadded
import com.example.goalhub.ui.theme.Gradients
import com.example.goalhub.ui.theme.Ink
import com.example.goalhub.ui.theme.MutedInk
import com.example.goalhub.ui.theme.Primary

@Composable
fun MoreScreen(
    viewModel: MoreViewModel = androidx.lifecycle.viewmodel.compose.viewModel(factory = com.example.goalhub.presentation.AppViewModelProvider.Factory),
) {
    val userName by viewModel.settings.userName.collectAsStateWithLifecycle()
    val remindersEnabled by viewModel.settings.remindersEnabled.collectAsStateWithLifecycle()
    val counts by viewModel.counts.collectAsStateWithLifecycle()

    var showNameDialog by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Text(
                text = "More",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Ink,
            )
        }

        // Profile
        item {
            SoftCardPadded(modifier = Modifier.fillMaxWidth(), cornerRadius = 24.dp) {
                // The card body is a Box, so its children must be stacked in a
                // single Column — otherwise the stats row would overlap the name row.
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(Brush.linearGradient(Gradients.Personal)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = userName.take(1).uppercase(),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = androidx.compose.ui.graphics.Color.White,
                            )
                        }
                        Spacer(Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = userName,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Ink,
                            )
                            Text(
                                text = "Personal goals & targets",
                                style = MaterialTheme.typography.bodySmall,
                                color = MutedInk,
                            )
                        }
                        IconButton(onClick = { showNameDialog = true }) {
                            Icon(Icons.Rounded.Edit, contentDescription = "Edit name", tint = MutedInk)
                        }
                    }
                    if (counts != null) {
                        Spacer(Modifier.height(14.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                        ) {
                            CountPill(counts!!.goals, "Goals")
                            CountPill(counts!!.projects, "Projects")
                            CountPill(counts!!.targets, "Targets")
                            CountPill(counts!!.tasks, "Tasks")
                        }
                    }
                }
            }
        }

        // Notifications
        item {
            SectionHeader(title = "Notifications")
        }
        item {
            SoftCardPadded(modifier = Modifier.fillMaxWidth(), cornerRadius = 20.dp) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.Notifications,
                        contentDescription = null,
                        tint = Primary,
                        modifier = Modifier.size(22.dp),
                    )
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Deadline reminders",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = Ink,
                        )
                        Text(
                            text = "One notification on the deadline morning — completely offline.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MutedInk,
                        )
                    }
                    Switch(
                        checked = remindersEnabled,
                        onCheckedChange = viewModel::setRemindersEnabled,
                    )
                }
            }
        }

        // Categories
        item {
            SectionHeader(title = "Categories")
        }
        item {
            SoftCardPadded(modifier = Modifier.fillMaxWidth(), cornerRadius = 20.dp) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Category.entries.forEach { category ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(RoundedCornerShape(9.dp))
                                    .background(Brush.linearGradient(Gradients.forCategory(category))),
                            )
                            Spacer(Modifier.width(12.dp))
                            Text(
                                text = category.label,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Ink,
                            )
                        }
                    }
                }
            }
        }

        // Data
        item {
            SectionHeader(title = "Data")
        }
        item {
            SoftCardPadded(modifier = Modifier.fillMaxWidth(), cornerRadius = 20.dp) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.Restore,
                        contentDescription = null,
                        tint = Primary,
                        modifier = Modifier.size(22.dp),
                    )
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Restore sample data",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = Ink,
                        )
                        Text(
                            text = "Replaces everything with the starter goals, targets and tasks.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MutedInk,
                        )
                    }
                    TextButton(onClick = { showResetDialog = true }) {
                        Text("Reset", color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }

        // About
        item {
            SectionHeader(title = "About")
        }
        item {
            SoftCardPadded(modifier = Modifier.fillMaxWidth(), cornerRadius = 20.dp) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "GoalHub",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Ink,
                    )
                    Text(
                        text = "Goals → Projects → Targets → Tasks, all offline-first on your device.\nVersion 1.0.0",
                        style = MaterialTheme.typography.bodySmall,
                        color = MutedInk,
                    )
                }
            }
        }
    }

    if (showNameDialog) {
        NameDialog(
            currentName = userName,
            onConfirm = viewModel::setUserName,
            onDismiss = { showNameDialog = false },
        )
    }
    if (showResetDialog) {
        ConfirmDialog(
            title = "Restore sample data?",
            message = "All your current goals, projects, targets and tasks will be replaced with the sample set.",
            confirmLabel = "Restore",
            onConfirm = viewModel::resetSampleData,
            onDismiss = { showResetDialog = false },
        )
    }
}

@Composable
private fun CountPill(count: Int, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = Ink,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MutedInk,
        )
    }
}

@Composable
private fun NameDialog(
    currentName: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var name by remember { mutableStateOf(currentName) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Your name", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onDismiss()
                    onConfirm(name)
                },
                enabled = name.isNotBlank(),
            ) { Text("Save", fontWeight = FontWeight.SemiBold) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}
