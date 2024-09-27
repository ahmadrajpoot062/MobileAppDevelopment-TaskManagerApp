package com.example.demo

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.demo.InitializeFirebase.Companion.auth
import com.example.demo.InitializeFirebase.Companion.isLogin
import com.google.android.material.imageview.ShapeableImageView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class NoteActivity : AppCompatActivity(), NoteAdapter.OnClickListener {

    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyView: TextView
    private lateinit var emptyIcon: ImageView
    private lateinit var searchIcon: ImageView
    private lateinit var countView: TextView
    private lateinit var openDialog: ShapeableImageView
    private lateinit var toolbar: Toolbar
    private lateinit var adapter: NoteAdapter
    private lateinit var dbHelper: NoteDbHelper

    private val notes = ArrayList<Note>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_note)
        InitializeFirebase.init()
        recyclerView = findViewById(R.id.recyclerView)
        searchIcon=findViewById(R.id.search_icon)
        countView=findViewById(R.id.tv_count)
        emptyView = findViewById(R.id.tv_empty_view)
        emptyIcon = findViewById(R.id.iv_empty_icon)
        openDialog = findViewById(R.id.btadd)
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        supportActionBar?.title="Notepad"

        dbHelper = NoteDbHelper(this)

        openDialog.setOnClickListener {
            showAddNoteDialog(false, null, -1)
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = NoteAdapter(notes).apply {
            setListener(this@NoteActivity)
        }
        recyclerView.adapter = adapter

        loadNotes()
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
                Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show()
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

    override fun onResume() {
        super.onResume()
        loadNotes()
    }

    private fun currentTimeStamp(): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy hh:mm:ss a", Locale.getDefault())

        return sdf.format(Date())
    }

    private fun getAllNotes(): List<Note> {
        return dbHelper.allNotes()
    }

    private fun loadNotes() {
        try {
            notes.clear()
            val allNotes = getAllNotes()
            if (allNotes.isEmpty()) {
                showEmptyView("No notes available")
            } else {
                hideEmptyView(allNotes.count())
                notes.addAll(allNotes)
            }
            adapter.notifyDataSetChanged()
        } catch (e: Exception) {
            Toast.makeText(this@NoteActivity, "Error loading notes", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showEmptyView(message: String) {
        val emptyView: TextView = findViewById(R.id.tv_empty_view)
        emptyView.text = message
        emptyView.visibility = View.VISIBLE
        emptyIcon.visibility = View.VISIBLE
        recyclerView.visibility = View.GONE
        searchIcon.visibility= View.GONE
        countView.visibility = View.GONE
    }

    private fun hideEmptyView(count: Int) {
        emptyView.visibility = View.GONE
        emptyIcon.visibility = View.GONE
        recyclerView.visibility = View.VISIBLE
        searchIcon.visibility= View.VISIBLE
        countView.visibility = View.VISIBLE
        countView.text = "My Notes ($count)"
    }

    private fun deleteConfirmation(note: Note) {
        AlertDialog.Builder(this)
            .setTitle("Confirm Delete")
            .setMessage("Are you sure you want to delete ${note.Title}?")
            .setIcon(R.drawable.ic_delete)
            .setPositiveButton("YES") { _, _ ->
                try {
                    dbHelper.deleteNote(note)
                    loadNotes()
                } catch (e: Exception) {
                    Toast.makeText(this@NoteActivity, "Error deleting note", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("NO") { dialog, _ ->
                dialog.cancel()
            }
            .show()
    }

    private fun showAddNoteDialog(shouldUpdate: Boolean, note: Note?, position: Int){
        val mDialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_note, null)
        val mAlertDialog = AlertDialog.Builder(this, R.style.FullScreenDialogTheme)
            .setView(mDialogView)
            .create()

        val tvHeader: TextView = mDialogView.findViewById(R.id.tvHeader)
        val edTitle: EditText = mDialogView.findViewById(R.id.edTitle)
        val edDesc: EditText = mDialogView.findViewById(R.id.edDesc)
        val btSave: AppCompatImageView = mDialogView.findViewById(R.id.btSave)
        val btCancel: ImageView = mDialogView.findViewById(R.id.btCancel)

        if(shouldUpdate && note!= null){
            tvHeader.text = "Edit Note"
            edTitle.setText(note.Title)
            edDesc.setText(note.Description)
        }else{
            tvHeader.text = "New Note"
        }

        btSave.setOnClickListener {
            val title = edTitle.text.toString()
            val desc = edDesc.text.toString()

            if(title.trim().isEmpty()){
                Toast.makeText(this, "Title is required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            } else if(desc.trim().isEmpty()){
                Toast.makeText(this, "Description is required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if(shouldUpdate && note != null) {
                if(note.Title!=title || note.Description!=desc) {
                    updateNote(Note(Id = note.Id, Title= title, Description = desc, Timestamp = currentTimeStamp(), Pinned = note.Pinned))
                }
            } else {
                addNote(Note(-1, Title = title, Description = desc, Timestamp = currentTimeStamp()))
            }
            mAlertDialog.dismiss()
        }
        btCancel.setOnClickListener { mAlertDialog.dismiss() }
        mAlertDialog.show()
    }

    private fun addNote(note: Note) {
        try {
            note.Id = dbHelper.insertNote(note)
            loadNotes()
        } catch (e: Exception) {
            Toast.makeText(this@NoteActivity, "Error saving note", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateNote(note: Note) {
        try {
            dbHelper.updateNote(note)
            loadNotes()
        } catch (e: Exception) {
            Toast.makeText(this@NoteActivity, "Error updating note", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onItemClick(note: Note, position: Int) {
        showAddNoteDialog(true, note, position)
    }

    override fun onItemDelete(note: Note) {
        deleteConfirmation(note)
    }
 }