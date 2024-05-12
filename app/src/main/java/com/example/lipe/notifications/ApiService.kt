package com.example.lipe.notifications

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {

    @POST("/events")
    fun sendEventData(@Body eventData: EventData): Call<Void>

    @POST
    fun sendFriendsRequestData(@Body friendRequestData: FriendRequestData)

    @POST
    fun acceptFriendsRequestData(@Body acceptFriendRequest: AcceptFriendRequest)
}