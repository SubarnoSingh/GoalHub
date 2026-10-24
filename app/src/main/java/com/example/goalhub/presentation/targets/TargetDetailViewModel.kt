package com.example.goalhub.presentation.targets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.goalhub.data.repository.GoalPlannerRepository
import com.example.goalhub.data.repository.ItemKind
import com.example.goalhub.domain.model.Goal
import com.example.goalhub.domain.model.ProgressMode
import com.example.goalhub.domain.model.Project
import com.example.goalhub.domain.model.Target
import com.example.goalhub.domain.model.Task
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TargetDetailViewModel(
    private val repository: GoalPlannerRepository,
    private val targetId: Long,
) : ViewModel() {

    val target: StateFlow<Target?> = repository.observeTarget(targetId).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = null,
    )

    /** Parent goal / project names for the "Part of" rows. */
    val parentGoal: StateFlow<Goal?> = repository.observeTarget(targetId)
        .flatMapLatest { t -> if (t?.goalId != null) repository.observeGoal(t.goalId) else flowOf(null) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val parentProject: StateFlow<Project?> = repository.observeTarget(targetId)
        .flatMapLatest { t -> if (t?.projectId != null) repository.observeProject(t.projectId) else flowOf(null) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    /** Tasks that hang off this target. */
    val linkedTasks: StateFlow<List<Task>> = repository.observeTasks()
        .map { tasks -> tasks.filter { it.targetId == targetId }.sortedBy { it.due } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun toggleSubtask(subtaskId: Long) = viewModelScope.launch {
        repository.toggleSubtask(subtaskId)
    }

    fun addSubtask(title: String) = viewModelScope.launch {
        repository.addSubtask(targetId, title)
    }

    fun deleteSubtask(subtaskId: Long) = viewModelScope.launch {
        repository.deleteSubtask(subtaskId)
    }

    fun addNote(text: String) = viewModelScope.launch {
        repository.addNote(targetId, text)
    }

    fun deleteNote(noteId: Long) = viewModelScope.launch {
        repository.deleteNote(noteId)
    }

    fun toggleTask(taskId: Long) = viewModelScope.launch {
        repository.toggleTask(taskId)
    }

    fun setProgress(progress: Int) = viewModelScope.launch {
        repository.setProgress(ItemKind.TARGET, targetId, progress, ProgressMode.MANUAL)
    }

    fun resetProgressToAuto() = viewModelScope.launch {
        repository.resetProgressToAuto(ItemKind.TARGET, targetId)
    }

    fun deleteTarget() = viewModelScope.launch {
        repository.deleteTarget(targetId)
    }
}
