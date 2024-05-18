package com.example.lipe.notifications

import com.google.gson.annotations.SerializedName
data class EntEventData(
    @SerializedName("event_id") var event_id: String,
    @SerializedName("type_of_event") var type_of_event: String,
    @SerializedName("creator_id") var creator_id: String,
    @SerializedName("time_of_creation") var time_of_creation: String,
    @SerializedName("sport_type") var sport_type: String,
    @SerializedName("title") var title: String,
    @SerializedName("coordinates") var coordinates: HashMap<String, Double>,
    @SerializedName("date_of_meeting") var date_of_meeting: String,
    @SerializedName("max_people") var max_people: Int,
    @SerializedName("age") var age: String,
    @SerializedName("description") var description: String,
    @SerializedName("photos") var photos: String,
    @SerializedName("reg_people_id") var reg_people_id: HashMap<String?, String?>,
    @SerializedName("amount_reg_people") var amount_reg_people: Int,
    @SerializedName("status") var status: String,
    @SerializedName("timestamp") var timestamp: Long
)
