package com.example.lipe.notifications

import com.google.gson.annotations.SerializedName

data class NewMessageGroup(
    @SerializedName("groupName") var groupName: String,
    @SerializedName("senderUid") var senderUid: String,
    @SerializedName("users") var users: ArrayList<String>,
    @SerializedName("message") var message: String
)
