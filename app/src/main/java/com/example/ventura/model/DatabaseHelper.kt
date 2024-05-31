package com.example.ventura.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.ventura.model.Task
import java.time.LocalDate

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "tasks.db"
        private const val DATABASE_VERSION = 1

        private const val TABLE_TASKS = "tasks"
        private const val COLUMN_ID = "id"
        private const val COLUMN_DATE = "date"
        private const val COLUMN_TITLE = "title"
        private const val COLUMN_DESCRIPTION = "description"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createTasksTable = ("CREATE TABLE " + TABLE_TASKS + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_DATE + " TEXT,"
                + COLUMN_TITLE + " TEXT,"
                + COLUMN_DESCRIPTION + " TEXT" + ")")
        db?.execSQL(createTasksTable)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_TASKS")
        onCreate(db)
    }

    fun insertTask(date: LocalDate, title: String, description: String) {
        val db = this.writableDatabase
        val contentValues = ContentValues().apply {
            put(COLUMN_DATE, date.toString())
            put(COLUMN_TITLE, title)
            put(COLUMN_DESCRIPTION, description)
        }
        db.insert(TABLE_TASKS, null, contentValues)
        db.close()
    }

    fun getTasksForDate(date: LocalDate?): List<Task> {
        val tasks = mutableListOf<Task>()
        val db = this.readableDatabase
        val cursor = db.query(
            TABLE_TASKS,
            arrayOf(COLUMN_ID, COLUMN_DATE, COLUMN_TITLE, COLUMN_DESCRIPTION),
            "$COLUMN_DATE = ?",
            arrayOf(date.toString()),
            null,
            null,
            null
        )

        cursor?.use {
            if (it.moveToFirst()) {
                do {
                    val task = Task(
                        it.getInt(it.getColumnIndexOrThrow(COLUMN_ID)),
                        LocalDate.parse(it.getString(it.getColumnIndexOrThrow(COLUMN_DATE))),
                        it.getString(it.getColumnIndexOrThrow(COLUMN_TITLE)),
                        it.getString(it.getColumnIndexOrThrow(COLUMN_DESCRIPTION))
                    )
                    tasks.add(task)
                } while (it.moveToNext())
            }
        }

        db.close()
        return tasks
    }
}
