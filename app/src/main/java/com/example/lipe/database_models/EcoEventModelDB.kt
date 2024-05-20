package com.example.lipe.database_models

import com.google.gson.annotations.SerializedName
import java.util.ArrayList

data class EcoEventModelDB(
    @SerializedName("event_id") var event_id: String,
    @SerializedName("type_of_event") var type_of_event: String,
    @SerializedName("creator_id") var creator_id: String,
    @SerializedName("time_of_creation") var time_of_creation: String,
    @SerializedName("title") var title: String,
    @SerializedName("coordinates") var coordinates: HashMap<String, Double>,
    @SerializedName("power_of_pollution") var power_of_pollution: String,
    @SerializedName("date_of_meeting") var date_of_meeting: String,
    @SerializedName("min_people") var min_people: Int,
    @SerializedName("max_people") var max_people: Int,
    @SerializedName("description") var description: String,
    @SerializedName("photos") var photos: String,
    @SerializedName("reg_people_id") var reg_people_id: HashMap<String?, String?>,
    @SerializedName("amount_reg_people") var amount_reg_people: Int,
    @SerializedName("get_points") var get_points: Int,
    @SerializedName("status") var status: String,
    @SerializedName("timestamp") var timestamp: Long
)