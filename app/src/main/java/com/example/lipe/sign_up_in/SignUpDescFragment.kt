package com.example.lipe.sign_up_in

import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.example.lipe.R
import com.example.lipe.database.UserDB
import com.example.lipe.databinding.FragmentSignUpDescBinding
import com.example.lipe.viewModels.AppVM
import com.example.lipe.viewModels.SignUpVM
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import java.time.LocalDate
import java.util.UUID

class SignUpDescFragment : Fragment() {

    private lateinit var spinner: Spinner

    private lateinit var dbRef: DatabaseReference

    private lateinit var auth: FirebaseAuth

    private lateinit var signUpVM: SignUpVM

    private lateinit var appVM: AppVM

    private lateinit var imageUri: Uri

    private var upload_photo: Boolean = false

    private var _binding: FragmentSignUpDescBinding? = null
    private val binding get() = _binding!!

    private var items: List<SpinnerItem> = listOf(
        SpinnerItem("Ваши увлечения", R.drawable.light_bulb),
        //summer act
        SpinnerItem("Баскетбол", R.drawable.img_basketballimg),
        SpinnerItem("Воллейбол", R.drawable.volleyball_2),
        SpinnerItem("Футбол", R.drawable.football),
        SpinnerItem("Рэгби", R.drawable.rugby_ball),
        SpinnerItem("Воркаут", R.drawable.weights),
        SpinnerItem("Большой тенис", R.drawable.tennis),
        SpinnerItem("Бадминтон", R.drawable.shuttlecock),
        SpinnerItem("Пинпонг", R.drawable.table_tennis),
        SpinnerItem("Гимнастика", R.drawable.gymnastic_rings),
        SpinnerItem("Фехтование", R.drawable.fencing),
        SpinnerItem("Бег", R.drawable.running_shoe),
        //winter act
        SpinnerItem("Кёрлинг", R.drawable.curling),
        SpinnerItem("Хоккей", R.drawable.ice_hockey),
        SpinnerItem("Катание на коньках", R.drawable.ice_skate),
        SpinnerItem("Лыжная ходьба", R.drawable.skiing_1),
        SpinnerItem("Горные лыжи", R.drawable.skiing),
        SpinnerItem("Сноуборд", R.drawable.snowboarding),
        //home act
        SpinnerItem("Настольные игры", R.drawable.board_game),
        SpinnerItem("Мобильные игры", R.drawable.mobile_game),
        SpinnerItem("Шахматы", R.drawable.chess_2),
        //program
        SpinnerItem("Программирование", R.drawable.programming),
        //noth
        SpinnerItem("Ничего", R.drawable.remove),
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSignUpDescBinding.inflate(inflater, container, false)

        signUpVM = ViewModelProvider(requireActivity()).get(SignUpVM::class.java)

        appVM = ViewModelProvider(requireActivity()).get(AppVM::class.java)

        dbRef = FirebaseDatabase.getInstance().getReference("users")
        auth = FirebaseAuth.getInstance()

        spinner = binding.spinner1

        binding.all.visibility = View.VISIBLE
        binding.progressBar.visibility = View.GONE

        val adapter = CustomAdapter(requireContext(), items)

        spinner.adapter = adapter

        val view = binding.root
        return view
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {


        binding.btnSignUp.setOnClickListener {

            binding.all.visibility = View.GONE
            binding.progressBar.visibility = View.VISIBLE

            var desc: String = binding.descText.text.toString().trim()

            if(desc.isNotEmpty() && upload_photo == true) {
                auth.createUserWithEmailAndPassword(signUpVM.email, signUpVM.pass).addOnCompleteListener {
                    if(it.isSuccessful) {
                        uploadImage {uid ->
                            if(uid != "null") {
                                appVM.reg = "yes"
                                addUserToDb(signUpVM.login, signUpVM.email, signUpVM.pass, signUpVM.number, signUpVM.name, signUpVM.lastName, desc, uid, view)
                            } else {
                                Log.d("INFOG", "Что-то пошло не так")
                                Toast.makeText(requireContext(), "Что-то пошло не так", Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                }.addOnFailureListener {
                    binding.all.visibility = View.VISIBLE
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(requireContext(), "Smth went wrong", Toast.LENGTH_LONG).show()

                    Log.d("INFOG", "NO!")
                }
            } else {
                checkForEmpty(desc)
                binding.all.visibility = View.VISIBLE
                binding.progressBar.visibility = View.GONE
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
            binding.avatar.setImageURI(uri)
            binding.avatar.setStrokeColorResource(R.color.green)
            upload_photo = true
            imageUri = uri
            Log.d("INFOG", imageUri.toString())
        } else {
            Log.d("INFOG", "No media selected")
        }
    }

    private fun uploadImage(callback: (uid: String) -> Unit) {
        val storageRef = FirebaseStorage.getInstance().getReference("avatars")
        imageUri.let { uri ->
            val uid: String = auth.currentUser!!.uid
            val imageRef = storageRef.child(uid)
            imageRef.putFile(uri)
                .addOnSuccessListener { task ->
                    task.storage.downloadUrl.addOnSuccessListener { url ->
                        callback(uid)
                    }
                }
                .addOnFailureListener { exception ->
                    callback("null")
                }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun addUserToDb(username: String, email: String, pass: String, phone: String, name: String, lastName: String, desc: String, avatar: String, view: View) {
        val user_DB_info = UserDB(
            auth.currentUser?.uid,
            avatar,
            LocalDate.now().toString(),
            0,
            0,
            0,
            desc,
            username,
            email,
            "7" + phone,
            pass,
            name,
            lastName,
            -1,
            "-",
            arrayListOf("-"),
            arrayListOf("Добро пожаловать!"),
            arrayListOf("-"),
            0,
            arrayListOf("-"),
            arrayListOf("-"),
            0,
        )

        dbRef.child(auth.currentUser!!.uid).setValue(user_DB_info).addOnSuccessListener {

            val navController = view.findNavController()
            navController.navigate(R.id.action_signUpDescFragment_to_mapsFragment)

        }.addOnFailureListener {
            Log.d("INFOG", it.toString())
        }
    }

    fun checkForEmpty(desc: String) {
        if(desc.isEmpty()) {
            Toast.makeText(requireContext(), "Введите описание!", Toast.LENGTH_LONG).show()
        }
    }

    private fun setError(er: String, field: TextInputEditText) {
        var textError: TextInputEditText = field
        textError.error=er
    }

    data class SpinnerItem(val name: String, val imageResourceId: Int)

    private class CustomAdapter(context: Context, private val items: List<SpinnerItem>) : ArrayAdapter<SpinnerItem>(context,
        R.layout.spinner_multi_chose, items) {

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.spinner_multi_chose, parent, false)

            val checkBox = view.findViewById<CheckBox>(R.id.checkBox)
            val imageView = view.findViewById<ImageView>(R.id.imageView)
            val textView = view.findViewById<TextView>(R.id.textView)

            if(position == 0) {
                checkBox.visibility = View.GONE
            } else {
                checkBox.visibility = View.VISIBLE
            }

            val item = getItem(position)
            textView.text = item?.name
            imageView.setImageResource(item?.imageResourceId ?: 0)

            return view
        }
        override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
            return getView(position, convertView, parent)
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}
