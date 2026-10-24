package com.example.goalhub.presentation

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.goalhub.GoalHubApplication
import com.example.goalhub.presentation.calendar.CalendarViewModel
import com.example.goalhub.presentation.create.CreateItemViewModel
import com.example.goalhub.presentation.goals.GoalDetailViewModel
import com.example.goalhub.presentation.goals.GoalsViewModel
import com.example.goalhub.presentation.goals.ProjectDetailViewModel
import com.example.goalhub.presentation.home.HomeViewModel
import com.example.goalhub.presentation.more.MoreViewModel
import com.example.goalhub.presentation.targets.TargetDetailViewModel
import com.example.goalhub.presentation.targets.TargetsViewModel
import com.example.goalhub.presentation.tasks.TasksViewModel
import com.example.goalhub.ui.components.CreateItemType

/** All ViewModel wiring in one place — swap the container for Hilt/Koin here. */
object AppViewModelProvider {

    val Factory = viewModelFactory {
        initializer { HomeViewModel(app().container.repository, app().container.settings) }
        initializer { GoalsViewModel(app().container.repository) }
        initializer { TargetsViewModel(app().container.repository) }
        initializer { TasksViewModel(app().container.repository) }
        initializer { CalendarViewModel(app().container.repository) }
        initializer { MoreViewModel(app().container.repository, app().container.settings) }
    }

    fun goalDetail(goalId: Long) = viewModelFactory {
        initializer { GoalDetailViewModel(app().container.repository, goalId) }
    }

    fun projectDetail(projectId: Long) = viewModelFactory {
        initializer { ProjectDetailViewModel(app().container.repository, projectId) }
    }

    fun targetDetail(targetId: Long) = viewModelFactory {
        initializer { TargetDetailViewModel(app().container.repository, targetId) }
    }

    fun create(
        type: CreateItemType,
        editId: Long?,
        goalLink: Long?,
        projectLink: Long?,
        targetLink: Long?,
    ) = viewModelFactory {
        initializer {
            CreateItemViewModel(
                repository = app().container.repository,
                type = type,
                editId = editId,
                goalLink = goalLink,
                projectLink = projectLink,
                targetLink = targetLink,
            )
        }
    }
}

private fun CreationExtras.app(): GoalHubApplication =
    this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as GoalHubApplication
