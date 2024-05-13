package com.example.lipe.viewModels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.lipe.sign_up_in.SignUpDescFragment

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

    var sender: String = ""

    var chat: String = ""

    val selectedItems: MutableLiveData<MutableList<SignUpDescFragment.SpinnerItem>> by lazy {
        MutableLiveData<MutableList<SignUpDescFragment.SpinnerItem>>(mutableListOf())
    }

    fun addItem(item: SignUpDescFragment.SpinnerItem) {
        val items = selectedItems.value ?: mutableListOf()
        items.add(item)
        selectedItems.value = items
    }

    fun removeItem(item: SignUpDescFragment.SpinnerItem) {
        val items = selectedItems.value ?: return
        items.remove(item)
        selectedItems.value = items
    }

    fun setCoord(lat: Double, long: Double) {
        latitude = lat
        longtitude = long
    }

    fun openFr(positionCreateFr_: Int) {
        positionCreateFr = positionCreateFr_
    }
}