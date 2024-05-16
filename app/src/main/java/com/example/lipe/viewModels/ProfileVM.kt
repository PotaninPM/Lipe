package com.example.lipe.viewModels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ProfileVM: ViewModel() {
    private var _nickname = MutableLiveData<String>()
    var nickname: LiveData<String> = _nickname

    private var _name = MutableLiveData<String>()
    var name: LiveData<String> = _name

    private var _friendsAmount = MutableLiveData<String>()
    val friendsAmount: LiveData<String> = _friendsAmount

    private var _eventsAmount = MutableLiveData<String>()
    val eventsAmount: LiveData<String> = _eventsAmount

    private var _ratingPoints = MutableLiveData<String>()
    val ratingPoints: LiveData<String> = _ratingPoints

    private var _desc = MutableLiveData<String>()
    val desc: LiveData<String> = _desc


    fun setInfo(nickname_: String, friendsAmount_: Int, eventsAmount_: Int, ratingPoints_: Int, desc: String, name_: String) {
        _nickname.value = nickname_
        _friendsAmount.value = friendsAmount_.toString()
        _eventsAmount.value = eventsAmount_.toString()
        _ratingPoints.value = ratingPoints_.toString()
        _desc.value = desc
        _name.value = name_

//
//        _age.value = age_
//        _creatorUsername.value = creatorUsername_
    }
}