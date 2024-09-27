package com.example.demo

import com.google.firebase.auth.FirebaseAuth
import kotlin.properties.Delegates

class InitializeFirebase {
    companion object{
        lateinit var auth: FirebaseAuth
        var isLogin by Delegates.notNull<Boolean>()
        fun init(){
            auth = FirebaseAuth.getInstance()
            isLogin = auth.currentUser != null
        }
    }
}