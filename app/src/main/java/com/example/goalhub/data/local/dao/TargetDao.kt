package com.example.goalhub.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.goalhub.data.local.entities.NoteEntity
import com.example.goalhub.data.local.entities.SubtaskEntity
import com.example.goalhub.data.local.entities.TargetEntity
import com.example.goalhub.data.local.relations.TargetWithDetails
import kotlinx.coroutines.flow.Flow

@Dao
interface TargetDao {

    @Transaction
    @Query("SELECT * FROM targets ORDER BY endDay ASC")
    fun observeAll(): Flow<List<TargetWithDetails>>

    @Transaction
    @Query("SELECT * FROM targets WHERE id = :id")
    fun observeById(id: Long): Flow<TargetWithDetails?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(target: TargetEntity): Long

    @Query(
        """
        UPDATE targets SET goalId = :goalId, projectId = :projectId, title = :title,
        description = :description, startDay = :startDay, endDay = :endDay, status = :status,
        priority = :priority, category = :category, gradientIndex = :gradientIndex WHERE id = :id
        """
    )
    suspend fun updateFields(
        id: Long,
        goalId: Long?,
        projectId: Long?,
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
        "UPDATE targets SET progress = :progress, progressMode = :mode, status = :status, completedAt = :completedAt WHERE id = :id"
    )
    suspend fun updateProgress(id: Long, progress: Int, mode: String, status: String, completedAt: Long?)

    @Query("DELETE FROM targets WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("SELECT COUNT(*) FROM targets")
    suspend fun count(): Int

    @Query("DELETE FROM targets")
    suspend fun deleteAll(): Int

    // ---- Subtasks ----

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSubtask(subtask: SubtaskEntity): Long

    @Query("UPDATE subtasks SET completed = :completed, completedAt = :completedAt WHERE id = :id")
    suspend fun setSubtaskCompleted(id: Long, completed: Boolean, completedAt: Long?)

    @Query("DELETE FROM subtasks WHERE id = :id")
    suspend fun deleteSubtask(id: Long)

    @Query("SELECT * FROM subtasks WHERE id = :id")
    suspend fun subtaskById(id: Long): SubtaskEntity?

    // ---- Notes ----

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertNote(note: NoteEntity): Long

    @Query("DELETE FROM notes WHERE id = :id")
    suspend fun deleteNote(id: Long)
}
