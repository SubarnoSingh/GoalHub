package com.example.goalhub.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/** A multi-task undertaking; optionally linked to a [GoalEntity]. */
@Entity(
    tableName = "projects",
    foreignKeys = [
        ForeignKey(
            entity = GoalEntity::class,
            parentColumns = ["id"],
            childColumns = ["goalId"],
            onDelete = ForeignKey.CASCADE,
        )
    ],
    indices = [Index("goalId")],
)
data class ProjectEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val goalId: Long? = null,
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
