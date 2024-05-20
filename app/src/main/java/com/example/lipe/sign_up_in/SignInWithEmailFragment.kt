package com.example.lipe.sign_up_in

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import com.example.lipe.R
import com.example.lipe.databinding.FragmentSignInWithEmailBinding
import com.example.lipe.databinding.FragmentSignUpBinding
import com.example.lipe.databinding.FragmentStartBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.security.MessageDigest

class SignInWithEmailFragment : Fragment() {

    private lateinit var binding: FragmentSignInWithEmailBinding

    private lateinit var dbRef: DatabaseReference
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSignInWithEmailBinding.inflate(inflater, container, false)

        //realtime data
        dbRef = FirebaseDatabase.getInstance().getReference("users")

        //auth firebase
        auth = FirebaseAuth.getInstance()

        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        binding.btnSignIn.setOnClickListener {
            var email: String = binding.etEmailinput.text.toString()
            var pass: String = binding.etPassinput.text.toString()

            if(email.isNotEmpty() && pass.isNotEmpty()) {
                signInUser(email, pass) {ready ->
                    if(ready) {
                        view.findNavController().navigate(R.id.action_signInWithEmailFragment_to_mapsFragment)
                    }
                }
            }
        }

        binding.txtNoAc.setOnClickListener {
            view.findNavController().navigate(R.id.action_signInWithEmailFragment_to_signUpFragment)
        }

//        binding.txtNoAc.setOnClickListener {
//            view.findNavController().navigate(R.id.action_signInWithEmailFragment_to_signUpFragment)
//        }
    }
    fun sha256(input: String): String {
        val bytes = input.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return digest.fold("", { str, it -> str + "%02x".format(it) })
    }

    private fun signInUser(email:String, pass: String, ready: (Boolean) -> Unit) {
        auth.signInWithEmailAndPassword(email, sha256(pass)).addOnCompleteListener {
            if(it.isSuccessful) {
                ready(true)
            } else {
                ready(false)
            }
        }
    }
}