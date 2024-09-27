package com.example.demo

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.demo.InitializeFirebase.Companion.auth
import com.example.demo.InitializeFirebase.Companion.isLogin
import com.google.android.material.imageview.ShapeableImageView

class TaskActivity : AppCompatActivity(), TaskAdapter.OnClickListener {

    private lateinit var rvTasks: RecyclerView
    private lateinit var emptyView: TextView
    private lateinit var emptyIcon: ImageView
    private lateinit var searchIcon: ImageView
    private lateinit var countView: TextView
    private lateinit var openDialog: ShapeableImageView
    private lateinit var taskAdapter: TaskAdapter
    private lateinit var dbHelper: TaskDbHelper
    private lateinit var toolbar: Toolbar
    private lateinit var category: String
    private lateinit var topImg: ImageView

    private val tasks: ArrayList<Any> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task)

        InitializeFirebase.init()
        rvTasks = findViewById(R.id.rv_tasks)
        searchIcon=findViewById(R.id.search_icon)
        countView=findViewById(R.id.tv_count)
        emptyView = findViewById(R.id.tv_empty_view)
        openDialog = findViewById(R.id.btadd)
        emptyIcon = findViewById(R.id.iv_empty_icon)
        toolbar = findViewById(R.id.toolbar)
        topImg = findViewById(R.id.btn_logo)

        setSupportActionBar(toolbar)

        category = intent.getStringExtra("category").toString()

        supportActionBar?.title = category

        setCategoryImage()

        dbHelper = TaskDbHelper(this)

        openDialog.setOnClickListener {
            showAddTaskDialog(false, null, -1)
        }

        rvTasks.layoutManager = LinearLayoutManager(this)

        taskAdapter = TaskAdapter(tasks, { task, isChecked ->
            handleTaskCompletion(task, isChecked)
        }).apply {
            setListener(this@TaskActivity)
        }

        rvTasks.adapter = taskAdapter

        loadTasksFromDatabase()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_scrolling, menu)
        setVisibility()
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_task -> {
                startActivity(Intent(this, TaskListActivity::class.java))
                true
            }
            R.id.action_note -> {
                startActivity(Intent(this, NoteActivity::class.java))
                true
            }
            R.id.action_about -> {
                startActivity(Intent(this, AboutActivity::class.java))
                true
            }
            R.id.action_backup -> {
                val intent = Intent(this, ServerActivity::class.java)
                startActivity(intent)
                true
            }
            R.id.action_recover -> {
                val intent = Intent(this, ServerActivity::class.java)
                startActivity(intent)
                true
            }
            R.id.action_logout -> {
                auth.signOut()
                Toast.makeText(this, "Logged Out", Toast.LENGTH_SHORT).show()
                setVisibility()
                startActivity(Intent(this, IntroActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setVisibility(){
        toolbar.menu.findItem(R.id.action_logout).isVisible = isLogin
    }

    private fun setCategoryImage() {
        if(category == "Personal") {
            topImg.setImageResource(R.drawable.ic_personal)
        } else if(category == "Work") {
            topImg.setImageResource(R.drawable.ic_work)
        } else if(category == "Grocery") {
            topImg.setImageResource(R.drawable.ic_grocery)
        } else if(category == "School") {
            topImg.setImageResource(R.drawable.ic_school)
        } else if(category=="General"){
            topImg.setImageResource(R.drawable.ic_general)
        }
    }

    private fun loadTasksFromDatabase() {
        val allTasks = dbHelper.getAllTasks().filter { it.Category == category }
        tasks.clear()

        if(allTasks.isEmpty())
        {
            showEmptyView("No work to do")
        }
        else {
            hideEmptyView(allTasks.count())
            val notCompletedTasks = allTasks.filter { !it.IsCompleted }
            if (notCompletedTasks.isNotEmpty()) {
                tasks.add("Not Completed")
                tasks.addAll(notCompletedTasks)
            }

            val completedTasks = allTasks.filter { it.IsCompleted }
            if (completedTasks.isNotEmpty()) {
                tasks.add("Completed")
                tasks.addAll(completedTasks)
            }
        }
        taskAdapter.notifyDataSetChanged()
    }

    private fun showEmptyView(message: String) {
        val emptyView: TextView = findViewById(R.id.tv_empty_view)
        emptyView.text = message
        emptyView.visibility = View.VISIBLE
        emptyIcon.visibility = View.VISIBLE
        rvTasks.visibility = View.GONE
        searchIcon.visibility= View.GONE
        countView.visibility = View.GONE
    }

    private fun deleteConfirmation(task: Task) {
        AlertDialog.Builder(this)
            .setTitle("Confirm Delete")
            .setMessage("Are you sure you want to delete ${task.Name}?")
            .setIcon(R.drawable.ic_delete)
            .setPositiveButton("YES") { _, _ ->
                try {
                    dbHelper.deleteTask(task)
                    loadTasksFromDatabase()
                } catch (e: Exception) {
                    Toast.makeText(this@TaskActivity, "Error deleting note", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("NO") { dialog, _ ->
                dialog.cancel()
            }
            .show()
    }

    private fun hideEmptyView(count: Int) {
        emptyView.visibility = View.GONE
        emptyIcon.visibility = View.GONE
        rvTasks.visibility = View.VISIBLE
        searchIcon.visibility= View.VISIBLE
        countView.visibility = View.VISIBLE
        countView.text = "My Tasks ($count)"
    }

    private fun handleTaskCompletion(task: Task, isChecked: Boolean) {
        task.IsCompleted = isChecked
        dbHelper.updateTask(task)
        loadTasksFromDatabase() // Reload the list to update sections
    }

    private fun showAddTaskDialog(shouldUpdate: Boolean, task: Task?, position: Int) {
        val addTaskDialogFragment = AddTaskBottomSheetFragment(
            shouldUpdate = shouldUpdate,
            task = task,
            position = position,
            onTaskAdded = { shouldUpdate, updatedTask, position->
                if (shouldUpdate && updatedTask != null) {
                    updateTask(updatedTask)
                } else {
                    updatedTask.Category = category
                    addTask(updatedTask)
                }
            }
        )

        addTaskDialogFragment.show(supportFragmentManager, "AddTaskBottomSheetFragment")
    }

    private fun addTask(task: Task) {
        try {
            task.Id = dbHelper.addTask(task)
            loadTasksFromDatabase()
        } catch (e: Exception){
            Toast.makeText(this@TaskActivity, "Error saving task "+e.localizedMessage, Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateTask(task: Task) {
         try {
             dbHelper.updateTask(task)
             loadTasksFromDatabase()
         } catch (e: Exception){
             Toast.makeText(this@TaskActivity, "Error on updating task", Toast.LENGTH_SHORT).show()
         }
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onItemClick(task: Task, position: Int) {
        showAddTaskDialog(true, task, position)
    }

    override fun onItemDelete(task: Task) {
        deleteConfirmation(task)
    }
}