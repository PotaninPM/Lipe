package com.example.lipe.viewModels

import androidx.lifecycle.ViewModel

class AppVM: ViewModel() {

    var reg: String = "no"

    var latitude: Double = 0.0
    var longtitude: Double = 0.0

    var event: String = ""

    var type: String = ""
    var type_sport: String = ""

    var positionCreateFr = 0

    var qrData: String = "jj"

    var markersType: String = ""
    fun setCoord(lat: Double, long: Double) {
        latitude = lat
        longtitude = long
    }

    fun openFr(positionCreateFr_: Int) {
        positionCreateFr = positionCreateFr_
    }
}