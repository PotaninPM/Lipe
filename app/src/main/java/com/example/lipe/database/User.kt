package com.example.lipe.database

import android.content.Intent

data class User(
    val uid:String?= null,
    val date_of_reg: String?= null,
    val balance: Int ?= 0,
    val bonus: Int ?= 0,
    val rating: Int ?= 0,
    val about_you: String ?= null,
    val username: String? = null,
    val email: String? = null,
    val phone: String? = null,
    val password: String?= null,
    val firstName: String?= null,
    val lastName: String ?= null,
    val place_in_total_rating: Int?= null,
    var organization: String ?= null,
    val hobbies: Array<String> ?= null,
    val query_friends: Array<String> ?= null,
    val friends: Array<String> ?= null,
    val friends_amount: Int ?= null
)