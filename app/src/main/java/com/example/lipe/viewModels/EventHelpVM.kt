package com.example.lipe.viewModels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class EventHelpVM : ViewModel() {
    private var _id = MutableLiveData<String>()
    val id: LiveData<String> = _id

    private var _maxPeople = MutableLiveData<Int>()
    val maxPeople: LiveData<Int> = _maxPeople

    private var _creator = MutableLiveData<String>()
    val creator: LiveData<String> = _creator

    private var _photos = MutableLiveData<ArrayList<String>>()
    val photos: LiveData<ArrayList<String>> = _photos

    private var _freePlaces = MutableLiveData<Int>()
    var freePlaces: LiveData<Int> = _freePlaces

    private var _eventDesc = MutableLiveData<String>()
    val eventDesc: LiveData<String> = _eventDesc

    private var _date = MutableLiveData<String>()
    val date: LiveData<String> = _date

    private var _typeSport = MutableLiveData<String>()
    var typeSport: LiveData<String> = _typeSport

    private var _type = MutableLiveData<String>()
    val type: LiveData<String> = _type

    private var _timeOfCreation = MutableLiveData<String>()
    val timeOfCreation: LiveData<String> = _timeOfCreation

    private var _amountRegPeople = MutableLiveData<Int>()
    val amountRegPeople: LiveData<Int> = _amountRegPeople

    private var _creatorUsername = MutableLiveData<String>()
    val creatorUsername: LiveData<String> = _creatorUsername

    private var _price = MutableLiveData<String>()
    val price: LiveData<String> = _price

    private var _friend = MutableLiveData<String>()
    val friend: LiveData<String> = _friend

    var latitude: Double = 0.0
    var longtitude: Double = 0.0

    fun setCoord(lat: Double, long: Double) {
        latitude = lat
        longtitude = long
    }

    fun setInfo(id_: String, price_: Int, creator_: String, creatorUsername_: String, photos_: ArrayList<String>, freePlaces_: Int, eventDesc_: String, time_of_creation_: Long, date_: String, friend_: String) {
        _id.value = id_
        _creator.value = creator_
        _photos.value = photos_
        _eventDesc.value = eventDesc_
        _freePlaces.value = freePlaces_

        _timeOfCreation.value = formatTimestamp(time_of_creation_)
        _date.value = formatTimestamp(date_.toLong())

        _friend.value = friend_

        _price.value = price_.toString() + " ₽"
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