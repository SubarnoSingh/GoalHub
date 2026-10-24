package com.example.goalhub.data.local.relations

import androidx.room.Embedded
import androidx.room.Relation
import com.example.goalhub.data.local.entities.GoalEntity
import com.example.goalhub.data.local.entities.MilestoneEntity
import com.example.goalhub.data.local.entities.NoteEntity
import com.example.goalhub.data.local.entities.ProjectEntity
import com.example.goalhub.data.local.entities.SubtaskEntity
import com.example.goalhub.data.local.entities.TargetEntity
import com.example.goalhub.data.local.entities.TaskEntity

data class GoalWithDetails(
    @Embedded val goal: GoalEntity,
    @Relation(parentColumn = "id", entityColumn = "goalId")
    val milestones: List<MilestoneEntity>,
    @Relation(parentColumn = "id", entityColumn = "goalId")
    val projects: List<ProjectEntity>,
    @Relation(parentColumn = "id", entityColumn = "goalId")
    val targets: List<TargetEntity>,
    @Relation(parentColumn = "id", entityColumn = "goalId")
    val tasks: List<TaskEntity>,
)

data class ProjectWithDetails(
    @Embedded val project: ProjectEntity,
    @Relation(parentColumn = "id", entityColumn = "projectId")
    val milestones: List<MilestoneEntity>,
    @Relation(parentColumn = "id", entityColumn = "projectId")
    val targets: List<TargetEntity>,
    @Relation(parentColumn = "id", entityColumn = "projectId")
    val tasks: List<TaskEntity>,
)

data class TargetWithDetails(
    @Embedded val target: TargetEntity,
    @Relation(parentColumn = "id", entityColumn = "targetId")
    val subtasks: List<SubtaskEntity>,
    @Relation(parentColumn = "id", entityColumn = "targetId")
    val notes: List<NoteEntity>,
)

data class TaskWithDetails(
    @Embedded val task: TaskEntity,
    @Relation(parentColumn = "id", entityColumn = "taskId")
    val subtasks: List<SubtaskEntity>,
)
