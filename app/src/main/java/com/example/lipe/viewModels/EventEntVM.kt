package com.example.lipe.viewModels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

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

    private var _adress = MutableLiveData<String>()
    val adress: LiveData<String> = _adress

    private var _freePlaces = MutableLiveData<Int>()
    var freePlaces: LiveData<Int> = _freePlaces

    private var _eventDesc = MutableLiveData<String>()
    val eventDesc: LiveData<String> = _eventDesc

    private var _date = MutableLiveData<String>()
    val date: LiveData<String> = _date

    private var _type_sport = MutableLiveData<String>()
    var type_sport: LiveData<String> = _type_sport

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
        _id.value = id_
        _maxPeople.value = maxPeople_
        _title.value = title_
        _creator.value = creator_
        _photos.value = photos_
        _peopleGo.value = peopleGo_
        _adress.value = adress_
        _eventDesc.value = eventDesc_
        _date.value = date_
        _type_sport.value = type_sport_
        _type.value = "Развлечение"
        _time_of_creation.value = time_of_creation_
        _amount_reg_people.value = amount_reg_people_
        _freePlaces.value = 100 - 100 * amount_reg_people.value!! / maxPeople.value!!

        _age.value = age_
        _creatorUsername.value = creatorUsername_

        Log.d("INFOG1", date_)
        setDate(date_)
    }

    private var _dateRussianMonthDayYear = MutableLiveData<String>()
    val dateRussianMonthDayYear: LiveData<String> = _dateRussianMonthDayYear

    fun setDate(date: String) {

        val months = arrayListOf("Январь", "Февраль", "Март", "Апрель", "Май", "Июнь", "Июль", "Август", "Сентябрь", "Октябрь", "Ноябрь", "Декабрь")

//        var year:String = date[0].toString() + date[1].toString() + date[2].toString() + date[3].toString()
//        var month: String = date[5].toString() + date[6].toString()
//        var day: String = date[8].toString() + date[9].toString()
        //var time:String = date[11].toString() + date[12].toString() + ":" + date[14].toString() + date[15].toString()

//        _dateRussianMonthDayYear.value = day + " " + months[month.toInt() - 1] + ", " + year
        _dateRussianMonthDayYear.value = "11"
        //Log.d("INFOG", dateRussianMonthDayYear.value.toString())


    }

//    fun setProgress(freePlaces: Int, maxPeople: Int) {
//        var
//    }
}