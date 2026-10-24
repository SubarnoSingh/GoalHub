package com.example.goalhub.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.goalhub.data.local.entities.ProjectEntity
import com.example.goalhub.data.local.relations.ProjectWithDetails
import kotlinx.coroutines.flow.Flow

@Dao
interface ProjectDao {

    @Transaction
    @Query("SELECT * FROM projects ORDER BY endDay ASC")
    fun observeAll(): Flow<List<ProjectWithDetails>>

    @Transaction
    @Query("SELECT * FROM projects WHERE id = :id")
    fun observeById(id: Long): Flow<ProjectWithDetails?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(project: ProjectEntity): Long

    @Query(
        """
        UPDATE projects SET goalId = :goalId, title = :title, description = :description,
        startDay = :startDay, endDay = :endDay, status = :status, priority = :priority,
        category = :category, gradientIndex = :gradientIndex WHERE id = :id
        """
    )
    suspend fun updateFields(
        id: Long,
        goalId: Long?,
        title: String,
        description: String,
        startDay: Long,
        endDay: Long,
        status: String,
        priority: String,
        category: String,
        gradientIndex: Int,
    )

    @Query(
        "UPDATE projects SET progress = :progress, progressMode = :mode, status = :status, completedAt = :completedAt WHERE id = :id"
    )
    suspend fun updateProgress(id: Long, progress: Int, mode: String, status: String, completedAt: Long?)

    @Query("DELETE FROM projects WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("SELECT COUNT(*) FROM projects")
    suspend fun count(): Int

    @Query("DELETE FROM projects")
    suspend fun deleteAll(): Int

    // ---- Milestones (shared table, queried through this DAO for projects) ----

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertMilestone(milestone: com.example.goalhub.data.local.entities.MilestoneEntity): Long
}
