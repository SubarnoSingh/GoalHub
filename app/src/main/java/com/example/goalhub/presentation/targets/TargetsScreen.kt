package com.example.goalhub.presentation.targets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.goalhub.domain.model.PlanningPeriod
import com.example.goalhub.ui.components.EmptyState
import com.example.goalhub.ui.components.PillFilterRow
import com.example.goalhub.ui.components.TargetCard
import com.example.goalhub.ui.theme.Ink

@Composable
fun TargetsScreen(
    onOpenTarget: (Long) -> Unit,
    viewModel: TargetsViewModel = androidx.lifecycle.viewmodel.compose.viewModel(factory = com.example.goalhub.presentation.AppViewModelProvider.Factory),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(
                    text = "Targets",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Ink,
                )
                PillFilterRow(
                    options = listOf(
                        PlanningPeriod.ACTIVE,
                        PlanningPeriod.TODAY,
                        PlanningPeriod.TOMORROW,
                        PlanningPeriod.THIS_WEEK,
                        PlanningPeriod.NEXT_WEEK,
                        PlanningPeriod.UPCOMING,
                        PlanningPeriod.COMPLETED,
                        PlanningPeriod.ALL,
                    ).map { it.label },
                    selected = uiState.filter.label,
                    onSelect = { label ->
                        PlanningPeriod.entries.firstOrNull { it.label == label }?.let(viewModel::setFilter)
                    },
                )
                Text(
                    text = when (uiState.filter) {
                        PlanningPeriod.ACTIVE -> "Targets that are open right now — a target can span several days."
                        PlanningPeriod.COMPLETED -> "Finished work, ready to celebrate."
                        PlanningPeriod.ALL -> "Everything, past and future."
                        PlanningPeriod.UPCOMING -> "Not started yet, but coming up."
                        else -> "Every target that overlaps ${uiState.filter.label.lowercase()}."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        if (uiState.visible.isEmpty()) {
            item {
                EmptyState(
                    icon = androidx.compose.material.icons.Icons.Rounded.CheckCircle,
                    title = "Nothing here",
                    subtitle = "Create a short-term target with its own start and end date — it will stay active across every day it spans.",
                )
            }
        } else {
            items(uiState.visible, key = { it.id }) { target ->
                TargetCard(target = target, onClick = { onOpenTarget(target.id) })
            }
        }
    }
}
