package com.example.demo

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class NoteDbHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_VERSION = 1
        private const val DATABASE_NAME = "notes_db"
        private const val TABLE_NAME = "noteTable"
        private const val COLUMN_ID = "Id"
        private const val COLUMN_TITLE = "Title"
        private const val COLUMN_DESCRIPTION = "Description"
        private const val COLUMN_TIMESTAMP = "Timestamp"
        private const val COLUMN_PINNED = "Pinned"

        private const val CREATE_TABLE = """
            CREATE TABLE $TABLE_NAME (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_TITLE TEXT NOT NULL,
                $COLUMN_DESCRIPTION TEXT NOT NULL,
                $COLUMN_TIMESTAMP TEXT NOT NULL,
                $COLUMN_PINNED INTEGER DEFAULT 0
            )
        """
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(CREATE_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    fun allNotes(): List<Note> {
        val notes = mutableListOf<Note>()
        val selectQuery = "SELECT * FROM $TABLE_NAME ORDER BY $COLUMN_PINNED DESC, $COLUMN_TIMESTAMP DESC"
        readableDatabase.rawQuery(selectQuery, null).use { cursor ->
            while (cursor.moveToNext()) {
                notes.add(cursor.toNote())
            }
        }
        return notes
    }

    fun insertNote(note: Note): Int {
        val values = ContentValues().apply {
            put(COLUMN_TITLE, note.Title)
            put(COLUMN_DESCRIPTION, note.Description)
            put(COLUMN_TIMESTAMP, note.Timestamp)
            put(COLUMN_PINNED, note.Pinned)
        }
        return writableDatabase.insert(TABLE_NAME, null, values).toInt()
    }

    fun updateNote(note: Note): Int {
        val values = ContentValues().apply {
            put(COLUMN_TITLE, note.Title)
            put(COLUMN_DESCRIPTION, note.Description)
            put(COLUMN_TIMESTAMP, note.Timestamp)
            put(COLUMN_PINNED, note.Pinned)
        }
        return writableDatabase.update(TABLE_NAME, values, "$COLUMN_ID = ?", arrayOf(note.Id.toString()))
    }

    fun deleteAllNotes(): Boolean {
        val result = writableDatabase.delete(TABLE_NAME, null, null)
        return result > 0
    }

    fun deleteNote(note: Note): Boolean {
        val result = writableDatabase.delete(TABLE_NAME, "$COLUMN_ID = ?", arrayOf(note.Id.toString()))
        return result > 0
    }

    private fun Cursor.toNote(): Note {
        return Note(
            Id = getInt(getColumnIndexOrThrow(COLUMN_ID)),
            Title = getString(getColumnIndexOrThrow(COLUMN_TITLE)),
            Description = getString(getColumnIndexOrThrow(COLUMN_DESCRIPTION)),
            Timestamp = getString(getColumnIndexOrThrow(COLUMN_TIMESTAMP)),
            Pinned = getInt(getColumnIndexOrThrow(COLUMN_PINNED)) > 0
        )
    }
}