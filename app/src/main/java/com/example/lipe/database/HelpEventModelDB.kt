package com.example.lipe.database

data class HelpEventModelDB(
    var event_id: Int,
    var creator_id: String,
    var time_of_creation: String,
    var title: String,
    var adress: String,
    var coordinates: String,
    var date_of_meeting: String ?= null,
    var description: String,
    var photo_one_id: String,
    var photo_two_id: String,
    var photo_three_id: String,
    var people_want_id: Array<String>,
    var award: Int
)