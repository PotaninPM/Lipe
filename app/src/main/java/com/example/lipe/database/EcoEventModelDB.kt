package com.example.lipe.database

import java.util.ArrayList

data class EcoEventModelDB(
    var event_id: String,
    var type_of_event: String,
    var creator_id: String,
    var time_of_creation: String,
    var title: String,
    var adress: String,
    var coordinates: List<Double>,
    var power_of_pollution: Int,
    var date_of_meeting: String ?= null,
    var min_people: Int,
    var max_people: Int,
    var description: String,
    var photo_before_id: ArrayList<String>,
    var photo_after_id: ArrayList<String>,
    var reg_people_id: ArrayList<String>,
    var amount_reg_people: Int,
    var get_points: Int,
    var status: String
)