package com.example.goalhub.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.goalhub.data.local.SettingsStore
import com.example.goalhub.data.repository.GoalPlannerRepository
import com.example.goalhub.domain.model.DashboardData
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class HomeUiState(
    val userName: String = "",
    val dashboard: DashboardData = DashboardData(
        activeGoals = emptyList(),
        activeTargets = emptyList(),
        todayTasks = emptyList(),
        weekDeadlines = emptyList(),
    ),
)

class HomeViewModel(
    private val repository: GoalPlannerRepository,
    settings: SettingsStore,
) : ViewModel() {

    fun toggleTask(taskId: Long) {
        viewModelScope.launch { repository.toggleTask(taskId) }
    }

    val uiState: StateFlow<HomeUiState> = combine(
        repository.observeDashboard(),
        settings.userName,
    ) { dashboard, userName ->
        HomeUiState(userName = userName, dashboard = dashboard)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState(),
    )
}
