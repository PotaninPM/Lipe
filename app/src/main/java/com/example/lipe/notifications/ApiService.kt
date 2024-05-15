package com.example.lipe.notifications

import com.example.lipe.database_models.EntEventModelDB
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {

    @POST("/create_ent_event")
    fun sendEventData(@Body entEventData: EntEventData): Call<Void>

    @POST
    fun sendFriendsRequestData(@Body friendRequestData: FriendRequestData)

    @POST
    fun acceptFriendsRequestData(@Body acceptFriendRequest: AcceptFriendRequest)
}