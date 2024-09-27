package com.example.demo

data class Note(
    var Id: Int,
    val Title: String,
    var Description: String,
    var Timestamp: String,
    var Pinned: Boolean = false,
)