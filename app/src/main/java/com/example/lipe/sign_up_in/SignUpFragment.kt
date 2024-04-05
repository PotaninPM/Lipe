package com.example.lipe.sign_up_in

import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.example.lipe.R
import com.example.lipe.database.User
import com.example.lipe.viewModels.AppVM
import com.example.lipe.databinding.FragmentSignUpBinding
import com.example.lipe.viewModels.SignUpVM
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.time.LocalDate

class SignUpFragment : Fragment() {

    private lateinit var appVM: AppVM

    private var _binding: FragmentSignUpBinding? = null
    private val binding get() = _binding!!

    private lateinit var signUpVM: SignUpVM

    private lateinit var dbRef: DatabaseReference

    private lateinit var auth: FirebaseAuth
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSignUpBinding.inflate(inflater, container, false)

        appVM = ViewModelProvider(requireActivity()).get(AppVM::class.java)
        signUpVM = ViewModelProvider(requireActivity()).get(SignUpVM::class.java)

        val view = binding.root

        return view
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //realtime data
        dbRef = FirebaseDatabase.getInstance().getReference("users")

        //auth firebase
        auth = FirebaseAuth.getInstance()

        binding.txHaveAc.setOnClickListener {
            view.findNavController().navigate(R.id.action_signUpFragment_to_signInWithEmailFragment)
        }

        binding.btnNext.setOnClickListener {
            var username: String = binding.etLogininput.text.toString().trim()
            var email:String = binding.etEmailinput.text.toString().trim()
            var phone: String = binding.etPhoneinput.text.toString().trim()
            var pass:String= binding.etPassinput.text.toString().trim()

            var name: String = binding.etNameinput.text.toString().trim()
            var lastName: String = binding.etLastNameinput.text.toString().trim()

            if(username.isNotEmpty() && email.isNotEmpty() && pass.isNotEmpty() && phone.isNotEmpty() && name.isNotEmpty() && lastName.isNotEmpty()) {
                checkIfUsernameExists(username) {result ->
                    Log.d("INFOG", result)
                    if(result == "ok") {
                        if('@' !in email || '.' !in email) {
                            setError("Вы ввели некоректный адрес почты", binding.etEmailinput)
                        } else if(pass.length < 4) {
                            setError("Пароль должен содержать хотя бы 4 символа", binding.etPassinput)
                        } else {
                            checkIfEmailExists(email) {result2 ->
                                if(result2 == "ok") {
                                    signUpVM.setData(lastName, name, username, email, phone, pass)
                                    view.findNavController().navigate(R.id.action_signUpFragment_to_signUpDescFragment)
                                } else if(result2 == "noEmail") {
                                    setError("Эта почта уже занята", binding.etEmailinput)
                                    binding.etEmailinput.setText("")
                                } else if(result2 == "Error") {
                                    Toast.makeText(requireContext(), "Что-то пошло не так", Toast.LENGTH_LONG).show()
                                }
                            }
                        }
                    } else if(result == "noUsername") {
                        setError("Такой никнейм уже занят", binding.etLogininput)
                    } else if(result == "Error") {
                        Toast.makeText(requireContext(), "Что-то пошло не так", Toast.LENGTH_LONG).show()
                    }
                }

            } else {
                checkForEmpty(username, email,pass, phone, name, lastName)
            }
        }
    }

    fun checkForEmpty(username: String, email: String, pass: String, phone: String, name: String, lastName: String) {
        if(username.isEmpty()) {
            setError("Введите имя!", binding.etLogininput)
        }
        if(email.isEmpty()) {
            setError("Введите почту!", binding.etEmailinput)
        }
        if(pass.isEmpty()) {
            Toast.makeText(requireContext(), "Введите пароль", Toast.LENGTH_LONG).show()
        }
        if(phone.isEmpty()) {
            setError("Введите номер телефона!", binding.etPhoneinput)
        }
        if(name.isEmpty()) {
            setError("Введите ваше имя!", binding.etNameinput)
        }
        if(lastName.isEmpty()) {
            setError("Введите ваше фамилию!", binding.etLastNameinput)
        }
    }



    //check username if exist(return (True) if not exist)

    fun checkIfUsernameExists(username: String, result: (String) -> Unit) {
        dbRef.orderByChild("username").equalTo(username)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if(dataSnapshot.exists() == true) {
                        result("noUsername")
                    } else {
                        result("ok")
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    result("Error")
                }
            })
    }
    fun checkIfEmailExists(email: String, result: (String) -> Unit) {
        dbRef.orderByChild("email").equalTo(email)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if(dataSnapshot.exists() == true) {
                        result("noEmail")
                    } else {
                        result("ok")
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    result("Error")
                }
            })
    }

    //set errors for fields
    private fun setError(er: String, field: TextInputEditText) {
        var textError: TextInputEditText = field
        textError.error=er
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}