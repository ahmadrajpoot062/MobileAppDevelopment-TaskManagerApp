package com.example.demo

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface NoteApiService {
    @GET("Note/FetchNotes")
    suspend fun fetchNotes(): List<Note>

    @POST("Note/SaveNotes")
    suspend fun saveNotes(@Body notes: List<Note>): Response
}