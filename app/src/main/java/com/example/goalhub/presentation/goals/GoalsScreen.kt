package com.example.goalhub.presentation.goals

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.rounded.Flag
import androidx.compose.material.icons.rounded.RocketLaunch
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.goalhub.ui.components.ChoiceChipRow
import com.example.goalhub.ui.components.EmptyState
import com.example.goalhub.ui.components.GoalListCard
import com.example.goalhub.ui.components.PillFilterRow
import com.example.goalhub.ui.components.ProjectCard
import com.example.goalhub.ui.components.SoftCardPadded
import com.example.goalhub.ui.components.TintedSurface
import com.example.goalhub.ui.theme.Ink

@Composable
fun GoalsScreen(
    onOpenGoal: (Long) -> Unit,
    onOpenProject: (Long) -> Unit,
    onCreate: (com.example.goalhub.ui.components.CreateItemType, Long?) -> Unit,
    viewModel: GoalsViewModel = androidx.lifecycle.viewmodel.compose.viewModel(factory = com.example.goalhub.presentation.AppViewModelProvider.Factory),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Text(
                text = "Goals & Projects",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Ink,
            )
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                ChoiceChipRow(
                    options = GoalsSegment.entries.toList(),
                    selected = uiState.segment,
                    onSelect = viewModel::setSegment,
                    label = { it.label },
                )
                PillFilterRow(
                    options = StatusFilter.entries.map { it.label },
                    selected = uiState.filter.label,
                    onSelect = { label ->
                        StatusFilter.entries.firstOrNull { it.label == label }?.let(viewModel::setFilter)
                    },
                )
            }
        }

        when (uiState.segment) {
            GoalsSegment.GOALS -> {
                if (uiState.visibleGoals.isEmpty()) {
                    item {
                        EmptyState(
                            icon = androidx.compose.material.icons.Icons.Rounded.Flag,
                            title = if (uiState.filter == StatusFilter.ACTIVE) "No active goals yet" else "Nothing here",
                            subtitle = "Create a long-term goal and break it into milestones, targets and tasks.",
                            action = {
                                TintedSurface {
                                    Text(
                                        text = "New goal",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.SemiBold,
                                        color = com.example.goalhub.ui.theme.Primary,
                                        modifier = Modifier
                                            .clickable { onCreate(com.example.goalhub.ui.components.CreateItemType.GOAL, null) }
                                            .padding(horizontal = 18.dp, vertical = 10.dp),
                                    )
                                }
                            },
                        )
                    }
                } else {
                    items(uiState.visibleGoals, key = { it.id }) { goal ->
                        GoalListCard(
                            goal = goal,
                            onClick = { onOpenGoal(goal.id) },
                        )
                    }
                }
            }

            GoalsSegment.PROJECTS -> {
                if (uiState.visibleProjects.isEmpty()) {
                    item {
                        EmptyState(
                            icon = androidx.compose.material.icons.Icons.Rounded.RocketLaunch,
                            title = "No projects here",
                            subtitle = "A project groups many tasks and targets under one deadline.",
                            action = {
                                TintedSurface {
                                    Text(
                                        text = "New project",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.SemiBold,
                                        color = com.example.goalhub.ui.theme.Primary,
                                        modifier = Modifier
                                            .clickable { onCreate(com.example.goalhub.ui.components.CreateItemType.PROJECT, null) }
                                            .padding(horizontal = 18.dp, vertical = 10.dp),
                                    )
                                }
                            },
                        )
                    }
                } else {
                    items(uiState.visibleProjects, key = { it.id }) { project ->
                        ProjectCard(project = project, onClick = { onOpenProject(project.id) })
                    }
                }
            }
        }
    }
}
