package com.example.lipe.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class OtherProfileVM : ViewModel() {
    private var _nameLastName = MutableLiveData<String>()
    var nameLastName: LiveData<String> = _nameLastName

    private var _friendsAmount = MutableLiveData<String>()
    val friendsAmount: LiveData<String> = _friendsAmount

    private var _eventsAmount = MutableLiveData<String>()
    val eventsAmount: LiveData<String> = _eventsAmount

    private var _ratingPoints = MutableLiveData<String>()
    val ratingPoints: LiveData<String> = _ratingPoints


    fun setInfo(nameLastName_: String, friendsAmount_: Int, eventsAmount_: Int, ratingPoints_: Int) {
        _nameLastName.value = nameLastName_
        _friendsAmount.value = friendsAmount_.toString()
        _eventsAmount.value = eventsAmount_.toString()
        _ratingPoints.value = ratingPoints_.toString()

//
//        _age.value = age_
//        _creatorUsername.value = creatorUsername_
    }
}