package com.example.goalhub.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/** A short-term target spanning a date range; optionally under a goal and/or project. */
@Entity(
    tableName = "targets",
    foreignKeys = [
        ForeignKey(
            entity = GoalEntity::class,
            parentColumns = ["id"],
            childColumns = ["goalId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = ProjectEntity::class,
            parentColumns = ["id"],
            childColumns = ["projectId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("goalId"), Index("projectId")],
)
data class TargetEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val goalId: Long? = null,
    val projectId: Long? = null,
    val title: String,
    val description: String = "",
    val startDay: Long,
    val endDay: Long,
    val status: String = "NOT_STARTED",
    val priority: String = "MEDIUM",
    val category: String = "OTHER",
    val progress: Int = 0,
    val progressMode: String = "AUTO",
    val gradientIndex: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null,
)

/** A checkbox item inside a target (or, in future, a task). */
@Entity(
    tableName = "subtasks",
    foreignKeys = [
        ForeignKey(
            entity = TargetEntity::class,
            parentColumns = ["id"],
            childColumns = ["targetId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = TaskEntity::class,
            parentColumns = ["id"],
            childColumns = ["taskId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("targetId"), Index("taskId")],
)
data class SubtaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val targetId: Long? = null,
    val taskId: Long? = null,
    val title: String,
    val completed: Boolean = false,
    val completedAt: Long? = null,
    val position: Int = 0,
)

/** A free-form note on a target. */
@Entity(
    tableName = "notes",
    foreignKeys = [
        ForeignKey(
            entity = TargetEntity::class,
            parentColumns = ["id"],
            childColumns = ["targetId"],
            onDelete = ForeignKey.CASCADE,
        )
    ],
    indices = [Index("targetId")],
)
data class NoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val targetId: Long,
    val text: String,
    val createdAt: Long = System.currentTimeMillis(),
)
