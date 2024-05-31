package com.example.lipe.database_models

data class HelpEventModelDB(
    var event_id: String,
    var creator_id: String,
    var type_of_event: String,
    var time_of_creation: String,
    var price: Int,
    var peopleNeed: Int,
    var coordinates: HashMap<String, Double>,
    var date_of_meeting: String ?= null,
    var description: String,
    var photos: ArrayList<String>,
    var people_want_id: ArrayList<String>? = null,
)