package com.example.lipe.create_events.event_ent

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.graphics.Color
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
import android.widget.DatePicker
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.TimePicker
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModelProvider
import com.example.lipe.CryptAlgo
import com.example.lipe.R
import com.example.lipe.viewModels.AppVM
import com.example.lipe.databinding.FragmentCreateEntEventBinding
import com.example.lipe.database_models.EntEventModelDB
import com.example.lipe.database_models.GroupModel
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.Calendar
import java.util.Date
import java.util.TimeZone
import java.util.UUID


class CreateEntEventFragment : Fragment(), DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {
    //view model
    private lateinit var appVM: AppVM

    var type_sport = "1"

    private lateinit var imageUri1: Uri
    private lateinit var imageUri2: Uri
    private lateinit var imageUri3: Uri

    private var image1: String = "-"
    private var image2: String = "-"
    private var image3: String = "-"

    private var imagesUid: ArrayList<String> = arrayListOf("-", "-", "-")

    private lateinit var firebaseRef: DatabaseReference

    private lateinit var spinner: Spinner

    private lateinit var storageRef : StorageReference

    private var _binding: FragmentCreateEntEventBinding? = null
    private val binding get() = _binding!!

    private var items: List<SpinnerItem> = listOf(
        SpinnerItem("Выберите тип развлечения", R.drawable.light_bulb),
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
        SpinnerItem("Кёрлинг", R.drawable.curling),
        SpinnerItem("Хоккей", R.drawable.ice_hockey),
        SpinnerItem("Катание на коньках", R.drawable.ice_skate),
        SpinnerItem("Лыжная ходьба", R.drawable.skiing_1),
        SpinnerItem("Горные лыжи", R.drawable.skiing),
        SpinnerItem("Сноуборд", R.drawable.snowboarding),
        SpinnerItem("Настольные игры", R.drawable.board_game),
        SpinnerItem("Мобильные игры", R.drawable.mobile_game),
        SpinnerItem("Шахматы", R.drawable.chess_2),
        SpinnerItem("Программирование", R.drawable.programming)
    )

    private var levels: List<SpinnerItem> = listOf(
        SpinnerItem("Для любого возраста", R.drawable.light_bulb),
        SpinnerItem("Для детей(6 - 14 лет)", R.drawable.img_basketballimg),
        SpinnerItem("Для подростков(14 - 18)", R.drawable.volleyball_2),
        SpinnerItem("Для взрослых(более 18)", R.drawable.football),
    )

    private lateinit var dbRef_events: DatabaseReference
    private lateinit var dbRef_users: DatabaseReference
    private lateinit var auth: FirebaseAuth

    var eventId: String = ""

    var savedYear = 0
    var savedMonth = 0
    var savedDay = 0
    var savedHour = 0
    var savedMinute = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentCreateEntEventBinding.inflate(inflater, container, false)

        storageRef = FirebaseStorage.getInstance().getReference("event_images")

        setDesignToFields()

        spinner = binding.spinner1

        val adapter = CustomAdapter(requireContext(), items)

