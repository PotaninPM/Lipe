package com.example.lipe.DB

data class EcoEventModelDB(
    var event_id: Int,
    var creator_id: String,
    var time_of_creation: String,
    var title: String,
    var adress: String,
    var coordinates: String,
    var power_of_pollution: Int,
    var date_of_meeting: String ?= null,
    var min_people: Int,
    var max_people: Int,
    var description: String,
    var photo_before_one_id: String,
    var photo_before_two_id: String,
    var photo_before_three_id: String,
    var photo_after_one_id: String,
    var photo_after_two_id: String,
    var photo_after_three_id: String,
    var reg_people_id: Array<String>,
    var get_points: Int
)