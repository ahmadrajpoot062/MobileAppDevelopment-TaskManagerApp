package com.example.demo

data class Task(
    var Id: Int,
    val Name: String,
    var Category: String,
    var DueDate: String? = null,
    var IsCompleted: Boolean = false,
)