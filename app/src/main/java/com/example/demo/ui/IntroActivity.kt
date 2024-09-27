package com.example.demo

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class IntroActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_intro)

        findViewById<Button>(R.id.btn_start).setOnClickListener{
            startActivity(Intent(this, TaskListActivity::class.java))
            finish()
        }
    }
}