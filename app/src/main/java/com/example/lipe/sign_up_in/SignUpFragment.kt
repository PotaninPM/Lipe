package com.example.lipe.sign_up_in

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.example.lipe.R
import com.example.lipe.databinding.FragmentSignUpBinding
import com.example.lipe.viewModels.AppVM
import com.example.lipe.viewModels.SignUpVM
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener


class SignUpFragment : Fragment() {

    private lateinit var appVM: AppVM
    private lateinit var binding: FragmentSignUpBinding
    private lateinit var signUpVM: SignUpVM
    private lateinit var dbRef: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var googleSignInLauncher: ActivityResultLauncher<Intent>

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSignUpBinding.inflate(inflater, container, false)
        appVM = ViewModelProvider(requireActivity()).get(AppVM::class.java)
        signUpVM = ViewModelProvider(requireActivity()).get(SignUpVM::class.java)
        auth = Firebase.auth
        dbRef = FirebaseDatabase.getInstance().getReference("users")

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("895088919548-56gvv91cvkgf398eim3d52nfj8i27fll.apps.googleusercontent.com")
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)

        googleSignInLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->

            Log.d("INFOG", result.data.toString())
            Log.d("INFOG", Activity.RESULT_OK.toString())
            Log.d("INFOG", result.resultCode.toString())
            if (result.resultCode == Activity.RESULT_OK) {
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                handleSignInResult(task)
            } else {
                Log.d("INFOG", "Failed with result code ${result.resultCode}")
            }
        }

        binding.signUpWithGoogle.setOnClickListener {
            signInWithGoogle()
        }

        binding.txHaveAc.setOnClickListener {
            view?.findNavController()?.navigate(R.id.action_signUpFragment_to_signInWithEmailFragment)
        }

        binding.btnNext.setOnClickListener {
            if(isAdded && context != null) {
                val username: String = binding.etLogininput.text.toString().trim()
                val email: String = binding.etEmailinput.text.toString().trim()
                val pass: String = binding.etPassinput.text.toString().trim()
                val nameAndSurname: String = binding.etNameAndSurnameinput.text.toString().trim()

                if(username.isNotEmpty() && pass.isNotEmpty() && email.isNotEmpty() && nameAndSurname.isNotEmpty() && username.length < 51 && username.length < 13 && email.length < 31 && pass.length < 51 && binding.agreeTerms.isChecked == true) {
                    checkIfUsernameExists(username) { result ->
                        Log.d("INFOG", result)
                        if (result == "ok") {
                            if (pass.length < 4) {
                                binding.etPassinput.error = getString(R.string._4)
                            } else {
                                checkIfEmailExists(email) { result2 ->
                                    if (result2 == "ok") {
                                        signUpVM.setData(nameAndSurname, username, email, pass)
                                        view?.findNavController()
                                            ?.navigate(R.id.action_signUpFragment_to_signUpDescFragment)
                                    } else if (result2 == "noEmail") {
                                        setError("Эта почта уже занята", binding.etEmailinput)
                                        binding.etEmailinput.setText("")
                                    } else if (result2 == "Error") {
                                        Toast.makeText(
                                            requireContext(),
                                            "Что-то пошло не так",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                                }
                            }
                        } else if (result == "noUsername") {
                            setError("Такой никнейм уже занят", binding.etLogininput)
                        } else if (result == "Error") {
                            Toast.makeText(
                                requireContext(),
                                "Что-то пошло не так",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                } else {
                    checkForEmpty(username, pass, email, nameAndSurname)
                }
            }
        }

        return binding.root
    }

    private fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        googleSignInLauncher.launch(signInIntent)
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)
            val idToken = account?.idToken ?: ""
            firebaseAuthWithGoogle(idToken)
        } catch (e: ApiException) {
            Log.w("INFOG", "Google sign in failed", e)
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    user?.let {
                        val email = it.email
                        val displayName = it.displayName
                        val photoUrl = it.photoUrl?.toString()

                        Log.d("INFOG", "Email: $email")
                        Log.d("INFOG", "Display Name: $displayName")
                        Log.d("INFOG", "Photo URL: $photoUrl")

                    }
                } else {
                    Log.w("INFOG", "signInWithCredential:failure", task.exception)
                }
            }
    }

    private fun checkForEmpty(username: String, pass: String, email: String, nameAndSurname: String) {
        if(username.isEmpty()) {
            setError(getString(R.string.enter_login), binding.etLogininput)
        }
        if(binding.agreeTerms.isChecked == false) {
            Toast.makeText(requireContext(), getString(R.string.policy), Toast.LENGTH_LONG).show()
        }
        if (pass.isEmpty()) {
            setError(getString(R.string.enter_pass), binding.etPassinput)
        }
        if (email.isEmpty()) {
            setError(getString(R.string.enter_email), binding.etEmailinput)
        }
        if (nameAndSurname.isEmpty()) {
            setError(getString(R.string.enter_name_lastname), binding.etNameAndSurnameinput)
        }
    }

    private fun setError(er: String, field: TextInputEditText) {
        field.error = er
    }

    private fun checkIfUsernameExists(username: String, result: (String) -> Unit) {
        dbRef.orderByChild("username").equalTo(username)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
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

    private fun checkIfEmailExists(email: String, result: (String) -> Unit) {
        dbRef.orderByChild("email").equalTo(email)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
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

    data class User(
        val name: String,
        val email: String,
        val photoUrl: String
    )
}
