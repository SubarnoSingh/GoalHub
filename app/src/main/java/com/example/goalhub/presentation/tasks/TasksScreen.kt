package com.example.goalhub.presentation.tasks

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.rounded.TaskAlt
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.goalhub.ui.components.EmptyState
import com.example.goalhub.ui.components.PillFilterRow
import com.example.goalhub.ui.components.SearchField
import com.example.goalhub.ui.components.TaskRow
import com.example.goalhub.ui.theme.Ink
import com.example.goalhub.ui.theme.MutedInk

@Composable
fun TasksScreen(
    onOpenTask: (Long) -> Unit,
    viewModel: TasksViewModel = androidx.lifecycle.viewmodel.compose.viewModel(factory = com.example.goalhub.presentation.AppViewModelProvider.Factory),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val query by viewModel.query.collectAsStateWithLifecycle()
    val filter by viewModel.filter.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(
                    text = "Tasks",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Ink,
                )
                SearchField(
                    query = query,
                    onQueryChange = viewModel::setQuery,
                    placeholder = "Search tasks",
                )
                PillFilterRow(
                    options = TasksFilter.entries.map { it.label },
                    selected = filter.label,
                    onSelect = { label ->
                        TasksFilter.entries.firstOrNull { it.label == label }?.let(viewModel::setFilter)
                    },
                )
            }
        }

        if (uiState.grouped.isEmpty()) {
            item {
                EmptyState(
                    icon = androidx.compose.material.icons.Icons.Rounded.TaskAlt,
                    title = if (query.isBlank()) "No tasks here" else "No matches",
                    subtitle = if (query.isBlank()) {
                        "Everyday actions live here — short, concrete, doable now."
                    } else {
                        "Try a different search or filter."
                    },
                )
            }
        } else {
            uiState.grouped.forEach { (bucket, tasks) ->
                item(key = "header_${bucket.name}") {
                    Text(
                        text = bucket.label,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MutedInk,
                        modifier = Modifier.padding(top = 6.dp, bottom = 2.dp),
                    )
                }
                items(tasks, key = { it.id }) { task ->
                    TaskRow(
                        task = task,
                        onToggle = { viewModel.toggleTask(task.id) },
                        onClick = { onOpenTask(task.id) },
                    )
                }
            }
        }
    }
}
