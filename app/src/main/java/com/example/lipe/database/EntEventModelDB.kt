package com.example.lipe.database

data class EntEventModelDB(
    var event_id: Long,
    var type_of_event: String,
    var creator_id: String,
    var time_of_creation: String,
    var sport_type: String,
    var title: String,
    var adress: String,
    var coordinates: List<Double>,
    var date_of_meeting: String,
    var max_people: Int,
    var description: String,
    var photos: ArrayList<String>,
    var reg_people_id: List<String?>,
    var amount_reg_people: Int
)