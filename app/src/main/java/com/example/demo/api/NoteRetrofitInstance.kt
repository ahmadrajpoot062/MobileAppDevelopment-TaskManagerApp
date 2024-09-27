package com.example.demo

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object NoteRetrofitInstance {
    private const val BASE_URL = "http://192.168.10.6:5093"
    val api: NoteApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(NoteApiService::class.java)
    }
}