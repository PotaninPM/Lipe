package com.example.lipe.create_events.event_ent

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.res.ColorStateList
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
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import com.example.lipe.R
import com.example.lipe.viewModels.AppVM
import com.example.lipe.databinding.FragmentCreateEntEventBinding
import com.example.lipe.notifications.EntEventData
import com.example.lipe.notifications.EventData
import com.example.lipe.notifications.RetrofitInstance
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.ArrayList
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.UUID


class CreateEntEventFragment : Fragment(), DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {
    //view model
    private val appVM: AppVM by activityViewModels()

    var typeSport = "1"

    private var selectedAge: Int = -1

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

    private lateinit var binding: FragmentCreateEntEventBinding

    private lateinit var items: List<SpinnerItem>

    private lateinit var dbrefEvents: DatabaseReference
    private lateinit var dbrefUsers: DatabaseReference
    private lateinit var auth: FirebaseAuth

    data class GroupModel(
        val uid: String,
        val title: String,
        val imageUid: String,
        val members: HashMap<String, String>,
        val messages: ArrayList<String>
    )

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

        binding = FragmentCreateEntEventBinding.inflate(inflater, container, false)

        storageRef = FirebaseStorage.getInstance().getReference("event_images")

        items = listOf(
            SpinnerItem(getString(R.string.choose_ent_type), R.drawable.light_bulb, "Choose entertainment type"),
            SpinnerItem(getString(R.string.basketball), R.drawable.img_basketballimg, "Basketball"),
            SpinnerItem(getString(R.string.volleyball), R.drawable.volleyball_2, "Volleyball"),
            SpinnerItem(getString(R.string.football), R.drawable.football, "Football"),
            SpinnerItem(getString(R.string.rugby), R.drawable.rugby_ball, "Rugby"),
            SpinnerItem(getString(R.string.workout), R.drawable.weights, "Workout"),
            SpinnerItem(getString(R.string.tennis), R.drawable.tennis, "Tennis"),
            SpinnerItem(getString(R.string.badminton), R.drawable.shuttlecock, "Badminton"),
            SpinnerItem(getString(R.string.table_tennis), R.drawable.table_tennis, "Table tennis"),
            SpinnerItem(getString(R.string.gymnastics), R.drawable.gymnastic_rings, "Gymnastics"),
            SpinnerItem(getString(R.string.fencing), R.drawable.fencing, "Fencing"),
            SpinnerItem(getString(R.string.jogging), R.drawable.running_shoe, "Jogging"),
            SpinnerItem(getString(R.string.curling), R.drawable.curling, "Curling"),
            SpinnerItem(getString(R.string.hockey), R.drawable.ice_hockey, "Hockey"),
            SpinnerItem(getString(R.string.ice_skating), R.drawable.ice_skate, "Ice skating"),
            SpinnerItem(getString(R.string.skiing), R.drawable.skiing_1, "Skiing"),
            SpinnerItem(getString(R.string.downhill_skiing), R.drawable.skiing, "Downhill skiing"),
            SpinnerItem(getString(R.string.snowboarding), R.drawable.snowboarding, "Snowboarding"),
            SpinnerItem(getString(R.string.table_games), R.drawable.board_game, "Table games"),
            SpinnerItem(getString(R.string.mobile_games), R.drawable.mobile_game, "Mobile games"),
            SpinnerItem(getString(R.string.chess), R.drawable.chess_2, "Chess"),
            SpinnerItem(getString(R.string.programming), R.drawable.programming, "Programming")
        )

        setDesignToFields()

        spinner = binding.spinner1

        val adapter = CustomAdapter(requireContext(), items)

        spinner.adapter = adapter

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedItem = items[position]
                typeSport = selectedItem.idString
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
        if(isAdded) {
            dbrefEvents = FirebaseDatabase.getInstance().getReference("current_events")
            dbrefUsers = FirebaseDatabase.getInstance().getReference("users")

            auth = FirebaseAuth.getInstance()

            val items = listOf(getString(R.string.any_age), getString(R.string.more_18),
                getString(R.string.before_18))
            val adapter =
                ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, items)
            val autoCompleteTextView = binding.ageSpinner
            autoCompleteTextView.setAdapter(adapter)
            autoCompleteTextView.setOnItemClickListener { parent, view, position, id ->
                selectedAge = position
            }

