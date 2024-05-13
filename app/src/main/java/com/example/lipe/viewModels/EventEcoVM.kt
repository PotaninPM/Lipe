package com.example.lipe.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class EventEcoVM: ViewModel() {

    private var _title = MutableLiveData<String>()
    val title: LiveData<String> = _title

    private var _id = MutableLiveData<String>()
    val id: LiveData<String> = _id

    private var _maxPeople = MutableLiveData<Int>()
    val maxPeople: LiveData<Int> = _maxPeople

    private var _powerPollution = MutableLiveData<String>()
    val powerPollution: LiveData<String> = _powerPollution

    private var _minPeople = MutableLiveData<Int>()
    val minPeople: LiveData<Int> = _minPeople

    private var _creator = MutableLiveData<String>()
    val creator: LiveData<String> = _creator

    private var _photosBefore = MutableLiveData<ArrayList<String>>()
    val photosBefore: LiveData<ArrayList<String>> = _photosBefore

    private var _peopleGo = MutableLiveData<ArrayList<String>>()
    val peopleGo: LiveData<ArrayList<String>> = _peopleGo

    private var _adress = MutableLiveData<String>()
    val adress: LiveData<String> = _adress

    private var _freePlaces = MutableLiveData<Int>()
    var freePlaces: LiveData<Int> = _freePlaces

    private var _eventDesc = MutableLiveData<String>()
    val eventDesc: LiveData<String> = _eventDesc

    private var _date = MutableLiveData<String>()
    val date: LiveData<String> = _date

    private var _type = MutableLiveData<String>()
    val type: LiveData<String> = _type

    private var _time_of_creation = MutableLiveData<String>()
    val time_of_creation: LiveData<String> = _time_of_creation

    private var _amount_reg_people = MutableLiveData<Int>()
    val amount_reg_people: LiveData<Int> = _amount_reg_people

    private var _creatorUsername = MutableLiveData<String>()
    val creatorUsername: LiveData<String> = _creatorUsername

    private var _getPoints = MutableLiveData<Int>()
    val getPoints: LiveData<Int> = _getPoints

    var latitude: Double = 0.0
    var longtitude: Double = 0.0

    fun setCoord(lat: Double, long: Double) {
        latitude = lat
        longtitude = long
    }

    fun setInfo(id_: String, maxPeople_: Int, minPeople_: Int, powerPollution_: String, title_: String, creator_: String, creatorUsername_: String, photosBefore_: ArrayList<String>, peopleGo_: ArrayList<String>, freePlaces_: Int, eventDesc_: String, time_of_creation_: String, date_: String, amount_reg_people_: Int, getPoints_: Int) {
        _id.value = id_
        _maxPeople.value = maxPeople_
        _title.value = title_
        _creator.value = creator_
        _photosBefore.value = photosBefore_
        _peopleGo.value = peopleGo_
        _eventDesc.value = eventDesc_
        _date.value = date_
        _type.value = "Экология"
        _time_of_creation.value = time_of_creation_
        _amount_reg_people.value = amount_reg_people_
        _freePlaces.value = 100 - 100 * amount_reg_people.value!! / maxPeople.value!!
        _powerPollution.value = powerPollution_.toString()
        _minPeople.value = minPeople_
        _getPoints.value = getPoints_

        _creatorUsername.value = creatorUsername_
    }

//    fun setProgress(freePlaces: Int, maxPeople: Int) {
//        var
//    }
}