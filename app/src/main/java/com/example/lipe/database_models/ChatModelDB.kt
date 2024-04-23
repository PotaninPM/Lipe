package com.example.lipe.database_models

data class ChatModelDB(val user1_uid: String, val user2_uid: String, val last_message: String,val messages: ArrayList<String>)
