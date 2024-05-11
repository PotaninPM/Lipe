package com.example.lipe.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class RatingVM : ViewModel() {
    private var _ratingPoints = MutableLiveData<String>()
    val ratingPoints: LiveData<String> = _ratingPoints

    private var _place = MutableLiveData<String>()
    val place: LiveData<String> = _place

    private var _avatar = MutableLiveData<String>()
    val avatar: LiveData<String> = _avatar


    fun setInfo(ratingPoints_: String, place_: String, avatar_: String) {
        _ratingPoints.value = ratingPoints_
        _place.value = place_
        _avatar.value = avatar_
    }
}