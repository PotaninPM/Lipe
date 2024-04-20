package com.example.lipe.database_models

data class EntEventModelDB(
    var event_id: String,
    var type_of_event: String,
    var creator_id: String,
    var time_of_creation: String,
    var sport_type: String,
    var title: String,
    var adress: String,
    var coordinates: List<Double>,
    var date_of_meeting: String,
    var max_people: Int,
    var age: String,
    var description: String,
    var photos: ArrayList<String>,
    var reg_people_id: ArrayList<String?>,
    var amount_reg_people: Int,
    var status: String
)