package com.example.lipe.viewModels

import androidx.lifecycle.ViewModel

class AppViewModel: ViewModel() {

    var reg: String = "no"

    var latitude: Double = 0.0
    var longtitude: Double = 0.0

    var event: String = ""

    fun setCoord(lat: Double, long: Double) {
        latitude = lat
        longtitude = long
    }
}