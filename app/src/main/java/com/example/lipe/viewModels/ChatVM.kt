package com.example.lipe.viewModels

import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.lipe.R
import java.util.Locale

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

    private var _opponentUid = MutableLiveData<String>()
    val opponentUid: LiveData<String> = _opponentUid

    fun setInfo(name_ :String, status_: String, chatUid_:String, key_: String, opponentUid_: String) {
        _name.value = name_
        _key.value = key_
        if(status_ == "online") {
            if(Locale.getDefault().language == "ru") {
                _status.value = "онлайн"
            } else {
                _status.value = "online"
            }
        } else {
            if(Locale.getDefault().language == "ru") {
                _status.value = "не в сети"
            } else {
                _status.value = "offline"
            }
        }
        _opponentUid.value = opponentUid_
        _chatUid.value = chatUid_
    }

}