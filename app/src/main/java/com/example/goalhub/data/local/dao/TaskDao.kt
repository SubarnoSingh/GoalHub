package com.example.goalhub.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.goalhub.data.local.entities.TaskEntity
import com.example.goalhub.data.local.relations.TaskWithDetails
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {

    @Transaction
    @Query("SELECT * FROM tasks ORDER BY dueDay ASC")
    fun observeAll(): Flow<List<TaskWithDetails>>

    @Transaction
    @Query("SELECT * FROM tasks WHERE id = :id")
    fun observeById(id: Long): Flow<TaskWithDetails?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(task: TaskEntity): Long

    @Query(
        """
        UPDATE tasks SET goalId = :goalId, projectId = :projectId, targetId = :targetId,
        title = :title, description = :description, startDay = :startDay, dueDay = :dueDay,
        priority = :priority, category = :category WHERE id = :id
        """
    )
    suspend fun updateFields(
        id: Long,
        goalId: Long?,
        projectId: Long?,
        targetId: Long?,
        title: String,
        description: String,
        startDay: Long?,
        dueDay: Long?,
        priority: String,
        category: String,
    )

    @Query("UPDATE tasks SET completed = :completed, completedAt = :completedAt WHERE id = :id")
    suspend fun setCompleted(id: Long, completed: Boolean, completedAt: Long?)

    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("SELECT COUNT(*) FROM tasks")
    suspend fun count(): Int

    @Query("DELETE FROM tasks")
    suspend fun deleteAll(): Int
}
