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

class SignUpFragment : Fragment() {

    private lateinit var appVM: AppVM

    private lateinit var binding: FragmentSignUpBinding

    private lateinit var signUpVM: SignUpVM

    private lateinit var dbRef: DatabaseReference

    private lateinit var auth: FirebaseAuth
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSignUpBinding.inflate(inflater, container, false)

        appVM = ViewModelProvider(requireActivity()).get(AppVM::class.java)
        signUpVM = ViewModelProvider(requireActivity()).get(SignUpVM::class.java)

        binding.signUpWithGoogle.setOnClickListener {
            signInWithGoogle()
        }
        val view = binding.root

        return view
    }

    private fun signInWithGoogle() {

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
            var email: String = binding.etEmailinput.text.toString().trim()
            var pass:String= binding.etPassinput.text.toString().trim()

            var nameAndSurname: String = binding.etNameAndSurnameinput.text.toString().trim()

            if(username.isNotEmpty() && pass.isNotEmpty() && email.isNotEmpty() && nameAndSurname.isNotEmpty()) {
                checkIfUsernameExists(username) {result ->
                    Log.d("INFOG", result)
                    if(result == "ok") {
                        if(pass.length < 4) {
                            binding.etPassinput.error = "Пароль должен содержать хотя бы 4 символа"
                        } else {
                            checkIfEmailExists(email) {result2 ->
                                if(result2 == "ok") {
                                    signUpVM.setData(nameAndSurname, username, email, pass)
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
                        setError( "Такой никнейм уже занят", binding.etLogininput)
                    } else if(result == "Error") {
                        Toast.makeText(requireContext(), "Что-то пошло не так", Toast.LENGTH_LONG).show()
                    }
                }

            } else {
                checkForEmpty(username,pass, email, nameAndSurname)
            }
        }
    }

    fun checkForEmpty(username: String, pass: String, phone: String, nameAndSurname: String) {
        if(username.isEmpty()) {
            setError("Введите логин!", binding.etLogininput)
        }
        if(pass.isEmpty()) {
            setError("Введите пароль", binding.etPassinput)
        }
        if(phone.isEmpty()) {
            setError("Введите электронную почту!", binding.etEmailinput)
        }
        if(nameAndSurname.isEmpty()) {
            setError("Введите ваше имя и фамилию!", binding.etNameAndSurnameinput)
        }
    }
    private fun setError(er: String, field: TextInputEditText) {
        var textError: TextInputEditText = field
        textError.error=er
    }

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
}