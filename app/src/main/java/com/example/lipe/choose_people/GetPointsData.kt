package com.example.lipe.choose_people

import com.google.gson.annotations.SerializedName

data class GetPointsData(
    @SerializedName("people") var people: List<String>,
    @SerializedName("points") var points: Int
)
