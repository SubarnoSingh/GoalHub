package com.example.goalhub.presentation.goals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.goalhub.data.repository.GoalPlannerRepository
import com.example.goalhub.data.repository.ItemKind
import com.example.goalhub.domain.model.Goal
import com.example.goalhub.domain.model.ProgressMode
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class GoalDetailViewModel(
    private val repository: GoalPlannerRepository,
    private val goalId: Long,
) : ViewModel() {

    val goal: StateFlow<Goal?> = repository.observeGoal(goalId).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = null,
    )

    fun toggleMilestone(milestoneId: Long) = viewModelScope.launch {
        repository.toggleMilestone(milestoneId)
    }

    fun addMilestone(title: String) = viewModelScope.launch {
        repository.addMilestone(goalId, null, title)
    }

    fun deleteMilestone(milestoneId: Long) = viewModelScope.launch {
        repository.deleteMilestone(milestoneId)
    }

    fun toggleTask(taskId: Long) = viewModelScope.launch {
        repository.toggleTask(taskId)
    }

    fun setProgress(progress: Int) = viewModelScope.launch {
        repository.setProgress(ItemKind.GOAL, goalId, progress, ProgressMode.MANUAL)
    }

    fun resetProgressToAuto() = viewModelScope.launch {
        repository.resetProgressToAuto(ItemKind.GOAL, goalId)
    }

    fun deleteGoal() = viewModelScope.launch {
        repository.deleteGoal(goalId)
    }
}
