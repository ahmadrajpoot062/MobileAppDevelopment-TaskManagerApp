package com.example.demo

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class NoteAdapter(private var notes: ArrayList<Note>) : RecyclerView.Adapter<NoteAdapter.NoteViewHolder>() {

    private var listener: OnClickListener? = null

    fun setListener(clickListener: OnClickListener) {
        this.listener = clickListener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item_note, parent, false)
        return NoteViewHolder(view)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        val note = notes[position]
        holder.bindItems(note)

        holder.itemView.setOnClickListener {
            listener?.onItemClick(note, position)
        }

        holder.itemView.setOnLongClickListener {
            showPinUnpinDeleteDialog(holder.itemView.context, note)
            true
        }

        if (note.Pinned) {
            holder.pinIcon.visibility = View.VISIBLE
        } else {
            holder.pinIcon.visibility = View.GONE
        }
    }

    private fun showPinUnpinDeleteDialog(context: android.content.Context, note: Note) {
        val options = mutableListOf<String>()
        if (note.Pinned) options.add("Unpin") else options.add("Pin")
        options.add("Delete")

        val builder = AlertDialog.Builder(context)
        builder.setTitle("Select Option")
        builder.setItems(options.toTypedArray()) { dialog, which ->
            when (options[which]) {
                "Pin" -> {
                    note.Pinned = true
                    note.Timestamp = currentTimeStamp()
                    notifyPinStatusChanged(note, context)
                }
                "Unpin" -> {
                    note.Pinned = false
                    note.Timestamp = currentTimeStamp()
                    notifyPinStatusChanged(note, context)
                }
                "Delete" -> {
                    listener?.onItemDelete(note)
                }
            }
            dialog.dismiss()
        }
        builder.show()
    }

    private fun notifyPinStatusChanged(note: Note, context: Context) {
        val db = NoteDbHelper(context)
        db.updateNote(note)
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = notes.size

    class NoteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        val tvDesc: TextView = itemView.findViewById(R.id.tvDesc)
        val tvTimestamp: TextView = itemView.findViewById(R.id.tvTimestamp)
        val pinIcon: ImageView = itemView.findViewById(R.id.ivPin)

        fun bindItems(note: Note) {
            tvTitle.text = note.Title
            tvDesc.text = note.Description
            tvTimestamp.text = note.Timestamp
        }
    }

    private fun currentTimeStamp(): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy hh:mm:ss a", Locale.getDefault())
        return sdf.format(Date())
    }

    interface OnClickListener {
        fun onItemClick(note: Note, position: Int)
        fun onItemDelete(note: Note)
    }
}