package com.example.lipe.viewModels

import androidx.lifecycle.ViewModel

class SignUpVM: ViewModel() {
    var lastName = ""
    var name = ""
    var login = ""
    var email = ""
    var number = ""
    var pass = ""

    fun setData(lastName_: String, name_: String, login_: String, email_: String, number_: String, pass_: String) {
        lastName = lastName_
        name = name_
        login = login_
        email = email_
        number = number_
        pass = pass_
    }
}