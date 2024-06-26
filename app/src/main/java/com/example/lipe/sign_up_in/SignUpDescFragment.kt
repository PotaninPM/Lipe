package com.example.lipe.sign_up_in

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.example.lipe.R
import com.example.lipe.database_models.UserDB
import com.example.lipe.databinding.FragmentSignUpDescBinding
import com.example.lipe.viewModels.AppVM
import com.example.lipe.viewModels.SignUpVM
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.FirebaseStorage
import java.security.MessageDigest
import java.time.LocalDate

class SignUpDescFragment : Fragment() {

    private lateinit var spinner: Spinner

    private lateinit var dbRef: DatabaseReference

    private lateinit var auth: FirebaseAuth

    private lateinit var signUpVM: SignUpVM

    private lateinit var appVM: AppVM

    private lateinit var imageUri: Uri

    private var upload_photo: Boolean = false

    private lateinit var binding: FragmentSignUpDescBinding


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSignUpDescBinding.inflate(inflater, container, false)

        signUpVM = ViewModelProvider(requireActivity()).get(SignUpVM::class.java)

        appVM = ViewModelProvider(requireActivity()).get(AppVM::class.java)

        dbRef = FirebaseDatabase.getInstance().getReference("users")
        auth = FirebaseAuth.getInstance()

        binding.all.visibility = View.VISIBLE
        binding.progressBar.visibility = View.GONE
        binding.progressText.visibility = View.GONE

        val view = binding.root
        return view
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        binding.btnSignUp.setOnClickListener {

            binding.all.visibility = View.GONE
            binding.progressBar.visibility = View.VISIBLE
            binding.progressText.visibility = View.VISIBLE

            var desc: String = binding.descText.text.toString().trim()

            if(desc.isNotEmpty() && upload_photo == true) {
                auth.createUserWithEmailAndPassword(signUpVM.email, sha256(signUpVM.pass)).addOnCompleteListener {
                    if(it.isSuccessful) {
                        uploadImage {url ->
                            if(url != "null") {
                                addUserToDb(signUpVM.login, signUpVM.email, signUpVM.pass, signUpVM.nameAndSurname, desc, url, view)
                                val sharedPrefUser = activity?.getSharedPreferences("userRef", Context.MODE_PRIVATE)
                                val editor = sharedPrefUser?.edit()

                                editor?.apply {
                                    putString("username", signUpVM.login)
                                    putString("email", signUpVM.email)
                                    putString("password", signUpVM.pass)
                                    putString("name", signUpVM.nameAndSurname)
                                    putString("desc", desc)
                                    putInt("rating", 0)
                                    putInt("friends", 0)
                                    putInt("events", 0)
                                    putString("desc", desc)
                                    putFloat("latitude_user", 0f)
                                    putFloat("longitude_user", 0f)
                                    putFloat("zoom_user", 0f)
                                    putBoolean("enter", true)
                                    apply()
                                }
                            } else {
                                Log.d("INFOG", "Что-то пошло не так")
                                Toast.makeText(requireContext(), "Что-то пошло не так", Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                }.addOnFailureListener {
                    binding.all.visibility = View.VISIBLE
                    binding.progressBar.visibility = View.GONE
                    binding.progressText.visibility = View.GONE

                    if(it.message.toString() == "The email address is already in use by another account.")
                        Toast.makeText(requireContext(), "Такой почтовый адрес уже используется", Toast.LENGTH_LONG).show()

                    Log.d("INFOG", it.message.toString())
                }
            } else {
                checkForEmpty(desc)
                binding.all.visibility = View.VISIBLE
                binding.progressBar.visibility = View.GONE
                binding.progressText.visibility = View.GONE
            }

        }

        binding.uploadPhoto.setOnClickListener {
            selectImage.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
    }

    val selectImage = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            binding.imageArrowup.visibility = View.GONE
            binding.txtUpload.visibility = View.GONE
            binding.avatarUpload.setImageURI(uri)
            binding.avatarUpload.setStrokeColorResource(R.color.green)
            upload_photo = true
            imageUri = uri
            Log.d("INFOG", imageUri.toString())
        } else {
            Log.d("INFOG", "No media selected")
        }
    }
    fun sha256(input: String): String {
        val bytes = input.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return digest.fold("", { str, it -> str + "%02x".format(it) })
    }

    private fun uploadImage(callback: (uid: String) -> Unit) {
        val storageRef = FirebaseStorage.getInstance().getReference("avatars")
        imageUri.let { uri ->
            val uid: String = auth.currentUser!!.uid
            val imageRef = storageRef.child(uid)
            imageRef.putFile(uri)
                .addOnSuccessListener { task ->
                    task.storage.downloadUrl.addOnSuccessListener { url ->
                        callback(url.toString())
                    }
                }
                .addOnFailureListener { exception ->
                    callback("null")
                }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun addUserToDb(username: String, email: String, pass: String, names: String, desc: String, imageUrl: String, view: View) {
        FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
            val dbRef_rating = FirebaseDatabase.getInstance().getReference("rating")

            dbRef_rating.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {

                    val count = snapshot.childrenCount

                    val user_DB_info = UserDB(
                        auth.currentUser?.uid,
                        imageUrl,
                        LocalDate.now().toString(),
                        0,
                        0,
                        0,
                        desc,
                        username,
                        email,
                        sha256(pass),
                        names,
                        arrayListOf(),
                        arrayListOf(),
                        arrayListOf(),
                        arrayListOf(),
                        arrayListOf(),
                        0,
                        count + 1,
                        arrayListOf(),
                        arrayListOf(),
                        "online",
                        token,
                        "user"
                    )

                    val user = mapOf(
                        "place" to count + 1,
                        "userUid" to auth.currentUser!!.uid,
                        "points" to 0,
                    )

                    dbRef_rating.child((count + 1).toString()).setValue(user)
                        .addOnSuccessListener {
                            Log.d("INFOG", "User added")
                        }
                        .addOnFailureListener { e ->
                            Log.e("INFOG", "Error adding", e)
                        }
                    FirebaseDatabase.getInstance().getReference("location").child(auth.currentUser!!.uid).setValue(hashMapOf("latitude" to "-", "longitude" to "-")).addOnSuccessListener {
                        dbRef.child(auth.currentUser!!.uid).setValue(user_DB_info).addOnSuccessListener {

                            val navController = view.findNavController()
                            navController.navigate(R.id.action_signUpDescFragment_to_mapsFragment)

                        }.addOnFailureListener {
                            Log.d("INFOG", it.toString())
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("INFOG", "Error reading number of users from rating", error.toException())
                }
            })
        }.addOnFailureListener {
            Log.d("INFOG", "Failed to get FCM token")
        }
    }

    fun checkForEmpty(desc: String) {
        if(desc.isEmpty()) {
            Toast.makeText(requireContext(), "Введите описание!", Toast.LENGTH_LONG).show()
        }
    }
}
