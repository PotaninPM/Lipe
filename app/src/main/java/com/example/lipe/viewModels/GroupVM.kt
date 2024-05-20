package com.example.lipe.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class GroupVM : ViewModel() {
    private var _name = MutableLiveData<String>()
    val name: LiveData<String> = _name

    private var _avatar = MutableLiveData<String>()
    val avatar: LiveData<String> = _avatar

    private var _groupUid = MutableLiveData<String>()
    val groupUid: LiveData<String> = _groupUid

    private var _countMembers = MutableLiveData<String>()
    val countMembers: LiveData<String> = _countMembers

    fun setInfo(name_ :String, chatUid_:String, countMembers_: String) {
        _name.value = name_
        _groupUid.value = chatUid_
        _countMembers.value = "$countMembers_ участников"
    }

    fun setPhoto(photo_: String) {
        _avatar.value = photo_
    }
}