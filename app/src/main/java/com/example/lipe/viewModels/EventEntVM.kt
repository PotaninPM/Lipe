package com.example.lipe.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class EventEntVM: ViewModel() {

    private val _quantity = MutableLiveData<String>()
    val quantity: LiveData<String> = _quantity

    var id: Int = -1
    var maxPeople: Int = 0
    var title: String = ""
    var creator: String = ""
    var photos = listOf("")
    var peopleGo =listOf("")
    var adress:String = ""
    var freePlaces: Int = 0
    var eventDesc: String = ""
    var date = ""
    var type_sport = "null"
    var type = ""
    var time_of_creation = ""
    var amount_reg_people: Int = 0

    fun setInfo(id_: Int, maxPeople_: Int, title_: String, creator_: String, photos_: List<String>, peopleGo_: List<String>, adress_: String, freePlaces_: Int, eventDesc_: String, time_of_creation_: String, date_: String, type_sport_: String, amount_reg_people_: Int) {
        id = id_
        maxPeople = maxPeople_
        title = title_
        creator = creator_
        photos = photos_
        peopleGo = peopleGo_
        adress = adress_
        freePlaces = freePlaces_
        eventDesc = eventDesc_
        date = date_
        type_sport = type_sport_
        type = "Развлечение"
        time_of_creation = time_of_creation_
        amount_reg_people = amount_reg_people_
    }

//    fun setProgress(freePlaces: Int, maxPeople: Int) {
//        var
//    }
}