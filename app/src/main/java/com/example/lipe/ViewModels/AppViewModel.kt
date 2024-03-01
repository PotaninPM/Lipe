package com.example.lipe.ViewModels

import androidx.lifecycle.ViewModel

class AppViewModel: ViewModel() {

//    var name: Mult = "";
    var reg: String = "no"

    var latitude: Double = 0.0
    var longtitude: Double = 0.0

    fun setCoord(lat: Double, long: Double) {
        latitude = lat
        longtitude = long
    }
}