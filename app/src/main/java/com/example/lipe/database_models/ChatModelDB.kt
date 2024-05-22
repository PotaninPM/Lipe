package com.example.lipe.database_models

data class ChatModelDB(val user1_uid: String, val user2_uid: String, var last_message: String ?= null,val messages: ArrayList<String> ?= null)
