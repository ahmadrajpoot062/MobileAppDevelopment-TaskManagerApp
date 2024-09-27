package com.example.demo

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.cardview.widget.CardView
import com.example.demo.InitializeFirebase.Companion.auth
import com.example.demo.InitializeFirebase.Companion.isLogin

class TaskListActivity : AppCompatActivity() {

    private lateinit var toolbar: Toolbar
    private lateinit var personal: CardView
    private lateinit var work: CardView
    private lateinit var grocery: CardView
    private lateinit var school: CardView
    private lateinit var general: CardView
    private lateinit var personalCount: TextView
    private lateinit var workCount: TextView
    private lateinit var groceryCount: TextView
    private lateinit var schoolCount: TextView
    private lateinit var generalCount: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task_list)

        InitializeFirebase.init()

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Todo List"

        personal = findViewById(R.id.personal_card)
        work = findViewById(R.id.work_card)
        grocery = findViewById(R.id.grocery_card)
        school = findViewById(R.id.school_card)
        general = findViewById(R.id.general_card)
        personalCount = findViewById(R.id.personal_count)
        workCount = findViewById(R.id.work_count)
        groceryCount = findViewById(R.id.grocery_count)
        schoolCount = findViewById(R.id.school_count)
        generalCount = findViewById(R.id.general_count)

        updateCount()

        personal.setOnClickListener {
            startTaskActivity("Personal")
        }

        work.setOnClickListener {
            startTaskActivity("Work")
        }

        grocery.setOnClickListener {
            startTaskActivity("Grocery")
        }

        school.setOnClickListener {
            startTaskActivity("School")
        }

        general.setOnClickListener {
            startTaskActivity("General")
        }

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
                updateCount()
                true
            }
            R.id.action_logout -> {
                auth.signOut()
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

    fun startTaskActivity(category: String) {
        val intent = Intent(this, TaskActivity::class.java)
        intent.putExtra("category", category)
        startActivity(intent)
    }

    fun getCount(category: String): Int {
        val db=TaskDbHelper(this)
        val count=db.getAllTasks().count { it.Category == category }
        return count
    }

    fun updateCount() {
        personalCount.text=getCount("Personal").toString()
        workCount.text=getCount("Work").toString()
        groceryCount.text=getCount("Grocery").toString()
        schoolCount.text=getCount("School").toString()
        generalCount.text=getCount("General").toString()
    }
}