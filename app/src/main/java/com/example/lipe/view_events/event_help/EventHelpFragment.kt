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
import coil.load
import coil.request.ImageRequest
import com.example.lipe.R
import com.example.lipe.choose_people.ChoosePeopleFragment
import com.example.lipe.databinding.FragmentEventHelpBinding
import com.example.lipe.people_go_to_event.PeopleGoToEventFragment
import com.example.lipe.reports.report_dialog.EventReportFragment
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
                            if (ans) {
                                val date_ = eventHelpVM.date.value
                                binding.dateOfMeetingHelp.text = date_

                                binding.btnRegToEvent.visibility = View.INVISIBLE

                                Log.d("INFOG", eventHelpVM.creator.value.toString())

                                if(eventHelpVM.creator.value == user) {
                                    binding.deleteOrLeave.visibility = View.VISIBLE
                                    binding.deleteOrLeave.text = getString(R.string.finish)

                                    binding.listUsers.text = getString(R.string.list)
                                    binding.listUsers.visibility = View.VISIBLE

                                    binding.report.visibility = View.INVISIBLE
                                } else {
                                    binding.deleteOrLeave.text = getString(R.string.leave)
                                    binding.deleteOrLeave.visibility = View.VISIBLE

                                    binding.listUsers.text = getString(R.string.list)
                                    binding.listUsers.visibility = View.VISIBLE

                                    binding.report.visibility = View.GONE
                                }

                                binding.allHelpEvent.visibility = View.VISIBLE
                                binding.loadingProgressBar.visibility = View.GONE
                            } else {
                                if(eventHelpVM.creator.value == user) {
                                    binding.deleteOrLeave.visibility = View.VISIBLE
                                    binding.deleteOrLeave.text = getString(R.string.finish)

                                    binding.listUsers.text = getString(R.string.list)
                                    binding.listUsers.visibility = View.VISIBLE

                                    binding.btnRegToEvent.visibility = View.INVISIBLE

                                    val date_ = eventHelpVM.date.value
                                    binding.dateOfMeetingHelp.text = date_

                                    binding.allHelpEvent.visibility = View.VISIBLE
                                    binding.loadingProgressBar.visibility = View.GONE

                                    binding.report.visibility = View.INVISIBLE
                                } else {
                                    binding.allHelpEvent.visibility = View.VISIBLE
                                    binding.loadingProgressBar.visibility = View.GONE
                                    binding.report.visibility = View.VISIBLE

                                    binding.listUsers.visibility = View.GONE

                                    binding.dateOfMeetingHelp.text = "*******"
                                }
                            }
                        }
                    }
                }
            }
        }

        binding.allHelpEvent.visibility = View.GONE
        binding.loadingProgressBar.visibility = View.VISIBLE

        binding.apply {
            lifecycleOwner = viewLifecycleOwner
            viewModel = eventHelpVM

            deleteOrLeave.setOnClickListener {
                if(auth.currentUser!!.uid == eventHelpVM.creator.value) {
                    showPeopleGoDialog(1)
                } else {
                    binding.dateOfMeetingHelp.text = "*******"
                    deleteUserFromEvent(eventHelpVM.id.value.toString())

                    binding.deleteOrLeave.visibility = View.GONE
                    binding.listUsers.visibility = View.GONE

                    binding.btnRegToEvent.visibility = View.VISIBLE
                }
            }

            binding.listUsers.setOnClickListener {
                showPeopleGoDialog(0)
            }

            binding.report.setOnClickListener {
                val reportDialog = EventReportFragment(eventHelpVM.id.value.toString(), auth.currentUser!!.uid)
                reportDialog.show(childFragmentManager, "EventReport")
            }


            btnRegToEvent.setOnClickListener {
                val curUid = auth.currentUser?.uid
                if (curUid != null) {
                    checkIfUserAlreadyReg(curUid, eventHelpVM.id.value!!) { isUserAlreadyRegistered ->
                        if (!isUserAlreadyRegistered) {
                            regUserToEvent(curUid) { result ->
                                if (result == true) {
                                    val date_ = eventHelpVM.date.value
                                    if(date_ != null) {
                                        binding.dateOfMeetingHelp.setText(
                                            date_
                                        )
                                    }

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
        dbRef_user.removeValue().addOnSuccessListener {
            userInEventRef.removeValue()
                .addOnSuccessListener {
                    binding.deleteOrLeave.visibility = View.GONE
                    binding.btnRegToEvent.visibility = View.VISIBLE
                }
                .addOnFailureListener {
                    Log.e("INFOG", "ErLeaveEvent")
                }
        }
    }

    private fun showPeopleGoDialog(lay: Int) {
        var dialog: DialogFragment ?= null

        dialog = when(lay) {
            0 -> PeopleGoToEventFragment(eventHelpVM.id.value.toString())
            1 -> ChoosePeopleFragment(eventHelpVM.id.value.toString(), "help", requireView())
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

                    callback(true)

                    binding.image.load(url)

                    tokenTask2.addOnSuccessListener { url_2 ->
                        lifecycleScope.launch {
                            binding.eventAvatar.load(url_2)
                        }
                    }.addOnFailureListener {

                    }

                }
            }
        }
    }

    private fun checkIfUserAlreadyFriend(creatorUid: String, callback: (ready: String) -> Unit) {
        if(creatorUid == auth.currentUser!!.uid) {
            callback("you")
        } else {
            val dbRef_user_friends = FirebaseDatabase.getInstance()
                .getReference("users/${auth.currentUser!!.uid}/friends")

            var reg = "not"

            dbRef_user_friends.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (user in snapshot.children) {
                        if (user.value.toString() == creatorUid) {
                            callback("friend")
                            reg = "friend"
                            return
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    reg = "error"
                }

            })
            callback(reg)
        }
    }

    private fun searchEvent(coord1: Double, coord2: Double, callback: (ready: Boolean) -> Unit) {
        if (isAdded && context != null) {
            val dbRefEvent = FirebaseDatabase.getInstance().getReference("current_events")
            val dbRefUser = FirebaseDatabase.getInstance().getReference("users")

            val eventListener = object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (!isAdded || context == null) return

                    for (eventSnapshot in dataSnapshot.children) {
                        val coordinates: List<Double>? = listOf(
                            eventSnapshot.child("coordinates").child("latitude").value.toString().toDouble(),
                            eventSnapshot.child("coordinates").child("longitude").value.toString().toDouble()
                        )

                        if (coordinates != null && coordinates[0] == coord1 && coordinates[1] == coord2) {
                            val type = eventSnapshot.child("type_of_event").value.toString()
                            val id = eventSnapshot.child("event_id").value.toString()
                            val maxPeople = eventSnapshot.child("peopleNeed").value.toString().toInt()
                            val description = eventSnapshot.child("description").value.toString()
                            val creatorUid = eventSnapshot.child("creator_id").value.toString()
                            val photos = arrayListOf(eventSnapshot.child("photos").value.toString())
                            val timeOfCreation = eventSnapshot.child("time_of_creation").value.toString()
                            val dateOfMeeting = eventSnapshot.child("date_of_meeting").value.toString()
                            val price = eventSnapshot.child("price").value.toString().toInt()

                            val freePlaces = 100 - 100 * eventSnapshot.child("reg_people_id").childrenCount.toString().toInt() / maxPeople

                            Log.i("INFOG", maxPeople.toString() + " " + eventSnapshot.child("reg_people_id").childrenCount.toString())

                            if (type == "help") {
                                checkIfUserAlreadyFriend(creatorUid) { friend ->
                                    if (!isAdded || context == null) return@checkIfUserAlreadyFriend

                                    val friend_tot = when (friend) {
                                        "you" -> getString(R.string.you)
                                        "friend" -> getString(R.string.your_friend)
                                        "not" -> getString(R.string.not_friend)
                                        else -> "-"
                                    }

                                    var found = false
                                    val userListener = object : ValueEventListener {
                                        override fun onDataChange(userSnapshot: DataSnapshot) {
                                            if (!isAdded || context == null) return

                                            for (userEventSnapshot in userSnapshot.children) {
                                                if (creatorUid == userEventSnapshot.child("uid").value) {
                                                    val creatorUsername = userEventSnapshot.child("username").value.toString()
                                                    eventHelpVM.setInfo(
                                                        id, price, creatorUid, creatorUsername, photos, freePlaces,
                                                        description, timeOfCreation.toLong(), dateOfMeeting, friend_tot
                                                    )
                                                    callback(true)
                                                    found = true
                                                    return
                                                }
                                            }

                                            if (!found) {
                                                eventHelpVM.setInfo(
                                                    id, price, creatorUid, "Удаленный аккаунт", photos, freePlaces,
                                                    description, timeOfCreation.toLong(), dateOfMeeting, friend_tot
                                                )
                                                callback(true)
                                            }
                                        }

                                        override fun onCancelled(error: DatabaseError) {
                                            callback(false)
                                        }
                                    }

                                    dbRefUser.addValueEventListener(userListener)
                                }
                            }
                            break
                        } else {
                            binding.listUsers.visibility = View.GONE
                            binding.report.visibility = View.GONE
                            binding.deleteOrLeave.visibility = View.GONE
                        }
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.e("FirebaseError", "Ошибка Firebase ${databaseError.message}")
                }
            }

            dbRefEvent.addValueEventListener(eventListener)
        }
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
    private fun regUserToEvent(curUid: String, callback: (Boolean) -> Unit) {
        try {
            val dbRef_users = FirebaseDatabase.getInstance().getReference("users/${auth.currentUser!!.uid}")
            val dbRef_event = FirebaseDatabase.getInstance().getReference("current_events")
            val event_id = eventHelpVM.id.value.toString()

            dbRef_event.child(event_id).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if(dataSnapshot.exists()) {
                        val max = dataSnapshot.child("peopleNeed").getValue(Int::class.java) ?: 0
                        val reg_people = dataSnapshot.child("reg_people_id").childrenCount

                        if (max - reg_people > 0) {
                            val regPeopleRef = dbRef_event.child(event_id).child("reg_people_id").child(auth.currentUser!!.uid)

                            regPeopleRef.setValue(curUid)
                                .addOnSuccessListener {
                                    dbRef_users.child("curRegEventsId").child(event_id).setValue(event_id)
                                        .addOnSuccessListener {
                                            binding.btnRegToEvent.visibility = View.GONE
                                            binding.deleteOrLeave.visibility = View.VISIBLE

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