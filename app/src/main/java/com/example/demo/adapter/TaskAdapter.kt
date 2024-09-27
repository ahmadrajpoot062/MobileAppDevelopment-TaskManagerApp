package com.example.demo

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView

class TaskAdapter(
    private var tasks: ArrayList<Any>,
    private val onTaskChecked: (Task, Boolean) -> Unit,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val VIEW_TYPE_TASK = 0
    private val VIEW_TYPE_HEADER = 1
    private var listener: OnClickListener? = null

    fun setListener(clickListener: OnClickListener) {
        this.listener = clickListener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_TASK) {
            val itemView = LayoutInflater.from(parent.context).inflate(R.layout.list_item_task, parent, false)
            TaskViewHolder(itemView)
        } else {
            val itemView = LayoutInflater.from(parent.context).inflate(R.layout.list_item_task_header, parent, false)
            HeaderViewHolder(itemView)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val task = tasks[position]

        if (task is Task) {
            if (!task.IsCompleted) {
                (holder as TaskViewHolder).bind(task, position)
            }

            holder.itemView.setOnLongClickListener {
                showTaskOptionsDialog(holder.itemView.context, task, position)
                true
            }
        } else {
            val header = task as String
            (holder as HeaderViewHolder).bind(header)
        }
    }

    override fun getItemCount(): Int {
        return tasks.count { it !is Task || !it.IsCompleted }
    }

    private fun showTaskOptionsDialog(context: Context, task: Task, position: Int) {
        val options = mutableListOf<String>()
        if (task.IsCompleted) options.add("Mark as Incomplete") else options.add("Mark as Complete")
        options.add("Delete")

        val builder = AlertDialog.Builder(context)
        builder.setTitle("Select Option")
        builder.setItems(options.toTypedArray()) { dialog, which ->
            when (options[which]) {
                "Mark as Complete" -> {
                    task.IsCompleted = true
                    onTaskChecked(task, true)
                }
                "Mark as Incomplete" -> {
                    task.IsCompleted = false
                    onTaskChecked(task, false)
                }
                "Delete" -> {
                    listener?.onItemDelete(task)
                }
            }
            dialog.dismiss()
        }
        builder.show()
    }

    inner class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTaskName: TextView = itemView.findViewById(R.id.tv_task_name)
        private val cbTaskDone: CheckBox = itemView.findViewById(R.id.cb_task_done)
        private val tvDueDate: TextView = itemView.findViewById(R.id.tv_due_date)

        fun bind(task: Task, position: Int) {
            tvTaskName.text = task.Name
            tvDueDate.text = task.DueDate
            cbTaskDone.setOnCheckedChangeListener(null)
            cbTaskDone.isChecked = task.IsCompleted

            tvTaskName.setOnClickListener(){
                listener?.onItemClick(task, position)
            }

            tvDueDate.setOnClickListener(){
                listener?.onItemClick(task, position)
            }

            cbTaskDone.setOnCheckedChangeListener { _, isChecked ->
                onTaskChecked(task, isChecked)
            }
        }
    }

    inner class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvHeader: TextView = itemView.findViewById(R.id.tv_header)

        fun bind(header: String) {
            tvHeader.text = header
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (tasks[position] is Task) VIEW_TYPE_TASK else VIEW_TYPE_HEADER
    }

    interface OnClickListener {
        fun onItemClick(task: Task, position: Int)
        fun onItemDelete(task: Task)
    }
}