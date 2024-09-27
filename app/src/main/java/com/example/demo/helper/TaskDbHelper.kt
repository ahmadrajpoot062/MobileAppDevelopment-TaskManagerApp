package com.example.demo

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

class TaskDbHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        val CREATE_TABLE = ("CREATE TABLE $TABLE_NAME ("
                + "$COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "$COLUMN_NAME TEXT, "
                + "$COLUMN_CATEGORY TEXT, "
                + "$COLUMN_DUE_DATE TEXT, "
                + "$COLUMN_IS_COMPLETED INTEGER)")
        db.execSQL(CREATE_TABLE)
        Log.d("TaskDbHelper", "Table created: $TABLE_NAME")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    fun addTask(task: Task):Int {
        val database = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_NAME, task.Name)
            put(COLUMN_CATEGORY, task.Category)
            put(COLUMN_DUE_DATE, task.DueDate)
            put(COLUMN_IS_COMPLETED, if (task.IsCompleted) 1 else 0)
        }
        val id = database.insert(TABLE_NAME, null, values)
        database.close()
        return id.toInt()
    }

    fun deleteAllTasks(): Boolean {
        val result = writableDatabase.delete(TABLE_NAME, null, null)
        return result > 0
    }

    fun getTaskById(taskId: Int): Task? {
        return getAllTasks().find { it.Id == taskId }
    }

    fun getAllTasks(): List<Task> {
        val tasks = mutableListOf<Task>()
        val database = readableDatabase
        var cursor: Cursor? = null
        try {
            cursor = database.query(TABLE_NAME, null, null, null, null, null, null)
            cursor?.use {
                while (it.moveToNext()) {
                    val id = it.getInt(it.getColumnIndexOrThrow(COLUMN_ID))
                    val name = it.getString(it.getColumnIndexOrThrow(COLUMN_NAME))
                    val category = it.getString(it.getColumnIndexOrThrow(COLUMN_CATEGORY))
                    val dueDate = it.getString(it.getColumnIndexOrThrow(COLUMN_DUE_DATE))
                    val isCompleted = it.getInt(it.getColumnIndexOrThrow(COLUMN_IS_COMPLETED)) == 1
                    tasks.add(Task(id, name, category, dueDate, isCompleted))
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            cursor?.close()
            database.close()
        }
        return tasks
    }

    fun updateTask(task: Task) {
        val database = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_NAME, task.Name)
            put(COLUMN_CATEGORY, task.Category)
            put(COLUMN_DUE_DATE, task.DueDate)
            put(COLUMN_IS_COMPLETED, if (task.IsCompleted) 1 else 0)
        }
        database.update(TABLE_NAME, values, "$COLUMN_ID = ?", arrayOf(task.Id.toString()))
        database.close()
    }

    fun deleteTask(task: Task) {
        val database = writableDatabase
        database.delete(TABLE_NAME, "$COLUMN_ID = ?", arrayOf(task.Id.toString()))
        database.close()
    }

    companion object {
        private const val DATABASE_VERSION = 1
        private const val DATABASE_NAME = "tasks.db"
        const val TABLE_NAME = "task"
        const val COLUMN_ID = "Id"
        const val COLUMN_NAME = "Name"
        const val COLUMN_CATEGORY = "Category"
        const val COLUMN_DUE_DATE = "DueDate"
        const val COLUMN_IS_COMPLETED = "IsCompleted"
    }
}