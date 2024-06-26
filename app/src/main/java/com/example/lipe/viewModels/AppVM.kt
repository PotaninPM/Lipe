package com.example.lipe.viewModels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.lipe.sign_up_in.SignUpDescFragment

class AppVM: ViewModel() {

    var latitude: Double = 0.0
    var longtitude: Double = 0.0

    var event: String = ""

    var type: String = ""

    var type_sport: String = ""

    var positionCreateFr = 0

    var markersType: String = ""

    fun setCoord(lat: Double, long: Double) {
        latitude = lat
        longtitude = long
    }
}