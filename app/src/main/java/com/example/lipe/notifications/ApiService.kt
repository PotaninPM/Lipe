package com.example.lipe.notifications

import com.example.lipe.choose_people.GetPointsData
import com.example.lipe.database_models.EcoEventModelDB
import com.example.lipe.database_models.EntEventModelDB
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {

    @POST("/new_message_chat")
    fun newMessageChat(@Body newMessageChat: NewMessageChat): Call<Void>

    @POST("/new_message_group")
    fun newMessageGroup(@Body newMessageGroup: NewMessageGroup): Call<Void>

    @POST("/create_event")
    fun sendEventData(@Body entEventData: EventData): Call<Void>

    @POST("/get_points")
    fun getPointsData(@Body points: GetPointsData): Call<Void>

    @POST("/query_to_friend")
    fun sendFriendsRequestData(@Body friendRequestData: FriendRequestData): Call<Void>

    @POST("/accept_query_to_friend")
    fun acceptFriendsRequestData(@Body acceptFriendRequest: FriendRequestData) : Call<Void>
}