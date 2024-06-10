package com.example.lipe.database_models

data class UserDB(
    val uid:String?= null,
    val date_of_reg: String?= null,
    val balance: Int ?= 0,
    val bonus: Int ?= 0,
    val points: Int ?= 0,
    val about_you: String ?= null,
    val username: String? = null,
    val email: String? = null,
    val password: String?= null,
    val firstAndLastName: String?= null,
    val hobbies: ArrayList<String> ?= null,
    val query_friends: ArrayList<String> ?= null,
    val friends: ArrayList<String> ?= null,
    val friends_amount: Int? = 0,
    val curRegEventsId: ArrayList<String>? = null,
    val yourCreatedEvents: ArrayList<String> ?= null,
    val events_amount: Int? = 0,
    val place_in_rating: Long ?= null,
    val chats: ArrayList<String>? = null,
    val groups: ArrayList<String> ?= null,
    val status: String = "online",
    val userToken: String ?= null,
    val role: String ?= null
)