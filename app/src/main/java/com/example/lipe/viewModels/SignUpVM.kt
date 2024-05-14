package com.example.lipe.viewModels

import androidx.lifecycle.ViewModel

class SignUpVM: ViewModel() {
    var nameAndSurname = ""
    var login = ""
    var email = ""
    var pass = ""

    fun setData(nameAndSurname_: String, login_: String, email_: String, pass_: String) {
        nameAndSurname = nameAndSurname_
        login = login_
        email = email_
        pass = pass_
    }
}