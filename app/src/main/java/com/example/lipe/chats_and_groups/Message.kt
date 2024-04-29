package com.example.lipe.chats_and_groups

data class Message(
    val text: String = "",
    val senderId: String = "",
    val time: Long = 0
)
