package com.example.lipe.create_events.event_eco

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
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.DatePicker
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.TimePicker
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.example.lipe.R
import com.example.lipe.create_events.event_ent.CreateEntEventFragment
import com.example.lipe.database_models.EcoEventModelDB
import com.example.lipe.database_models.GroupModel
import com.example.lipe.databinding.FragmentCreateEcoEventBinding
import com.example.lipe.notifications.RetrofitInstance
import com.example.lipe.viewModels.AppVM
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

class CreateEcoEventFragment : Fragment(), DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {

    private lateinit var appVM: AppVM

    private lateinit var imageUri1: Uri

    private var image1: String = "-"

    private var selectedPower = "-"

    private lateinit var storageRef : StorageReference

    private lateinit var binding: FragmentCreateEcoEventBinding

    private lateinit var dbRef: DatabaseReference
    private lateinit var dbRef_id: DatabaseReference
    private lateinit var auth: FirebaseAuth

    var eventId: String = ""

    var savedYear = 0
    var savedMonth = 0
    var savedDay = 0
    var savedHour = 0
    var savedMinute = 0
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentCreateEcoEventBinding.inflate(inflater, container, false)

        storageRef = FirebaseStorage.getInstance().getReference("event_images")

        setDesignToFields()

        val view = binding.root
        return view
    }


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dbRef = FirebaseDatabase.getInstance().getReference("current_events")
        dbRef_id = FirebaseDatabase.getInstance().getReference("id_event")

        auth = FirebaseAuth.getInstance()

        val items = listOf(
            getString(R.string.some),
            getString(R.string.quite_a_lot), getString(R.string.a_lot),
            getString(R.string.like_landfield))
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, items)
        val autoCompleteTextView = view.findViewById<AutoCompleteTextView>(R.id.pollutionSpinner)
        autoCompleteTextView.setAdapter(adapter)
        autoCompleteTextView.setOnItemClickListener { parent, view, position, id ->
            selectedPower = parent.getItemAtPosition(position).toString()
        }

        binding.btnCreateEvent.setOnClickListener {
            binding.creating.visibility = View.VISIBLE
            binding.allEco.visibility = View.GONE
            binding.progressBar.visibility = View.VISIBLE
            if(isAdded) {
                binding.btnCreateEvent.isEnabled = false
                binding.btnCreateEvent.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.grey))

                uploadImage {photos ->
                    if(photos != "-") {
                        createEvent(photos)
                    } else {
                        binding.btnCreateEvent.isEnabled = true
                        binding.btnCreateEvent.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.green))
                        setDialog(getString(R.string.no_image),
                            getString(R.string.min_one_photo), "Хорошо")
                    }
                }
            }
        }

        binding.dateLay.setOnClickListener {
            getDateTime()
        }

        binding.photoLay1.setOnClickListener {
            selectImage1.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
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
    private fun uploadImage(callback: (photos: String) -> Unit) {
        val storageRef = FirebaseStorage.getInstance().getReference("event_images")

        if (image1 == "-") {
            callback("-")
        } else {
            if(isAdded) {
                if (image1 != "-") {
                    imageUri1.let { uri ->
                        val uid: String = UUID.randomUUID().toString()
                        val imageRef = storageRef.child(uid)
                        imageRef.putFile(uri)
                            .addOnSuccessListener { task ->
                                task.storage.downloadUrl.addOnSuccessListener { url ->
                                    callback(url.toString())
                                }
                            }
                            .addOnFailureListener { exception ->
                                callback("-")
                            }
                    }
                }
            }
        }
    }

    private fun setDialog(title: String, desc: String, btnText: String) {
        if(isAdded) {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(title)
                .setMessage(desc)
                .setPositiveButton(btnText) { dialog, which ->
                    dialog.dismiss()
                }
                .show()
        }
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

    private fun createEvent(photosBefore: String) {
            appVM = ViewModelProvider(requireActivity()).get(AppVM::class.java)
            if(checkForEmpty() == true) {
                eventId = UUID.randomUUID().toString()

                val current = System.currentTimeMillis()

                var title = binding.etNameinput.text.toString().trim()

                var coord: HashMap<String, Double> = hashMapOf("latitude" to appVM.latitude, "longitude" to appVM.longtitude)
                //var date_of_meeting: String = binding.text.toString()
                val minPeople: Int = binding.etMinInputText.text.toString().trim().toInt()
                var maxPeople: Int = binding.etMaxInputText.text.toString().trim().toInt()
                var desc: String = binding.etDescInputText.text.toString().trim()

                var date_of_meeting: String = binding.timeText.text.toString() + " " + binding.dateText.text.toString()

                //need to be repaired
                var getPoints: Int = -1

                getPoints = when(selectedPower) {
                    "Не очень сильный" -> 5
                    "Достаточно много" -> 7
                    "Очень много" -> 10
                    "Похоже на свалку" -> 15
                    else -> 0
                }


                var type: String = "eco"

                var event = EcoEventModelDB(
                    eventId,
                    type,
                    auth.currentUser?.uid.toString(),
                    current.toString(),
                    title,
                    coord,
                    selectedPower,
                    parseDateToTimestamp(date_of_meeting).toString(),
                    minPeople,
                    maxPeople,
                    desc,
                    photosBefore,
                    hashMapOf(auth.currentUser?.uid.toString() to auth.currentUser?.uid.toString()),
                    1,
                    getPoints,
                    "ok",
                    Instant.now().epochSecond
                )

                val dbRef_user_your = FirebaseDatabase.getInstance().getReference("users/${event.creator_id}/yourCreatedEvents")
                val dbRef_user_groups = FirebaseDatabase.getInstance().getReference("users/${event.creator_id}/groups")
                val dbRef_user_events_amount = FirebaseDatabase.getInstance().getReference("users/${event.creator_id}/events_amount")

                val dbRef_group = FirebaseDatabase.getInstance().getReference("groups")

                val latitude = event.coordinates["latitude"]!!.toDouble()
                val longitude = event.coordinates["longitude"]!!.toDouble()
                val creatorUid = event.creator_id

                val dbRef_events = FirebaseDatabase.getInstance().getReference("current_events")
                val dbRef_user_cr = FirebaseDatabase.getInstance().getReference("users/${event.creator_id}/curRegEventsId")

                dbRef_events.child(event.event_id).setValue(event) {e, _ ->
                    dbRef_user_cr.child(event.event_id).setValue(event.event_id) {e, _ ->
                        dbRef_user_your.child(event.event_id).setValue(event.event_id) {e, _ ->
                            val group = CreateEntEventFragment.GroupModel(
                                event.event_id,
                                event.title,
                                event.photos,
                                hashMapOf(event.creator_id to event.creator_id),
                                arrayListOf()
                            )
                            dbRef_group.child(event.event_id).setValue(group) {e, _ ->
                                dbRef_user_groups.child(event.event_id).setValue(event.event_id) {e, _ ->

                                }
                            }
                            dbRef_user_events_amount.addListenerForSingleValueEvent(object :
                                ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    dbRef_user_events_amount.setValue(snapshot?.value.toString().toInt() + 1) {e, _ ->

                                    }
                                }

                                override fun onCancelled(error: DatabaseError) {
                                    TODO("Not yet implemented")
                                }

                            })
                        }
                    }
                }

                val call: Call<Void> = RetrofitInstance.api.sendEventEcoData(event)

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
                //binding.allEco.visibility = View.VISIBLE
                binding.creating.visibility = View.GONE
                binding.congrats.visibility = View.VISIBLE
                binding.progressBar.visibility = View.GONE
            } else {
                binding.btnCreateEvent.isEnabled = true
                binding.btnCreateEvent.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.green))
            }
    }

    fun checkForEmpty(): Boolean {
        var check: Boolean = true
        if(binding.etDescInputText.text.toString().isEmpty()) {
            setError("Введите описание!", binding.etDescInputText)
            check = false
            return false
        }
        if(binding.etNameinput.text.toString().isEmpty()) {
            setError("Введите название!", binding.etNameinput)
            check = false
            return false
        }
        if(binding.etMaxInputText.text.toString().isEmpty()) {
            setError("Введите количество людей!", binding.etMaxInputText)
            check = false
            return false
        }
        return true
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
    fun onTimeSet_() {
        binding.notifySetDate.visibility = View.GONE
        binding.timeText.visibility = View.VISIBLE
        binding.timeText.text = String.format("%02d:%02d", savedHour, savedMinute)

        val months = resources.getStringArray(R.array.months)
        val monthName = months[savedMonth]

        binding.dateText.visibility = View.VISIBLE
        binding.dateText.text = String.format("%d %s %d ${getString(R.string.year)}", savedDay, monthName, savedYear)

        binding.dateLay.setBackgroundResource(R.drawable.chosen_date_lay)
    }

    override fun onDateSet(p0: DatePicker?, p1: Int, p2: Int, p3: Int) {
        TODO("Not yet implemented")
    }
    override fun onTimeSet(p0: TimePicker?, p1: Int, p2: Int) {
        TODO("Not yet implemented")
    }
}