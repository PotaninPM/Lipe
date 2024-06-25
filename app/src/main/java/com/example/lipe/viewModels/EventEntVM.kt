package com.example.lipe.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class EventEntVM : ViewModel() {

    private var _title = MutableLiveData<String>()
    val title: LiveData<String> = _title

    private var _id = MutableLiveData<String?>()
    val id: LiveData<String?> = _id

    private var _maxPeople = MutableLiveData<Int>()
    val maxPeople: LiveData<Int> = _maxPeople

    private var _creator = MutableLiveData<String>()
    val creator: LiveData<String> = _creator

    private var _photos = MutableLiveData<ArrayList<String>>()
    val photos: LiveData<ArrayList<String>> = _photos

    private var _peopleGo = MutableLiveData<List<String>>()
    val peopleGo: LiveData<List<String>> = _peopleGo

    private var _freePlaces = MutableLiveData<Int>()
    val freePlaces: LiveData<Int> = _freePlaces

    private var _eventDesc = MutableLiveData<String>()
    val eventDesc: LiveData<String> = _eventDesc

    private var _date = MutableLiveData<String>()
    val date: LiveData<String> = _date

    private var _typeSport = MutableLiveData<String>()
    val typeSport: LiveData<String> = _typeSport

    private var _langTypeSport = MutableLiveData<String>()
    val langTypeSport: LiveData<String> = _langTypeSport

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

    fun setInfo(id_: String?, maxPeople_: Int, title_: String, creator_: String, creatorUsername_: String, photos_: ArrayList<String>, peopleGo_: List<String>, freePlaces_: Int, age_: String, eventDesc_: String, time_of_creation_: Long, date_: String, type_sport_: String, lang_type_sport_: String, amount_reg_people_: Int, friend_: String) {
        _id.value = id_
        _maxPeople.value = maxPeople_
        _title.value = title_
        _creator.value = creator_
        _photos.value = photos_
        _peopleGo.value = peopleGo_
        _eventDesc.value = eventDesc_
        _typeSport.value = type_sport_
        _amountRegPeople.value = amount_reg_people_
        _freePlaces.value = 100 - 100 * amountRegPeople.value!! / maxPeople.value!!

        _langTypeSport.value = lang_type_sport_

        _timeOfCreation.value = formatTimestamp(time_of_creation_)
        _date.value = formatTimestamp(date_.toLong())

        _friend.value = friend_

        _age.value = age_
        _creatorUsername.value = creatorUsername_
    }

    private fun getDateFormat(): SimpleDateFormat {
        val locale = Locale.getDefault()
        return if (locale.language == "ru") {
            SimpleDateFormat("dd MMMM yyyy 'года' 'в' HH:mm", locale)
        } else {
            SimpleDateFormat("MMMM dd, yyyy 'at' HH:mm", locale)
        }
    }

    private fun formatTimestamp(timestamp: Long): String {
        val date = Date(timestamp)
        val outputFormat = getDateFormat()
        return outputFormat.format(date)
    }
}