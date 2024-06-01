package com.example.lipe.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ChatVM : ViewModel() {

    private var _name = MutableLiveData<String>()
    val name: LiveData<String> = _name

    private var _status = MutableLiveData<String>()
    var status: LiveData<String> = _status

    private var _avatar = MutableLiveData<String>()
    val avatar: LiveData<String> = _avatar

    private var _chatUid = MutableLiveData<String>()
    val chatUid: LiveData<String> = _chatUid

    private var _key = MutableLiveData<String>()
    val key: LiveData<String> = _key

    fun setInfo(name_ :String, status_: String, chatUid_:String, key_: String) {
        _name.value = name_
        _key.value = key_
        if(status_ == "online"){
            _status.value = "в сети"
        } else if(status_ == "offline") {
            _status.value = "не в сети"
        }
        _chatUid.value = chatUid_
    }

}