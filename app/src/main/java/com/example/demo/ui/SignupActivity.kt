package com.example.demo

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.demo.InitializeFirebase.Companion.auth
import com.example.demo.databinding.ActivitySignupBinding

class SignupActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)
        InitializeFirebase.init()
        binding.signUpButton.setOnClickListener {
            val email = binding.emailEditText.text.toString().trim()
            val password = binding.passwordEditText.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                if (isValidEmail(email) && password.length >= 6) { // Basic validation
                    auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener{
                       if(it.isSuccessful){
                           Toast.makeText(this, "Registered Successfully", Toast.LENGTH_SHORT).show()
                           val intent = Intent(this, LoginActivity::class.java)
                           startActivity(intent)
                       }
                    }.addOnFailureListener{
                        Toast.makeText(this, it.localizedMessage, Toast.LENGTH_LONG).show()
                    }
                } else {
                    Toast.makeText(this, "Incorrect Email or Password", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Fill all fields", Toast.LENGTH_SHORT).show()
            }
        }
        binding.loginTextView.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}