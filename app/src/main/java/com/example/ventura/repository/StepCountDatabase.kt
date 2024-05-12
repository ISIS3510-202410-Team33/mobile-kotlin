package com.example.ventura.repository

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.ventura.data.models.StepCount
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch


@Database(entities = arrayOf(StepCount::class), version = 1, exportSchema = false)
public abstract class StepCountDatabase : RoomDatabase() {

    abstract fun stepCountDao(): StepCountDao

    private class StepCountDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {

        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch {
                    // TODO: could add sample dates
                }
            }
        }
    }


    companion object {
        // singleton to only have one open instance
        @Volatile
        private var INSTANCE: StepCountDatabase? = null

        fun getDatabase(
            context: Context,
            scope: CoroutineScope
        ): StepCountDatabase {
            // if the INSTANCE is not null, then return it,
            // if it is, then create the database
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    StepCountDatabase::class.java,
                    "step_count_database"
                )
                    .addCallback(StepCountDatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                // return instance
                instance
            }
        }
    }
}