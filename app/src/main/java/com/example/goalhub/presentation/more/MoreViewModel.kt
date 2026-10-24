package com.example.goalhub.presentation.more

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.goalhub.data.local.SettingsStore
import com.example.goalhub.data.repository.GoalPlannerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class DataCounts(
    val goals: Int,
    val projects: Int,
    val targets: Int,
    val tasks: Int,
)

class MoreViewModel(
    private val repository: GoalPlannerRepository,
    val settings: SettingsStore,
) : ViewModel() {

    private val _counts = MutableStateFlow<DataCounts?>(null)
    val counts: StateFlow<DataCounts?> = _counts.asStateFlow()

    init {
        viewModelScope.launch {
            _counts.value = DataCounts(
                goals = repository.countGoals(),
                projects = repository.countProjects(),
                targets = repository.countTargets(),
                tasks = repository.countTasks(),
            )
        }
    }

    fun setUserName(name: String) = settings.setUserName(name)
    fun setRemindersEnabled(enabled: Boolean) = settings.setRemindersEnabled(enabled)

    fun resetSampleData() = viewModelScope.launch {
        repository.clearAndReseed()
        _counts.value = DataCounts(
            goals = repository.countGoals(),
            projects = repository.countProjects(),
            targets = repository.countTargets(),
            tasks = repository.countTasks(),
        )
    }
}
