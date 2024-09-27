package com.example.demo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class AddTaskBottomSheetFragment(
    private val shouldUpdate: Boolean,
    private val task: Task?,
    private val position: Int,
    private val onTaskAdded: (shouldUpdate: Boolean, task: Task, position: Int) -> Unit
) : BottomSheetDialogFragment() {

    private lateinit var tvTitle: TextView
    private lateinit var etTaskName: EditText
    private lateinit var btnSetAlerts: Button
    private lateinit var btnSave: Button
    private lateinit var cancelButton: ImageView
    private var selectedDateTime: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_add_task_bottom_sheet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tvTitle = view.findViewById(R.id.tv_add_task_title)
        etTaskName = view.findViewById(R.id.et_task_name)
        btnSetAlerts = view.findViewById(R.id.btn_set_alerts)
        btnSave = view.findViewById(R.id.btn_save)
        cancelButton = view.findViewById(R.id.iv_cancel)

        if (shouldUpdate && task != null) {
            tvTitle.text = "Edit To-do"
            etTaskName.setText(task.Name)
            btnSetAlerts.text = task.DueDate ?: "Set Alerts"
            btnSave.text = "Update"
        } else {
            tvTitle.text = "New To-do"
            btnSetAlerts.text = "Set Alerts"
            btnSave.text = "Save"
        }

        btnSetAlerts.setOnClickListener {
            showDateTimePicker()
        }

        btnSave.setOnClickListener {
            val taskName = etTaskName.text.toString()
            if (taskName.trim().isNotEmpty()) {
                val dueDate = btnSetAlerts.text.toString().takeIf { it != "Set Alerts" }
                val taskToSave = Task(
                    Id = task?.Id ?: -1,
                    Name = taskName,
                    IsCompleted = task?.IsCompleted ?: false,
                    Category = task?.Category ?: "",
                    DueDate = dueDate
                )

                onTaskAdded(shouldUpdate, taskToSave, position)
                dismiss()
            } else {
                etTaskName.error = "Task Name cannot be empty"
            }
        }

        cancelButton.setOnClickListener {
            dismiss()
        }
    }

    private fun showDateTimePicker() {
        val dateTimePicker = DateTimePickerDialogFragment { selectedDateTimeString ->
            selectedDateTime = selectedDateTimeString
            btnSetAlerts.text = selectedDateTime
        }
        dateTimePicker.show(childFragmentManager, "dateTimePicker")
    }
}