package com.example.remember.feature.mvp.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [GoalEntity::class, GoalCompletionEntity::class],
    version = 1,
    exportSchema = true
)
abstract class RememberDatabase : RoomDatabase() {
    abstract fun goalDao(): GoalDao
    abstract fun goalCompletionDao(): GoalCompletionDao

    companion object {
        @Volatile
        private var instance: RememberDatabase? = null

        fun getInstance(context: Context): RememberDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(
                context.applicationContext,
                RememberDatabase::class.java,
                "remember.db"
            ).build().also { instance = it }
        }
    }
}