        spinner.adapter = adapter
        binding.setDateLay.setOnClickListener {
            getDateTime()
        }

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedItem = items[position]
                type_sport = selectedItem.name
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
        }

        val view = binding.root
        return view
    }


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dbRef_events = FirebaseDatabase.getInstance().getReference("current_events")
        dbRef_users = FirebaseDatabase.getInstance().getReference("users")

        auth = FirebaseAuth.getInstance()

        binding.btnCreateEvent.setOnClickListener {
//            binding.scrollView.visibility = View.GONE
//            binding.loadingProgressBar.visibility = View.VISIBLE
            uploadImage {photos ->
                if(photos[0] != "-" || photos[1] != "-" || photos[2] != "-") {
                    createEvent(photos)
                } else {
                    setDialog("Вы не загрузили ни одного фото", "Вы должны загрузить минимум одно фото", "Хорошо")
                }
            }
        }

        binding.photoLay1.setOnClickListener {
            selectImage1.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
        binding.photoLay2.setOnClickListener {
            selectImage2.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
        binding.photoLay3.setOnClickListener {
            selectImage3.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
    }

    val selectImage1 = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            binding.photo1.setImageURI(uri)
            imageUri1 = uri
            image1 = "1"
            Log.d("INFOG", imageUri1.toString())
        } else {
            Log.d("INFOG", "No media selected")
        }
    }
    val selectImage2 = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            binding.photo2.setImageURI(uri)
            imageUri2 = uri
            image2 = "1"
            Log.d("INFOG", imageUri1.toString())
        } else {
            Log.d("INFOG", "No media selected")
        }
    }
    val selectImage3 = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            binding.photo3.setImageURI(uri)
            imageUri3 = uri
            image3 = "1"
            Log.d("INFOG", imageUri1.toString())
        } else {
            Log.d("INFOG", "No media selected")
        }
    }
    private fun uploadImage(callback: (photos: ArrayList<String>) -> Unit) {
        val storageRef = FirebaseStorage.getInstance().getReference("event_images")

        val photos: ArrayList<String> = arrayListOf()
        if (image1 == "-" && image2 == "-" && image3 == "-") {
            callback(arrayListOf("-", "-", "-"))
        } else {
            var used: Int = 0;
            if(image1 != "-") {
                imageUri1.let { uri ->
                    val uid: String = UUID.randomUUID().toString()
                    val imageRef = storageRef.child(uid)
                    imageRef.putFile(uri)
                        .addOnSuccessListener { task ->
                            task.storage.downloadUrl.addOnSuccessListener { url ->
                                photos.add(uid)
                                callback(photos)
                            }
                        }
                        .addOnFailureListener { exception ->
                            callback(arrayListOf("-", "-", "-"))
                            used = -1
                        }
                }
            }
//
//            if(image2 != "-") {
//                imageUri2.let { uri ->
//                    val uid: String = UUID.randomUUID().toString()
//                    val imageRef = storageRef.child(uid)
//                    imageRef.putFile(uri)
//                        .addOnSuccessListener { task ->
//                            task.storage.downloadUrl.addOnSuccessListener { url ->
//                                photos.add(uid)
//                            }
//                        }
//                        .addOnFailureListener { exception ->
//                            callback(arrayListOf("-", "-", "-"))
//                            used = -1
//                        }
//                }
//            }
//
//            if(image3 != "-") {
//                imageUri3.let { uri ->
//                    val uid: String = UUID.randomUUID().toString()
//                    val imageRef = storageRef.child(uid)
//                    imageRef.putFile(uri)
//                        .addOnSuccessListener { task ->
//                            task.storage.downloadUrl.addOnSuccessListener { url ->
//                                photos.add(uid)
//                            }
//                        }
//                        .addOnFailureListener { exception ->
//                            callback(arrayListOf("-", "-", "-"))
//                            used = -1
//                        }
//                }
//            }
//
        }
    }

    private fun setDialog(title: String, desc: String, btnText: String) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(title)
            .setMessage(desc)
            .setPositiveButton(btnText) { dialog, which ->
                dialog.dismiss()
            }
            .show()
    }

    private fun createEvent(photos: ArrayList<String>) {
        appVM = ViewModelProvider(requireActivity()).get(AppVM::class.java)
        if (checkForEmpty() == true) {
            eventId = UUID.randomUUID().toString()
            val time = Calendar.getInstance().time
            val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm")
            val current = formatter.format(time)

            var title = binding.etNameinputText.text.toString().trim()
            var adress = binding.adressText.text.toString().trim()
            var coord: List<Double> = listOf(appVM.latitude, appVM.longtitude)
            var date_of_meeting: String = binding.setDate.text.toString()
            var maxPeople: Int = binding.etMaxInputText.text.toString().trim().toInt()
            var desc: String = binding.etDescInputText.text.toString().trim()

            var type: String = "ent"

            if(type_sport == "1" || type_sport == "Выберите тип развлечения") {
                Toast.makeText(requireContext(), "Введите спорт!", Toast.LENGTH_LONG).show()
            } else {
                val uidGroup = UUID.randomUUID().toString()
                var event = EntEventModelDB(
                    eventId,
                    type,
                    auth.currentUser?.uid.toString(),
                    current,
                    type_sport,
                    title,
                    adress,
                    coord,
                    date_of_meeting,
                    maxPeople,
                    "16-18",
                    desc,
                    photos,
                    arrayListOf(auth.currentUser?.uid),
                    1,
                    "ok",
                    uidGroup
                )

                val dbRef_user = FirebaseDatabase.getInstance().getReference("users/${auth.currentUser!!.uid}/curRegEventsId")
                val dbRef_user_your = FirebaseDatabase.getInstance().getReference("users/${auth.currentUser!!.uid}/yourCreatedEvents")
                val dbRef_group = FirebaseDatabase.getInstance().getReference("groups")

                dbRef_events.child(eventId).setValue(event).addOnSuccessListener {
                    dbRef_user.child(eventId).setValue(eventId).addOnSuccessListener {
                        dbRef_user_your.child(eventId).setValue(eventId).addOnSuccessListener {
                            val group = GroupModel(uidGroup, arrayListOf(auth.currentUser!!.uid), arrayListOf("-"))
                            dbRef_group.child(uidGroup).setValue(group).addOnSuccessListener {
                                //do smth
                            }
                        }
                    }
                }
            }
        }
    }

    fun checkForEmpty(): Boolean {
        var check: Boolean = true
        if(binding.etDescInputText.text.toString().isEmpty()) {
            setError("Введите описание!", binding.etDescInputText)
            check = false
        }
        if(binding.etNameinputText.text.toString().isEmpty()) {
            setError("Введите название!", binding.etNameinputText)
            check = false
        }
        if(binding.etMaxInputText.text.toString().isEmpty()) {
            setError("Введите количество людей!", binding.etMaxInputText)
            check = false
        }
        return check
    }

    private fun setError(er: String, field: TextInputEditText) {
        var textError: TextInputEditText = field
        textError.error=er
    }

    private fun setDesignToFields() {
        binding.etNameinputLay.boxStrokeColor = Color.BLUE
        binding.etDescInputLay.boxStrokeColor = Color.BLUE
        binding.etMaxInputLay.boxStrokeColor = Color.BLUE
    }

    data class SpinnerItem(val name: String, val imageResourceId: Int)
    class CustomAdapter(context: Context, private val items: List<SpinnerItem>) : ArrayAdapter<SpinnerItem>(context,
        R.layout.spinner_one_chose, items) {
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.spinner_one_chose, parent, false)

            val imageView = view.findViewById<ImageView>(R.id.imageView)
            val textView = view.findViewById<TextView>(R.id.textView)

            val item = getItem(position)
            textView.text = item?.name
            imageView.setImageResource(item?.imageResourceId ?: 0)

            return view
        }

        override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
            return getView(position, convertView, parent)
        }
    }
    private fun getDateTime() {
        val datePicker =
            MaterialDatePicker.Builder.datePicker()
                .setTitleText("Выберите дату")
                .build()

        datePicker.addOnPositiveButtonClickListener {
            val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
            calendar.time = Date(it)
            savedYear = calendar.get(Calendar.YEAR)
            savedMonth = calendar.get(Calendar.MONTH)
            savedDay = calendar.get(Calendar.DAY_OF_MONTH)

            val timePicker = MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_24H)
                .setInputMode(MaterialTimePicker.INPUT_MODE_CLOCK)
                .setTitleText("Выберите время")
                .build()

            timePicker.addOnPositiveButtonClickListener {
                savedHour = timePicker.hour
                savedMinute = timePicker.minute
                onTimeSet_()
            }
            timePicker.show(childFragmentManager, "time_picker_tag")
        }

        datePicker.show(childFragmentManager, "date_picker_tag")
    }
    fun onTimeSet_() {
        binding.setDateLay.setBackgroundResource(R.drawable.choose_date_success)
        binding.setDate.setText("$savedYear.$savedMonth.$savedDay в $savedHour:$savedMinute")
    }

    override fun onDateSet(p0: DatePicker?, p1: Int, p2: Int, p3: Int) {
        TODO("Not yet implemented")
    }
    override fun onTimeSet(p0: TimePicker?, p1: Int, p2: Int) {
        TODO("Not yet implemented")
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }


}
