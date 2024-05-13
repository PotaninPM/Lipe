package com.example.lipe.notifications

import com.google.gson.annotations.SerializedName
data class EventData(
    @SerializedName("latitude") val latitude: String,
    @SerializedName("longitude") val longitude: String,
    @SerializedName("creatorUid") val creatorUid: String
)
