package com.example.goalhub.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.goalhub.data.local.entities.GoalEntity
import com.example.goalhub.data.local.entities.MilestoneEntity
import com.example.goalhub.data.local.relations.GoalWithDetails
import kotlinx.coroutines.flow.Flow

@Dao
interface GoalDao {

    @Transaction
    @Query("SELECT * FROM goals ORDER BY endDay ASC")
    fun observeAll(): Flow<List<GoalWithDetails>>

    @Transaction
    @Query("SELECT * FROM goals WHERE id = :id")
    fun observeById(id: Long): Flow<GoalWithDetails?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(goal: GoalEntity): Long

    @Query(
        """
        UPDATE goals SET title = :title, description = :description, startDay = :startDay,
        endDay = :endDay, status = :status, priority = :priority, category = :category,
        gradientIndex = :gradientIndex WHERE id = :id
        """
    )
    suspend fun updateFields(
        id: Long,
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
        "UPDATE goals SET progress = :progress, progressMode = :mode, status = :status, completedAt = :completedAt WHERE id = :id"
    )
    suspend fun updateProgress(id: Long, progress: Int, mode: String, status: String, completedAt: Long?)

    @Query("DELETE FROM goals WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("SELECT COUNT(*) FROM goals")
    suspend fun count(): Int

    @Query("DELETE FROM goals")
    suspend fun deleteAll(): Int

    // ---- Milestones ----

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertMilestone(milestone: MilestoneEntity): Long

    @Query("UPDATE milestones SET completed = :completed, completedAt = :completedAt WHERE id = :id")
    suspend fun setMilestoneCompleted(id: Long, completed: Boolean, completedAt: Long?)

    @Query("DELETE FROM milestones WHERE id = :id")
    suspend fun deleteMilestone(id: Long)

    @Query("SELECT * FROM milestones WHERE id = :id")
    suspend fun milestoneById(id: Long): MilestoneEntity?
}
