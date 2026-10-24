package com.example.goalhub.presentation.goals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.goalhub.data.repository.GoalPlannerRepository
import com.example.goalhub.data.repository.ItemKind
import com.example.goalhub.domain.model.Goal
import com.example.goalhub.domain.model.ProgressMode
import com.example.goalhub.domain.model.Project
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ProjectDetailViewModel(
    private val repository: GoalPlannerRepository,
    private val projectId: Long,
) : ViewModel() {

    val project: StateFlow<Project?> = repository.observeProject(projectId).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = null,
    )

    /** The goal this project hangs off, if any — lets the detail screen show its real name. */
    val parentGoal: StateFlow<Goal?> = repository.observeProject(projectId)
        .flatMapLatest { p ->
            if (p?.goalId != null) repository.observeGoal(p.goalId) else flowOf(null)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null,
        )

    fun toggleMilestone(milestoneId: Long) = viewModelScope.launch {
        repository.toggleMilestone(milestoneId)
    }

    fun addMilestone(title: String) = viewModelScope.launch {
        repository.addMilestone(null, projectId, title)
    }

    fun deleteMilestone(milestoneId: Long) = viewModelScope.launch {
        repository.deleteMilestone(milestoneId)
    }

    fun toggleTask(taskId: Long) = viewModelScope.launch {
        repository.toggleTask(taskId)
    }

    fun setProgress(progress: Int) = viewModelScope.launch {
        repository.setProgress(ItemKind.PROJECT, projectId, progress, ProgressMode.MANUAL)
    }

    fun resetProgressToAuto() = viewModelScope.launch {
        repository.resetProgressToAuto(ItemKind.PROJECT, projectId)
    }

    fun deleteProject() = viewModelScope.launch {
        repository.deleteProject(projectId)
    }
}
