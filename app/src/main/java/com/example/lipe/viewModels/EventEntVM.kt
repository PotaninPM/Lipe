package com.example.lipe.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.text.SimpleDateFormat
import java.util.Locale

class EventEntVM: ViewModel() {

    private var _title = MutableLiveData<String>()
    val title: LiveData<String> = _title

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

    private var _freePlaces = MutableLiveData<Int>()
    var freePlaces: LiveData<Int> = _freePlaces

    private var _eventDesc = MutableLiveData<String>()
    val eventDesc: LiveData<String> = _eventDesc

    private var _date = MutableLiveData<String>()
    val date: LiveData<String> = _date

    private var _typeSport = MutableLiveData<String>()
    var typeSport: LiveData<String> = _typeSport

    private var _langTypeSport = MutableLiveData<String>()
    var langTypeSport: LiveData<String> = _langTypeSport

    private var _type = MutableLiveData<String>()
    val type: LiveData<String> = _type

    private var _timeOfCreation = MutableLiveData<String>()
    val timeOfCreation: LiveData<String> = _timeOfCreation

    private var _amountRegPeople = MutableLiveData<Int>()
    val amountRegPeople: LiveData<Int> = _amountRegPeople

    private var _creatorUsername = MutableLiveData<String>()
    val creatorUsername: LiveData<String> = _creatorUsername

    private var _age = MutableLiveData<String>()
    val age: LiveData<String> = _age

    private var _friend = MutableLiveData<String>()
    val friend: LiveData<String> = _friend

    var latitude: Double = 0.0
    var longtitude: Double = 0.0

    fun setCoord(lat: Double, long: Double) {
        latitude = lat
        longtitude = long
    }

    fun setInfo(id_: String?, maxPeople_: Int, title_: String, creator_: String, creatorUsername_: String, photos_: ArrayList<String>, peopleGo_: List<String>, freePlaces_: Int, age_: String, eventDesc_: String, time_of_creation_: String, date_: String, type_sport_: String, lang_type_sport_: String, amount_reg_people_: Int, friend_: String) {
        _id.value = id_
        _maxPeople.value = maxPeople_
        _title.value = title_
        _creator.value = creator_
        _photos.value = photos_
        _peopleGo.value = peopleGo_
        _eventDesc.value = eventDesc_
        _date.value = date_
        _typeSport.value = type_sport_
        _timeOfCreation.value = time_of_creation_
        _amountRegPeople.value = amount_reg_people_
        _freePlaces.value = 100 - 100 * amountRegPeople.value!! / maxPeople.value!!

        _langTypeSport.value = lang_type_sport_

        _timeOfCreation.value = formatDate(time_of_creation_)

        _friend.value = friend_

        _age.value = age_
        _creatorUsername.value = creatorUsername_

        setDate(date_)
    }

    fun formatDate(dateString: String): String {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        val outputFormat = SimpleDateFormat("Создано d MMMM yyyy года HH:mm", Locale("ru"))
        val date = inputFormat.parse(dateString)
        return outputFormat.format(date)
    }

    private var _dateRussianMonthDayYear = MutableLiveData<String>()
    val dateRussianMonthDayYear: LiveData<String> = _dateRussianMonthDayYear

    fun setDate(date: String) {
        _dateRussianMonthDayYear.value = date
    }

//    fun setProgress(freePlaces: Int, maxPeople: Int) {
//        var
//    }
}