            binding.btnCreateEvent.setOnClickListener {
                binding.btnCreateEvent.isEnabled = false
                binding.btnCreateEvent.backgroundTintList = ColorStateList.valueOf(Color.GRAY)

                val eventUid = UUID.randomUUID().toString()
                if (checkForEmpty() == true) {
                    uploadImage(eventUid) { photo ->
                        if (photo != "-") {
                            createEvent(eventUid, photo)
                        } else {
                            binding.btnCreateEvent.isEnabled = true
                            binding.btnCreateEvent.setBackgroundColor(
                                ContextCompat.getColor(
                                    requireContext(),
                                    R.color.green
                                )
                            )
                            setDialog(
                                getString(R.string.no_image),
                                getString(R.string.min_one_photo),
                                getString(R.string.okey),
                            )
                        }
                    }
                }else {
                    binding.allEnt.visibility = View.VISIBLE

                    binding.progressBar.visibility = View.INVISIBLE
                    binding.creating.visibility = View.INVISIBLE
                    binding.btnCreateEvent.isEnabled = true
                    binding.btnCreateEvent.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.green))
                }
            }

            binding.dateLay.setOnClickListener {
                getDateTime()
            }

            binding.photoLay1.setOnClickListener {
                selectImage1.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            }
        }
    }

    val selectImage1 = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            binding.photo1.setImageURI(uri)
            binding.arrowUp.visibility = View.GONE
            binding.textImg.visibility = View.GONE
            imageUri1 = uri
            image1 = "1"
            Log.d("INFOG", imageUri1.toString())
        } else {
            Log.d("INFOG", "No media selected")
        }
    }
    private fun uploadImage(eventUid: String, callback: (photo: String) -> Unit) {
        val storageRef = FirebaseStorage.getInstance().getReference("event_images")

        if (image1 == "-") {
            callback("-")
        } else {
            if(image1 != "-") {
                binding.allEnt.visibility = View.GONE
                binding.progressBar.visibility = View.VISIBLE
                binding.creating.visibility = View.VISIBLE
                imageUri1.let { uri ->
                    val imageRef = storageRef.child(eventUid)
                    imageRef.putFile(uri)
                        .addOnSuccessListener { task ->
                            task.storage.downloadUrl.addOnSuccessListener { url ->
                                callback(url.toString())
                            }
                        }
                        .addOnFailureListener { exception ->
                            binding.btnCreateEvent.isEnabled = true
                            binding.btnCreateEvent.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.green))
                            callback("-")
                        }
                }
            } else {
                binding.btnCreateEvent.isEnabled = true
                binding.btnCreateEvent.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.green))
            }
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

    fun parseDateToTimestamp(dateString: String): Long {
        val locale = Locale.getDefault()
        if(locale.language == "ru") {
            val pattern = "HH:mm dd MMMM yyyy 'года'"
            val dateFormat = SimpleDateFormat(pattern, locale)

            return try {
                val date = dateFormat.parse(dateString)
                date?.time ?: throw IllegalArgumentException("Invalid date string")
            } catch (e: Exception) {
                e.printStackTrace()
                0L
            }
        } else {
            val pattern = "HH:mm dd MMMM yyyy 'year'"
            val dateFormat = SimpleDateFormat(pattern, locale)

            return try {
                val date = dateFormat.parse(dateString)
                date?.time ?: throw IllegalArgumentException("Invalid date string")
            } catch (e: Exception) {
                e.printStackTrace()
                0L
            }
        }
    }

    private fun createEvent(eventUid: String, photos: String) {
        binding.creating.visibility = View.VISIBLE
        binding.btnCreateEvent.isEnabled = false
        binding.btnCreateEvent.backgroundTintList = ColorStateList.valueOf(Color.GRAY)
            eventId = eventUid

            val current = System.currentTimeMillis()

            var title = binding.etNameinputText.text.toString().trim()
            var coord: HashMap<String, Double> =
                hashMapOf("latitude" to appVM.latitude, "longitude" to appVM.longtitude)
            var date_of_meeting: String =
                binding.timeText.text.toString() + " " + binding.dateText.text.toString()

            Log.i("INFOG", parseDateToTimestamp(date_of_meeting).toString())

            var maxPeople: Int = binding.etMaxInputText.text.toString().trim().toInt()
            var desc: String = binding.etDescInputText.text.toString().trim()

            var type: String = "ent"

            var age: String = when (selectedAge) {
                0 -> "any_age"
                1 -> "more_18"
                2 -> "before_18"
                else -> "-1"
            }

            if (typeSport == "1") {
                binding.btnCreateEvent.isEnabled = true
                Toast.makeText(
                    requireContext(),
                    getString(R.string.choose_sport),
                    Toast.LENGTH_LONG
                ).show()
            } else {
                var event = EntEventData(
                    eventId,
                    type,
                    auth.currentUser?.uid.toString(),
                    current.toString(),
                    typeSport,
                    title,
                    coord,
                    parseDateToTimestamp(date_of_meeting).toString(),
                    maxPeople,
                    age,
                    desc,
                    photos,
                    hashMapOf(auth.currentUser?.uid to auth.currentUser?.uid),
                    1,
                    "ok",
                    Instant.now().epochSecond
                )

                val dbRef_user_your = FirebaseDatabase.getInstance()
                    .getReference("users/${event.creator_id}/yourCreatedEvents")
                val dbRef_user_groups =
                    FirebaseDatabase.getInstance().getReference("users/${event.creator_id}/groups")
                val dbRef_user_events_amount = FirebaseDatabase.getInstance()
                    .getReference("users/${event.creator_id}/events_amount")

                val dbRef_group = FirebaseDatabase.getInstance().getReference("groups")

                val latitude = event.coordinates["latitude"]!!.toDouble()
                val longitude = event.coordinates["longitude"]!!.toDouble()
                val creatorUid = event.creator_id

                val dbRef_events = FirebaseDatabase.getInstance().getReference("current_events")
                val dbRef_user_cr = FirebaseDatabase.getInstance()
                    .getReference("users/${event.creator_id}/curRegEventsId")

                dbRef_events.child(event.event_id).setValue(event) { e, _ ->
                    dbRef_user_cr.child(event.event_id).setValue(event.event_id) { e, _ ->
                        dbRef_user_your.child(event.event_id).setValue(event.event_id) { e, _ ->
                            val group = GroupModel(
                                event.event_id,
                                event.title,
                                event.photos,
                                hashMapOf(event.creator_id to event.creator_id),
                                arrayListOf()
                            )
                            dbRef_group.child(event.event_id).setValue(group) { e, _ ->
                                dbRef_user_groups.child(event.event_id)
                                    .setValue(event.event_id) { e, _ ->

                                    }
                            }
                            dbRef_user_events_amount.addListenerForSingleValueEvent(object :
                                ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    dbRef_user_events_amount.setValue(
                                        snapshot.value.toString().toInt() + 1
                                    ) { e, _ ->

                                    }
                                }

                                override fun onCancelled(error: DatabaseError) {
                                    TODO("Not yet implemented")
                                }

                            })
                        }
                    }
                }

                val call: Call<Void> = RetrofitInstance.api.sendEventData(
                    EventData(
                        latitude.toFloat(),
                        longitude.toFloat()
                    )
                )

                Log.d("INFOG", call.request().toString())

                call.enqueue(object : Callback<Void> {
                    override fun onResponse(call: Call<Void>, response: Response<Void>) {
                        if (response.isSuccessful) {
                            Log.d("INFOG", "OK, notifications were sent")
                        } else {
                            Log.d("INFOG", "${response.message()}")
                        }
                    }

                    override fun onFailure(call: Call<Void>, t: Throwable) {
                        Log.d("INFOG", "${t.message}")
                    }
                })

                binding.creating.visibility = View.GONE
                binding.congrats.visibility = View.VISIBLE
                binding.progressBar.visibility = View.INVISIBLE
            }
    }

    fun checkForEmpty(): Boolean {
        var check: Boolean = true
        if(binding.etDescInputText.text.toString().isEmpty()) {
            setError(getString(R.string.enter_desc), binding.etDescInputText)
            check = false
        }
        if(binding.etNameinputText.text.toString().isEmpty()) {
            setError(getString(R.string.enter_title), binding.etNameinputText)
            check = false
        }
        if(binding.etMaxInputText.text.toString().isEmpty()) {
            setError(getString(R.string.enter_people_amount), binding.etMaxInputText)
            check = false
        }
        if(selectedAge == -1) {
            setDialog(getString(R.string.age_was_not_chosen), getString(R.string.must_age), getString(R.string.okey))
            check = false
        }
        if(savedYear == 0) {
            setDialog(getString(R.string.date_start_event),
                getString(R.string.date_of_meeting), getString(R.string.okey))
            check = false
        }
        if(isAdded) {
            if (typeSport == "1" || typeSport == getString(R.string.choose_ent_type)) {
                setDialog(
                    getString(R.string.no_sport_error),
                    getString(R.string.must_type_sport), getString(R.string.okey)
                )
                check = false
            }
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

    data class SpinnerItem(val name: String, val imageResourceId: Int, val idString: String)
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
        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText(getString(R.string.choose_the_date))
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
                .setTitleText(getString(R.string.choose_time))
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

    private fun onTimeSet_() {
        binding.notifySetDate.visibility = View.GONE
        binding.timeText.visibility = View.VISIBLE
        binding.timeText.text = String.format("%02d:%02d", savedHour, savedMinute)

        val locale = Locale.getDefault().language

        val months = if (locale == "ru") {
            resources.getStringArray(R.array.months_ru)
        } else {
            resources.getStringArray(R.array.months_eng)
        }
        val monthName = months[savedMonth]

        binding.dateText.visibility = View.VISIBLE
        binding.dateText.text = String.format(
            "%d %s %d %s",
            savedDay,
            monthName,
            savedYear,
            getString(R.string.year)
        )

        binding.dateLay.setBackgroundResource(R.drawable.chosen_date_lay)
    }

    override fun onDateSet(p0: DatePicker?, p1: Int, p2: Int, p3: Int) {

    }

    override fun onTimeSet(p0: TimePicker?, p1: Int, p2: Int) {

    }

}
