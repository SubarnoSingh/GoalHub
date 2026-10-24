package com.example.goalhub.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.goalhub.data.local.dao.GoalDao
import com.example.goalhub.data.local.dao.ProjectDao
import com.example.goalhub.data.local.dao.TargetDao
import com.example.goalhub.data.local.dao.TaskDao
import com.example.goalhub.data.local.entities.GoalEntity
import com.example.goalhub.data.local.entities.MilestoneEntity
import com.example.goalhub.data.local.entities.NoteEntity
import com.example.goalhub.data.local.entities.ProjectEntity
import com.example.goalhub.data.local.entities.SubtaskEntity
import com.example.goalhub.data.local.entities.TargetEntity
import com.example.goalhub.data.local.entities.TaskEntity

@Database(
    entities = [
        GoalEntity::class,
        ProjectEntity::class,
        TargetEntity::class,
        TaskEntity::class,
        MilestoneEntity::class,
        SubtaskEntity::class,
        NoteEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
abstract class GoalPlannerDatabase : RoomDatabase() {

    abstract fun goalDao(): GoalDao
    abstract fun projectDao(): ProjectDao
    abstract fun targetDao(): TargetDao
    abstract fun taskDao(): TaskDao

    companion object {
        fun build(context: Context): GoalPlannerDatabase =
            Room.databaseBuilder(
                context,
                GoalPlannerDatabase::class.java,
                "goalhub.db",
            )
                // Schema is owned by the app; on a future migration failure the
                // sample data is simply reseeded (see SampleDataProvider).
                .fallbackToDestructiveMigration()
                .build()
    }
}
