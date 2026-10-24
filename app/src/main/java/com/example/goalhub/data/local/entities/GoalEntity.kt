package com.example.goalhub.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * A long-term goal. Dates are stored as epoch days (LocalDate.toEpochDay) so
 * range queries and span calculations are trivial integer comparisons.
 */
@Entity(tableName = "goals")
data class GoalEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
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
