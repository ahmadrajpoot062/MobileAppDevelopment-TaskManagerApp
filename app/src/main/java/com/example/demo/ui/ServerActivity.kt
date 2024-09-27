package com.example.demo

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.demo.InitializeFirebase.Companion.isLogin
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.example.demo.NoteRetrofitInstance.api as noteApi
import com.example.demo.TaskRetrofitInstance.api as taskApi

class ServerActivity: AppCompatActivity() {
    private var action: String? = "backup"
    private var from: String? = "note"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        InitializeFirebase.init()
        if (!isLogin) {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        } else {
            performServerOperation()
        }
    }

    private fun performServerOperation() {
        if (from == "note") {
            when (action) {
                "backup" -> notesBackup()
                "recover" -> notesRecover()
            }
        } else {
            when (action) {
                "backup" -> tasksBackup()
                "recover" -> tasksRecover()
            }
        }
    }

    private fun getAllNotes(): List<Note> = NoteDbHelper(this).allNotes()

    private fun getAllTasks(): List<Task> = TaskDbHelper(this).getAllTasks()

    private fun returnToActivity(){
        if (from == "note") {
            startActivity(Intent(this, NoteActivity::class.java))
        } else {
            val intent = Intent(this, TaskActivity::class.java)
            startActivity(intent)
        }
    }

    private fun notesBackup() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = noteApi.saveNotes(getAllNotes())

                if (response.success) {
                    runOnUiThread {
                        Toast.makeText(this@ServerActivity, "Notes backed up successfully", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(this@ServerActivity, "Error: ${response.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Log.e("API_ERROR", "Error backing up notes to server", e)
                runOnUiThread {
                    Toast.makeText(this@ServerActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
        returnToActivity()
    }

    private fun modifyLocalNotes(notes: List<Note>) {
        val dbHelper = NoteDbHelper(this)
        dbHelper.deleteAllNotes()
        notes.forEach { note ->
            dbHelper.insertNote(note)
        }
    }

    private fun notesRecover() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val notes = noteApi.fetchNotes()
                withContext(Dispatchers.Main) {
                    modifyLocalNotes(notes)
                    Toast.makeText(this@ServerActivity, "Notes recovered successfully", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main){
                    Toast.makeText(this@ServerActivity, "Failed to recover notes: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
        returnToActivity()
    }

    private fun tasksBackup() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = taskApi.saveTasks(getAllTasks())

                if (response.success) {
                    runOnUiThread {
                        Toast.makeText(this@ServerActivity, "Tasks backed up successfully", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(this@ServerActivity, "Error: ${response.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Log.e("API_ERROR", "Error backing up tasks to server", e)
                runOnUiThread {
                    Toast.makeText(this@ServerActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
        returnToActivity()
    }

    private fun modifyLocalTasks(tasks: List<Task>) {
        val dbHelper = TaskDbHelper(this)
        dbHelper.deleteAllTasks()
        tasks.forEach { task ->
            dbHelper.addTask(task)
        }
    }

    private fun tasksRecover() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val tasks = taskApi.fetchTasks()
                withContext(Dispatchers.Main) {
                    modifyLocalTasks(tasks)
                    Toast.makeText(this@ServerActivity, "Tasks recovered successfully", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main){
                    Toast.makeText(this@ServerActivity, "Failed to recover tasks: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
        returnToActivity()
    }
}