package com.example.lipe.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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

    private var _photosBefore = MutableLiveData<String>()
    val photosBefore: LiveData<String> = _photosBefore

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

    private var _timeOfCreation = MutableLiveData<String>()
    val timeOfCreation: LiveData<String> = _timeOfCreation

    private var _amountRegPeople = MutableLiveData<Int>()
    val amountRegPeople: LiveData<Int> = _amountRegPeople

    private var _creatorUsername = MutableLiveData<String>()
    val creatorUsername: LiveData<String> = _creatorUsername

    private var _getPoints = MutableLiveData<Int>()
    val getPoints: LiveData<Int> = _getPoints

    var latitude: Double = 0.0
    var longtitude: Double = 0.0

    fun setInfo(
        id_: String?,
        maxPeople_: Int?,
        minPeople_: Int?,
        powerPollution_: String?,
        title_: String?,
        creator_: String?,
        creatorUsername_: String?,
        photosBefore_: String?,
        peopleGo_: ArrayList<String>?,
        freePlaces_: Int?,
        eventDesc_: String?,
        time_of_creation_: Long,
        date_: String,
        amount_reg_people_: Int?,
        getPoints_: Int?,
    ) {
        _id.value = id_ ?: ""
        _maxPeople.value = maxPeople_ ?: 0
        _title.value = title_ ?: ""
        _creator.value = creator_ ?: ""
        _photosBefore.value = photosBefore_ ?: ""
        _peopleGo.value = peopleGo_ ?: ArrayList()
        _eventDesc.value = eventDesc_ ?: ""
        _timeOfCreation.value = formatTimestamp(time_of_creation_)
        _date.value = formatTimestamp(date_.toLong())
        _amountRegPeople.value = amount_reg_people_ ?: 0
        _freePlaces.value = 100 - 100 * amountRegPeople.value!! / maxPeople.value!!
        _powerPollution.value = powerPollution_ ?: ""
        _minPeople.value = minPeople_ ?: 0
        _getPoints.value = getPoints_ ?: 0

        _creatorUsername.value = creatorUsername_ ?: ""
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