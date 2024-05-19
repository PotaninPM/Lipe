package com.example.lipe.notifications

import com.google.gson.annotations.SerializedName

data class FriendRequestData(
    @SerializedName("receiverUid") var receiverUid: String,
    @SerializedName("senderUid") var senderUid: String
)
