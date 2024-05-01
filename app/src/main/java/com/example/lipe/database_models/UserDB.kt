package com.example.lipe.database_models

data class UserDB(
    val uid:String?= null,
    var avatarId: String,
    val date_of_reg: String?= null,
    val balance: Int ?= 0,
    val bonus: Int ?= 0,
    val rating: Int ?= 0,
    val about_you: String ?= null,
    val username: String? = null,
    val email: String? = null,
    val phone: String? = null,
    val password: String?= null,
    val firstName: String?= null,
    val lastName: String ?= null,
    val place_in_total_rating: Int?= null,
    val hobbies: ArrayList<String> ?= null,
    val query_friends: ArrayList<String> ?= null,
    val friends: ArrayList<String> ?= null,
    val friends_amount: Int? = 0,
    val curRegEventsId: ArrayList<String>? = null,
    val yourCreatedEvents: ArrayList<String> ?= null,
    val events_amount: Int? = 0,
    val chats: ArrayList<String>? = null,
    val groups: ArrayList<String> ?= null,
    val location: HashMap<String, Double> = hashMapOf("latitude" to 0.0, "longitude" to 0.0),
    val themeUid: String = "default"
)