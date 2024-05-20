package com.example.lipe.view_events.event_eco

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.viewModelFactory
import coil.ImageLoader
import coil.request.ImageRequest
import com.example.lipe.R
import com.example.lipe.choose_people.ChoosePeopleFragment
import com.example.lipe.databinding.FragmentEventEcoBinding
import com.example.lipe.databinding.FragmentProfileBinding
import com.example.lipe.people_go_to_event.PeopleGoToEventFragment
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
                        if (ready) {
                            checkIfUserAlreadyReg(
                                auth.currentUser!!.uid,
                                eventEcoVM.id.value.toString()
                            ) { ans ->
                                if (ans) {
                                    binding?.btnRegToEvent?.visibility = View.GONE

                                    if (eventEcoVM.creator.value == auth.currentUser!!.uid) {
                                        binding?.deleteOrLeave?.visibility = View.VISIBLE
                                        binding?.deleteOrLeave?.setText("Завершить")

                                        binding?.listUsers?.setText("Список")
                                        binding?.listUsers?.visibility = View.VISIBLE
                                    } else {
                                        binding?.deleteOrLeave?.setText("Покинуть")
                                        binding?.deleteOrLeave?.visibility = View.VISIBLE

                                        binding?.listUsers?.setText("Список")
                                        binding?.listUsers?.visibility = View.VISIBLE
                                    }

                                    binding?.allEcoEvent?.visibility = View.VISIBLE
                                    binding?.loadingProgressBar?.visibility = View.GONE
                                } else {
                                    binding?.allEcoEvent?.visibility = View.VISIBLE
                                    binding?.loadingProgressBar?.visibility = View.GONE
                                }
                            }
                        }
                    }
                }
            }
            listUsers?.setOnClickListener {
                showPeopleGoDialog(0)
            }

            deleteOrLeave?.setOnClickListener {
                if(auth.currentUser!!.uid == eventEcoVM.creator.value) {
                    showPeopleGoDialog(1)
                } else {
                    deleteUserFromEvent(eventEcoVM.id.value.toString())

                    binding?.deleteOrLeave?.visibility = View.GONE
                    binding?.listUsers?.visibility = View.GONE

                    binding?.btnRegToEvent?.visibility = View.VISIBLE
                }
            }
        }

        auth = FirebaseAuth.getInstance()

        val view = binding.root
        return view
    }

    fun deleteUserFromEvent(uid: String) {
        val dbRef_user = FirebaseDatabase.getInstance().getReference("users").child(auth.currentUser!!.uid).child("curRegEventsId").child(uid)
        val userInEventRef = dbRef_event.child(eventEcoVM.id.value.toString()).child("reg_people_id").child(auth.currentUser!!.uid)
        val curPeople = dbRef_event.child(eventEcoVM.id.value.toString()).child("amount_reg_people")
        val groupRef = FirebaseDatabase.getInstance().getReference("groups/${eventEcoVM.id.value}/members/${auth.currentUser!!.uid}")
        val groupInProfile = FirebaseDatabase.getInstance().getReference("users/${auth.currentUser!!.uid}/groups/$uid")
        dbRef_user.removeValue().addOnSuccessListener {
            userInEventRef.removeValue()
                .addOnSuccessListener {
                    groupRef.removeValue().addOnSuccessListener {
                        curPeople.setValue(eventEcoVM.amount_reg_people.value?.minus(1)).addOnSuccessListener {
                            groupInProfile.removeValue().addOnSuccessListener {
                                binding.deleteOrLeave.visibility = View.GONE
                                binding.btnRegToEvent.visibility = View.VISIBLE
                            }
                        }
                    }
                }
                .addOnFailureListener {
                    Log.e("INFOG", "ErLeaveEvent")
                }
        }
    }

    private fun showPeopleGoDialog(lay: Int) {
        var dialog: DialogFragment?= null

        dialog = when(lay) {
            0 -> PeopleGoToEventFragment(eventEcoVM.id.value.toString())
            1 -> ChoosePeopleFragment(eventEcoVM.id.value.toString())
            else -> DialogFragment()
        }
        dialog.show(childFragmentManager, "PeopleGoDialog")
    }

    private fun checkIfUserAlreadyReg(curUid: String, eventId: String, callback: (Boolean) -> Unit) {
        try {
            dbRef_event = FirebaseDatabase.getInstance().getReference("current_events")
            dbRef_event.child(eventId).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val regPeopleSnapshot = dataSnapshot.child("reg_people_id")
                    var isUserRegistered = false
                    for (childSnapshot in regPeopleSnapshot.children) {
                        val uid = childSnapshot.getValue(String::class.java)
                        if(uid == curUid) {
                            isUserRegistered = true
                            break
                        }
                    }
                    callback(isUserRegistered)
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.e("INFOG", "Ошибка Firebase ${databaseError.message}")
                }
            })
        } catch (e: Exception) {
            Log.d("INFOG", e.message.toString())
        }
    }

    private fun loadAllImages(callback: (Boolean) -> Unit) {

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
            lifecycleScope.launch {
                val bitmap = withContext(Dispatchers.IO) {
                    ImageLoader(requireContext()).execute(
                        ImageRequest.Builder(requireContext())
                            .data(eventEcoVM.photosBefore.value.toString())
                            .build()
                    )
                }.drawable?.toBitmap()

                binding.image.setImageBitmap(bitmap)
            }
            callback(true)
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
                        val photos = eventSnapshot.child("photos").value.toString()
                        val freePlaces = maxPeople - eventSnapshot.child("amount_reg_people").value.toString().toInt()
                        val timeOfCreation = eventSnapshot.child("time_of_creation").value.toString()
                        val dateOfMeeting = eventSnapshot.child("date_of_meeting").value.toString()
                        val amountRegPeople = eventSnapshot.child("amount_reg_people").value.toString().toInt()
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
                                                Log.d("INFOG", "нуы")
                                                eventEcoVM.setInfo(
                                                    id,
                                                    maxPeople,
                                                    minPeople,
                                                    powerPollution,
                                                    title,
                                                    creatorUid,
                                                    creatorUsername,
                                                    photos,
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
                                                photos,
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