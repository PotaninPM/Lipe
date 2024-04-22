package com.example.lipe.database_models

import java.util.ArrayList

data class GroupModel(
    val uid: String,
    val members: ArrayList<String>,
    val messages: ArrayList<String>
)
