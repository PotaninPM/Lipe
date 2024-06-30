package com.example.lipe.viewModels

import android.content.Context
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.util.Locale

class GroupVM : ViewModel() {

    private var _name = MutableLiveData<String>()
    val name: LiveData<String> = _name

    private var _avatar = MutableLiveData<String>()
    val avatar: LiveData<String> = _avatar

    private var _groupUid = MutableLiveData<String>()
    val groupUid: LiveData<String> = _groupUid

    private var _countMembers = MutableLiveData<String>()
    val countMembers: LiveData<String> = _countMembers

    private var _key = MutableLiveData<String>()
    val key: LiveData<String> = _key

    fun setInfo(name_ :String, chatUid_:String, countMembers_: String, key_: String) {
        _name.value = name_
        _groupUid.value = chatUid_
        if(Locale.getDefault().language == "ru") {
            _countMembers.value = "$countMembers_ участников"
        } else {
            _countMembers.value = "$countMembers_ participants"
        }
        _key.value = key_
    }

    fun setPhoto(photo_: String) {
        _avatar.value = photo_
    }
}