package com.example.lipe.DB

data class EntEventModelDB(
    var event_id: Long,
    var creator_id: String,
    var time_of_creation: String,
    var type_of_event: String,
    var title: String,
    var adress: String,
    var coordinates: List<Double>,
    var date_of_meeting: String,
    var max_people: Int,
    var description: String,
    var photo_one_id: String,
    var photo_two_id: String,
    var photo_three_id: String,
    var reg_people_id: List<String?>
)