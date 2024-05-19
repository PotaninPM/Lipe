package com.example.lipe.view_events.event_eco

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.viewModelFactory
import coil.ImageLoader
import coil.request.ImageRequest
import com.example.lipe.R
import com.example.lipe.databinding.FragmentEventEcoBinding
import com.example.lipe.databinding.FragmentProfileBinding
import com.example.lipe.viewModels.AppVM
import com.example.lipe.viewModels.EventEcoVM
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.GenericTypeIndicator
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.ArrayList

class EventEcoFragment : Fragment() {

    private lateinit var auth: FirebaseAuth

    private lateinit var dbRef: DatabaseReference

    private lateinit var dbRef_event: DatabaseReference

    private lateinit var dbRef_user: DatabaseReference

    private lateinit var storageRef : StorageReference

    private lateinit var appVM: AppVM

    private lateinit var binding: FragmentEventEcoBinding

    private val eventEcoVM: EventEcoVM by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentEventEcoBinding.inflate(inflater, container, false)

        binding.allEcoEvent.visibility = View.GONE
        binding.loadingProgressBar.visibility = View.VISIBLE

        binding.apply {
            lifecycleOwner = viewLifecycleOwner
            viewModel = eventEcoVM

            storageRef = FirebaseStorage.getInstance().reference

            appVM = ViewModelProvider(requireActivity()).get(AppVM::class.java)

            dbRef_event = FirebaseDatabase.getInstance().getReference("current_events")

            searchEvent(appVM.latitude, appVM.longtitude) {ready ->
                if(ready) {
                    loadAllImages {ready->
                        if(ready) {
                            binding.allEcoEvent.visibility = View.VISIBLE
                            binding.loadingProgressBar.visibility = View.GONE
                        }
                    }
                }
            }
        }

        auth = FirebaseAuth.getInstance()

        val view = binding.root
        return view
    }

    private fun loadAllImages(callback: (Boolean) -> Unit) {
        val uid = eventEcoVM.photosBefore.value?.get(0).toString().removeSurrounding("[", "]")

        val photoRef = storageRef.child("event_images/$uid")
        val tokenTask = photoRef.downloadUrl

        val userAvatarRef = storageRef.child("avatars/${eventEcoVM.creator.value}")

        userAvatarRef.downloadUrl.addOnSuccessListener {url ->
            lifecycleScope.launch {
                val bitmap = withContext(Dispatchers.IO) {
                    ImageLoader(requireContext()).execute(
                        ImageRequest.Builder(requireContext())
                            .data(url)
                            .build()
                    )
                }.drawable?.toBitmap()

                binding.eventAvatar.setImageBitmap(bitmap)
            }
        }

        tokenTask.addOnSuccessListener { url ->
            lifecycleScope.launch {
                val bitmap = withContext(Dispatchers.IO) {
                    ImageLoader(requireContext()).execute(
                        ImageRequest.Builder(requireContext())
                            .data(url)
                            .build()
                    )
                }.drawable?.toBitmap()

                binding.image.setImageBitmap(bitmap)
            }
            callback(true)

        }.addOnFailureListener {
            callback(false)
        }
    }

    private fun searchEvent(coord1: Double, coord2: Double, callback: (ready: Boolean) -> Unit) {
        val dbRefEvent = FirebaseDatabase.getInstance().getReference("current_events")
        val dbRefUser = FirebaseDatabase.getInstance().getReference("users")

        dbRefEvent.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for(eventSnapshot in dataSnapshot.children) {
                    val coordinates: List<Double>? = listOf(eventSnapshot.child("coordinates").child("latitude").value.toString().toDouble(), eventSnapshot.child("coordinates").child("longitude").value.toString().toDouble())
                    if(coordinates != null && coordinates[0] == coord1 && coordinates[1] == coord2) {
                        val type = eventSnapshot.child("type_of_event").value.toString()
                        val id = eventSnapshot.child("event_id").value.toString()
                        val maxPeople = eventSnapshot.child("max_people").value.toString().toInt()
                        val minPeople = eventSnapshot.child("min_people").value.toString().toInt()
                        val title = eventSnapshot.child("title").value.toString()
                        val description = eventSnapshot.child("description").value.toString()
                        val creatorUid = eventSnapshot.child("creator_id").value.toString()
                        val photosBefore = arrayListOf(eventSnapshot.child("photo_before_id").value.toString())
                        val freePlaces = maxPeople - eventSnapshot.child("amount_reg_people").value.toString().toInt()
                        val timeOfCreation = eventSnapshot.child("time_of_creation").value.toString()
                        val dateOfMeeting = eventSnapshot.child("date_of_meeting").value.toString()
                        val amountRegPeople = eventSnapshot.child("amount_reg_people").value.toString().toInt()
                        //val peopleGo = eventSnapshot.child("reg_people_id").value
                        val getPoints:Int = eventSnapshot.child("get_points").value.toString().toInt()
                        val powerPollution: String = eventSnapshot.child("power_of_pollution").value.toString()

                        when(type) {
                            "eco" -> {
                                var found: Boolean = false
                                dbRefUser.addValueEventListener(object : ValueEventListener {
                                    override fun onDataChange(userSnapshot: DataSnapshot) {
                                        for(userEventSnapshot in userSnapshot.children) {
                                            if(creatorUid == userEventSnapshot.child("uid").value) {
                                                val creatorUsername = userEventSnapshot.child("username").value.toString()
                                                val userPhoto = "1"
                                                Log.d("INFOG", "нуы")
                                                eventEcoVM.setInfo(
                                                    id,
                                                    maxPeople,
                                                    minPeople,
                                                    powerPollution,
                                                    title,
                                                    creatorUid,
                                                    creatorUsername,
                                                    photosBefore,
                                                    arrayListOf("1"),
                                                    freePlaces,
                                                    description,
                                                    timeOfCreation,
                                                    dateOfMeeting,
                                                    amountRegPeople,
                                                    getPoints)
                                                callback(true)
                                                found = true
                                                return
                                            }
                                        }

                                        if(found == false) {
                                            eventEcoVM.setInfo(
                                                id,
                                                maxPeople,
                                                minPeople,
                                                powerPollution,
                                                title,
                                                creatorUid,
                                                "Удаленный аккаунт",
                                                photosBefore,
                                                arrayListOf("1"),
                                                freePlaces,
                                                description,
                                                timeOfCreation,
                                                dateOfMeeting,
                                                amountRegPeople,
                                                getPoints)
                                            callback(true)
                                        }
                                    }

                                    override fun onCancelled(error: DatabaseError) {
                                        callback(false)
                                    }
                                })
                            }
                        }
                        break
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("FirebaseError", "Ошибка Firebase ${databaseError.message}")
            }
        })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        binding.allEntEvent.visibility = View.GONE
//        binding.loadingProgressBar.visibility = View.VISIBLE
    }

}