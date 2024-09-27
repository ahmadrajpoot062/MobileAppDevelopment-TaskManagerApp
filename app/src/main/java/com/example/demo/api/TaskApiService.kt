package com.example.demo

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface TaskApiService {
    @GET("Task/FetchTasks")
    suspend fun fetchTasks(): List<Task>

    @POST("Task/SaveTasks")
    suspend fun saveTasks(@Body tasks: List<Task>): Response
}