package com.example.lipe

import retrofit2.Call
import retrofit2.http.POST

interface EventApi {
    @POST("/delete_old_events")
    suspend fun deleteOldEvents(): Call<Void>
}