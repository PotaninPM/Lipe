package com.example.lipe.viewModels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ProfileVM: ViewModel() {
    private var _nameLastName = MutableLiveData<String>()
    val nameLastName: LiveData<String> = _nameLastName

    private var _id = MutableLiveData<String>()
    val id: LiveData<String> = _id

    private var _maxPeople = MutableLiveData<Int>()
    val maxPeople: LiveData<Int> = _maxPeople

    private var _creator = MutableLiveData<String>()
    val creator: LiveData<String> = _creator

    private var _photos = MutableLiveData<ArrayList<String>>()
    val photos: LiveData<ArrayList<String>> = _photos

    private var _peopleGo = MutableLiveData<List<String>>()
    val peopleGo: LiveData<List<String>> = _peopleGo

    private var _adress = MutableLiveData<String>()
    val adress: LiveData<String> = _adress

    private var _freePlaces = MutableLiveData<Int>()
    var freePlaces: LiveData<Int> = _freePlaces

    private var _eventDesc = MutableLiveData<String>()
    val eventDesc: LiveData<String> = _eventDesc

    private var _date = MutableLiveData<String>()
    val date: LiveData<String> = _date

    private var _type_sport = MutableLiveData<String>()
    val type_sport: LiveData<String> = _type_sport

    private var _type = MutableLiveData<String>()
    val type: LiveData<String> = _type

    private var _time_of_creation = MutableLiveData<String>()
    val time_of_creation: LiveData<String> = _time_of_creation

    private var _amount_reg_people = MutableLiveData<Int>()
    val amount_reg_people: LiveData<Int> = _amount_reg_people

    private var _creatorUsername = MutableLiveData<String>()
    val creatorUsername: LiveData<String> = _creatorUsername

    private var _age = MutableLiveData<String>()
    val age: LiveData<String> = _age

    var latitude: Double = 0.0
    var longtitude: Double = 0.0

    fun setCoord(lat: Double, long: Double) {
        latitude = lat
        longtitude = long
    }

    fun setInfo(id_: String, maxPeople_: Int, title_: String, creator_: String, creatorUsername_: String, photos_: ArrayList<String>, peopleGo_: List<String>, adress_: String, freePlaces_: Int, age_: String, eventDesc_: String, time_of_creation_: String, date_: String, type_sport_: String, amount_reg_people_: Int) {
//        _id.value = id_
//        _maxPeople.value = maxPeople_
//        _title.value = title_
//        _creator.value = creator_
//        _photos.value = photos_
//        _peopleGo.value = peopleGo_
//        _adress.value = adress_
//        _eventDesc.value = eventDesc_
//        _date.value = date_
//        _type_sport.value = type_sport_
//        _type.value = "Развлечение"
//        _time_of_creation.value = time_of_creation_
//        _amount_reg_people.value = amount_reg_people_
//        _freePlaces.value = 100 - 100 * amount_reg_people.value!! / maxPeople.value!!
//
//        _age.value = age_
//        _creatorUsername.value = creatorUsername_
    }
}