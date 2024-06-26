package com.example.lipe.view_events.event_help

import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import coil.Coil
import coil.request.ImageRequest
import com.example.lipe.R
import com.example.lipe.choose_people.ChoosePeopleFragment
import com.example.lipe.databinding.FragmentEventHelpBinding
import com.example.lipe.viewModels.AppVM
import com.example.lipe.viewModels.EventHelpVM
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EventHelpFragment : Fragment() {

    private lateinit var auth: FirebaseAuth

    private lateinit var dbRef: DatabaseReference

    private lateinit var dbRef_event: DatabaseReference

    private lateinit var dbRef_user: DatabaseReference

    private lateinit var storageRef : StorageReference

    private val appVM: AppVM by activityViewModels()

    private val eventHelpVM: EventHelpVM by activityViewModels()

    private lateinit var binding: FragmentEventHelpBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentEventHelpBinding.inflate(inflater, container, false)

        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()

        storageRef = FirebaseStorage.getInstance().reference

        dbRef_event = FirebaseDatabase.getInstance().getReference("current_events")

        searchEvent(appVM.latitude, appVM.longtitude) { ready ->
            val user = auth.currentUser!!.uid

            if (ready) {
                loadAllImages { ready ->
                    if (ready) {
                        checkIfUserAlreadyReg(
                            auth.currentUser!!.uid,
                            eventHelpVM.id.value.toString()
                        ) { ans ->
                            if(ans) {
                                binding.btnRegToEvent.visibility = View.GONE

                                val date_ = eventHelpVM.date.value!!
                                binding.dateOfMeetingHelp.text = buildString {
                                    append(
                                        date_.substring(
                                            6,
                                            date_.length
                                        )
                                    )
                                    append(getString(R.string.`in`))
                                    append(date_.substring(0, 5))
                                }

                                if (eventHelpVM.creator.value == user) {
                                    binding.deleteOrLeave.visibility = View.VISIBLE
                                    binding.deleteOrLeave.text = getString(R.string.finish)
                                } else {
                                    binding.deleteOrLeave.text = getString(R.string.leave)
                                    binding.deleteOrLeave.visibility = View.VISIBLE
                                }

                                binding.allHelpEvent.visibility = View.VISIBLE
                                binding.loadingProgressBar.visibility = View.GONE
                            } else {
                                binding.dateOfMeetingHelp.text = "*****"
                                binding.allHelpEvent.visibility = View.VISIBLE
                                binding.loadingProgressBar.visibility = View.GONE
                            }
                        }
                    }
                }
            }
        }

        binding.allHelpEvent.visibility = View.GONE
        binding.loadingProgressBar.visibility = View.VISIBLE

        //binding.creator.setOnClickListener {
//            val context = it.context
//            if (context is AppCompatActivity) {
//                val cardView = context.findViewById<CardView>(R.id.cardView)
//                cardView.visibility = View.GONE
//                if (eventEntVM.creator.value.toString() != auth.currentUser!!.uid) {
//                    val context = it.context
//                    if (context is AppCompatActivity) {
//                        val fragment = OtherProfileFragment(eventEntVM.creator.value.toString())
//                        val fragmentManager = context.supportFragmentManager
//                        fragmentManager.beginTransaction()
//                            .replace(R.id.allEntEvent, fragment)
//                            .addToBackStack(null)
//                            .commit()
//                    }
//                }
//            }
       // }

        binding?.apply {
            lifecycleOwner = viewLifecycleOwner
            viewModel = eventHelpVM

            deleteOrLeave.setOnClickListener {
                if (auth.currentUser!!.uid == eventHelpVM.creator.value) {
                    showPeopleGoDialog(1)
                    //deleteEvent(eventEntVM.id.value.toString())
                } else {
                    binding.dateOfMeetingHelp.text = "*******"
                    deleteUserFromEvent(eventHelpVM.id.value.toString())

                    binding.deleteOrLeave.visibility = View.GONE

                    binding.btnRegToEvent.visibility = View.VISIBLE
                }
            }


            btnRegToEvent.setOnClickListener {
                val curUid = auth.currentUser?.uid
                if (curUid != null) {
                    checkIfUserAlreadyReg(curUid, eventHelpVM.id.value!!) { isUserAlreadyRegistered ->
                        if (!isUserAlreadyRegistered) {
                            regUserToEvent(curUid) { result ->
                                if (result == true) {
                                    setDialog(
                                        getString(R.string.success_reg),
                                        getString(R.string.congrats_success_reg),
                                        getString(R.string.nice)
                                    )
                                } else {
                                    //fail
                                    setDialog(
                                        getString(R.string.error_reg),
                                        getString(R.string.smth_went_wrong_reg_event),
                                        getString(R.string.okey)
                                    )
                                }
                            }
                        } else {
                            Log.d("INFOG", "Пользователь уже зарегистрирован на мероприятие")
                        }
                    }
                } else {
                    Log.e("INFOG", "UID пользователя не найден")
                }
            }
        }
    }

    fun deleteUserFromEvent(uid: String) {
        val dbRef_user = FirebaseDatabase.getInstance().getReference("users").child(auth.currentUser!!.uid).child("curRegEventsId").child(uid)
        val userInEventRef = dbRef_event.child(eventHelpVM.id.value.toString()).child("reg_people_id").child(auth.currentUser!!.uid)
        val curPeople = dbRef_event.child(eventHelpVM.id.value.toString()).child("amount_reg_people")
        val groupRef = FirebaseDatabase.getInstance().getReference("groups/${eventHelpVM.id.value}/members/${auth.currentUser!!.uid}")
        val groupInProfile = FirebaseDatabase.getInstance().getReference("users/${auth.currentUser!!.uid}/groups/$uid")
        dbRef_user.removeValue().addOnSuccessListener {
            userInEventRef.removeValue()
                .addOnSuccessListener {
                    groupRef.removeValue().addOnSuccessListener {
                        curPeople.setValue(eventHelpVM.amountRegPeople.value?.minus(1)).addOnSuccessListener {
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
            0 -> ChoosePeopleFragment(eventHelpVM.id.value.toString(), "help", requireView())
            else -> DialogFragment()
        }
        dialog.show(childFragmentManager, "PeopleGoDialog")
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

    private fun loadAllImages(callback: (Boolean) -> Unit) {
        if(eventHelpVM.creatorUsername.value == "Удаленный аккаунт") {
            binding.eventAvatar.setImageResource(R.drawable.block_user)
            callback(true)
        } else {
            if(isAdded) {
                val url = eventHelpVM.photos.value?.get(0).toString().removeSurrounding("[", "]")

                val userAvatarRef = storageRef.child("avatars/${eventHelpVM.creator.value}")
                val tokenTask2 = userAvatarRef.downloadUrl
                lifecycleScope.launch {
                    val bitmap: Bitmap = withContext(Dispatchers.IO) {
                        Coil.imageLoader(requireContext()).execute(
                            ImageRequest.Builder(requireContext())
                                .data(url)
                                .build()
                        ).drawable?.toBitmap()!!
                    }
                    binding.image.setImageBitmap(bitmap)
                    tokenTask2.addOnSuccessListener { url_2 ->
                        lifecycleScope.launch {
                            val bitmap: Bitmap = withContext(Dispatchers.IO) {
                                Coil.imageLoader(requireContext()).execute(
                                    ImageRequest.Builder(requireContext())
                                        .data(url_2)
                                        .build()
                                ).drawable?.toBitmap()!!
                            }
                            binding.eventAvatar.setImageBitmap(bitmap)
                        }
                        callback(true)
                    }.addOnFailureListener {

                    }

                }
            }
        }
    }

    private fun checkIfUserAlreadyFriend(callback: (ready: String) -> Unit) {
        val dbRef_user_friends = FirebaseDatabase.getInstance().getReference("users/${auth.currentUser!!.uid}/friends")
        val dbRef_user_query_friends_to_you = FirebaseDatabase.getInstance().getReference("users/${auth.currentUser!!.uid}/query_friends")
        val dbRef_user_query_friends_to_creator = FirebaseDatabase.getInstance().getReference("users/${eventHelpVM.creator.value.toString()}/query_friends")

        var reg = "not"

        dbRef_user_friends.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for(user in snapshot.children) {
                    if(user.value.toString() == eventHelpVM.creator.value.toString()) {
                        reg = "friend"
                        break
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                reg = "error"
            }

        })
        if(reg != "friend") {
            dbRef_user_query_friends_to_you.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (user in snapshot.children) {
                        if (user.value == auth.currentUser!!.uid) {
                            reg = "request_to_you"
                            break
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    reg = "error"
                }

            })
        }

        if(reg != "friend" && reg != "request_to_you") {
            dbRef_user_query_friends_to_creator.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (user in snapshot.children) {
                        if (user.value == auth.currentUser!!.uid) {
                            reg = "request_to_creator"
                            break
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    reg = "error"
                }

            })
        }
        callback(reg)
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
                        val maxPeople = eventSnapshot.child("peopleNeed").value.toString().toInt()
                        val description = eventSnapshot.child("description").value.toString()
                        val creatorUid = eventSnapshot.child("creator_id").value.toString()
                        val photos = arrayListOf(eventSnapshot.child("photos").value.toString())
                        val freePlaces = maxPeople - eventSnapshot.child("people_want_id").childrenCount.toString().toInt()
                        val timeOfCreation = eventSnapshot.child("time_of_creation").value.toString()
                        val dateOfMeeting = eventSnapshot.child("date_of_meeting").value.toString()
                        val amountRegPeople = eventSnapshot.child("people_want_id").childrenCount.toString().toInt()
                        val price = eventSnapshot.child("price").value.toString().toInt()

                        when(type) {
                            "help" -> {
                                checkIfUserAlreadyFriend { ready ->
                                    var found: Boolean = false
                                    dbRefUser.addValueEventListener(object : ValueEventListener {
                                        override fun onDataChange(userSnapshot: DataSnapshot) {
                                            for (userEventSnapshot in userSnapshot.children) {
                                                if (creatorUid == userEventSnapshot.child("uid").value) {
                                                    val creatorUsername =
                                                        userEventSnapshot.child("username").value.toString()
                                                    eventHelpVM.setInfo(
                                                        id,
                                                        maxPeople,
                                                        price,
                                                        creatorUid,
                                                        creatorUsername,
                                                        photos,
                                                        freePlaces,
                                                        description,
                                                        timeOfCreation,
                                                        dateOfMeeting,
                                                        amountRegPeople,
                                                        ready
                                                    )
                                                    Log.d("INFOG", ready)
                                                    callback(true)
                                                    found = true
                                                    return
                                                }
                                            }

                                            if (found == false) {
                                                eventHelpVM.setInfo(
                                                    id,
                                                    maxPeople,
                                                    price,
                                                    creatorUid,
                                                    "Удаленный аккаунт",
                                                    photos,
                                                    freePlaces,
                                                    description,
                                                    timeOfCreation,
                                                    dateOfMeeting,
                                                    amountRegPeople,
                                                    ready
                                                )
                                                callback(true)
                                            }
                                        }

                                        override fun onCancelled(error: DatabaseError) {
                                            callback(false)
                                        }
                                    })
                                }
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

    private fun checkIfUserAlreadyReg(curUid: String, eventId: String, callback: (Boolean) -> Unit) {
        try {
            dbRef_event = FirebaseDatabase.getInstance().getReference("current_events")
            dbRef_event.child(eventId).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val regPeopleSnapshot = dataSnapshot.child("people_want_id")
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
    private fun regUserToEvent(curUid: String, callback: (Boolean) -> Unit) {
        try {
            val dbRef_users = FirebaseDatabase.getInstance().getReference("users/${auth.currentUser!!.uid}")
            val dbRef_event = FirebaseDatabase.getInstance().getReference("current_events")
            val dbRef_groups = FirebaseDatabase.getInstance().getReference("groups/${eventHelpVM.id.value}/members")
            val event_id = eventHelpVM.id.value.toString()

            dbRef_event.child(event_id).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if(dataSnapshot.exists()) {
                        val max = dataSnapshot.child("peopleNeed").getValue(Int::class.java) ?: 0
                        val reg_people = dataSnapshot.child("people_want_id").childrenCount

                        if (max - reg_people > 0) {
                            val regPeopleRef = dbRef_event.child(event_id).child("people_want_id").child(auth.currentUser!!.uid)

                            regPeopleRef.setValue(curUid)
                                .addOnSuccessListener {
                                    dbRef_users.child("curRegEventsId").child(event_id).setValue(event_id)
                                        .addOnSuccessListener {
                                            dbRef_groups.child(auth.currentUser!!.uid).setValue(auth.currentUser!!.uid).addOnSuccessListener {
                                                dbRef_users.child("groups").child(eventHelpVM.id.value.toString()).setValue(eventHelpVM.id.value.toString()).addOnSuccessListener {
                                                    binding.btnRegToEvent.visibility = View.GONE
                                                    binding.deleteOrLeave.visibility = View.VISIBLE
                                                }
                                            }

                                            callback(true)
                                        }
                                        .addOnFailureListener { e ->
                                            callback(false)
                                            Log.e("FirebaseError", "Ошибка Firebase при записи ID события в curRegEventsId пользователя: ${e.message}")
                                        }
                                }
                                .addOnFailureListener { e ->
                                    callback(false)
                                    Log.e("FirebaseError", "Ошибка Firebase при обновлении счетчика участников: ${e.message}")
                                }
                        } else {
                            callback(false)
                            Log.e("FirebaseError", "Достигнуто максимальное количество участников")
                        }
                    } else {
                        callback(false)
                        Log.e("FirebaseError", "Мероприятие с ID $event_id не найдено")
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    callback(false)
                    Log.e("FirebaseError", "Ошибка Firebase ${databaseError.message}")
                }
            })
        } catch (e: Exception) {
            Log.e("INFOG", e.message.toString())
        }
    }

}