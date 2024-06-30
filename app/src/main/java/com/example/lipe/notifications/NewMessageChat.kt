package com.example.lipe.notifications

import com.google.gson.annotations.SerializedName

data class NewMessageChat(
    @SerializedName("senderUid") var senderUid: String,
    @SerializedName("receiverUid") var receiverUid: String,
    @SerializedName("message") var message: String
)

