package com.example.lipe.view_events.event_ent

import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import coil.Coil
import coil.request.ImageRequest
import com.example.lipe.reports.report_dialog.EventReportFragment
import com.example.lipe.R
import com.example.lipe.choose_people.ChoosePeopleFragment
import com.example.lipe.databinding.FragmentEventEntBinding
import com.example.lipe.people_go_to_event.PeopleGoToEventFragment
import com.example.lipe.viewModels.AppVM
import com.example.lipe.viewModels.EventEntVM
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

class EventEntFragment : Fragment() {

    private lateinit var auth: FirebaseAuth

    private lateinit var dbrefEvent1: DatabaseReference

    private lateinit var storageRef : StorageReference

    private val appVM: AppVM by activityViewModels()

    private lateinit var binding: FragmentEventEntBinding

    private val eventEntVM: EventEntVM by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentEventEntBinding.inflate(inflater, container, false)

        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()

        storageRef = FirebaseStorage.getInstance().reference

        dbrefEvent1 = FirebaseDatabase.getInstance().getReference("current_events")

        binding.report.setOnClickListener {
            val reportDialog = EventReportFragment(eventEntVM.id.value.toString(), auth.currentUser!!.uid)
            reportDialog.show(childFragmentManager, "EventReport")
        }

        searchEvent(appVM.latitude, appVM.longtitude) { ready ->
            val sportType = appVM.type_sport
            val imageSport = when (sportType) {
                "Basketball" -> R.drawable.img_basketballimg
                "Volleyball" -> R.drawable.volleyball_2
                "Football" -> R.drawable.football
                "Rugby" -> R.drawable.rugby_ball
                "Workout" -> R.drawable.weights
                "Tennis" -> R.drawable.tennis
                "Badminton" -> R.drawable.shuttlecock
                "Table tennis" -> R.drawable.table_tennis
                "Gymnastics" -> R.drawable.gymnastic_rings
                "Fencing" -> R.drawable.fencing
                "Jogging" -> R.drawable.running_shoe
                "Curling" -> R.drawable.curling
                "Hockey" -> R.drawable.ice_hockey
                "Ice skating" -> R.drawable.ice_skate
                "Skiing" -> R.drawable.skiing_1
                "Downhill skiing" -> R.drawable.skiing
                "Snowboarding" -> R.drawable.snowboarding
                "Table games" -> R.drawable.board_game
                "Mobile games" -> R.drawable.mobile_game
                "Chess" -> R.drawable.chess_2
                "Programming" -> R.drawable.programming
                else -> 0
            }
            val user = auth.currentUser!!.uid
            binding.typeSport.setImageResource(imageSport)
            if (ready) {
                loadAllImages { ready ->
                    if (ready) {
                        checkIfUserAlreadyReg(
                            auth.currentUser!!.uid,
                            eventEntVM.id.value.toString()
                        ) { ans ->
                            if (ans) {
                                val date_ = eventEntVM.date.value
                                binding.dateOfMeetingEnt.text = date_

                                binding.btnRegToEvent.visibility = View.INVISIBLE

                                if (eventEntVM.creator.value == user) {
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
                                    binding.report.visibility = View.INVISIBLE
                                }

                                binding.allEntEvent.visibility = View.VISIBLE
                                binding.loadingProgressBar.visibility = View.GONE
                            } else {
                                binding.allEntEvent.visibility = View.VISIBLE
                                binding.loadingProgressBar.visibility = View.GONE

                                binding.dateOfMeetingEnt.text = "*******"
                            }
                        }
                    }
                }
            }
        }

        binding.allEntEvent.visibility = View.GONE
        binding.loadingProgressBar.visibility = View.VISIBLE

        binding.creator.setOnClickListener {

        }

        binding.apply {
            lifecycleOwner = viewLifecycleOwner
            viewModel = eventEntVM

            listUsers.setOnClickListener {
                showPeopleGoDialog(0)
            }

            deleteOrLeave.setOnClickListener {
                if (auth.currentUser!!.uid == eventEntVM.creator.value) {
                    showPeopleGoDialog(1)
                } else {
                    binding.dateOfMeetingEnt.text = "*******"
                    deleteUserFromEvent(eventEntVM.id.value.toString())

                    binding.deleteOrLeave.visibility = View.GONE
                    binding.listUsers.visibility = View.GONE

                    binding.btnRegToEvent.visibility = View.VISIBLE
                }
            }


            btnRegToEvent.setOnClickListener {
                val curUid = auth.currentUser?.uid
                if (curUid != null) {
                    checkIfUserAlreadyReg(curUid, eventEntVM.id.value!!) { isUserAlreadyRegistered ->
                        if (!isUserAlreadyRegistered) {
                            regUserToEvent(curUid) { result ->
                                if (result == true) {
                                    val date_ = eventEntVM.date.value
                                    if(date_ != null) {
                                        binding.dateOfMeetingEnt.setText(
                                            buildString {
                                                append(
                                                    date_.substring(
                                                        6,
                                                        date_.length
                                                    )
                                                )
                                                append(getString(R.string.`in`))
                                                append(date_.substring(0, 5))
                                            }
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


    private fun showPeopleGoDialog(lay: Int) {
        var dialog: DialogFragment ?= null

        dialog = when(lay) {
            0 -> PeopleGoToEventFragment(eventEntVM.id.value.toString())
            1 -> ChoosePeopleFragment(eventEntVM.id.value.toString(), "ent")
            else -> DialogFragment()
        }
        dialog.show(childFragmentManager, "PeopleGoDialog")
    }

    private fun sendFriendRequest() {
        val dbrefUserQueryFriends = FirebaseDatabase.getInstance().getReference("users/${eventEntVM.creator.value.toString()}/query_friends")
        dbrefUserQueryFriends.child(auth.currentUser!!.uid).setValue(auth.currentUser!!.uid)
    }

    private fun checkIfUserAlreadyFriend(callback: (ready: String) -> Unit) {
        val dbrefUserFriends = FirebaseDatabase.getInstance().getReference("users/${auth.currentUser!!.uid}/friends")
        val dbrefUserQueryFriendsToYou = FirebaseDatabase.getInstance().getReference("users/${auth.currentUser!!.uid}/query_friends")
        val dbrefUserQueryFriendsToCreator = FirebaseDatabase.getInstance().getReference("users/${eventEntVM.creator.value.toString()}/query_friends")

        var reg = "not"

        dbrefUserFriends.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
               for(user in snapshot.children) {
                   if(user.value.toString() == eventEntVM.creator.value.toString()) {
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
            dbrefUserQueryFriendsToYou.addValueEventListener(object : ValueEventListener {
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
            dbrefUserQueryFriendsToCreator.addValueEventListener(object : ValueEventListener {
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


    private fun loadAllImages(callback: (Boolean) -> Unit) {
        if(eventEntVM.creatorUsername.value == "Удаленный аккаунт") {
            binding.eventAvatar.setImageResource(R.drawable.block_user)
            callback(true)
        } else {
            if(isAdded) {
                val url = eventEntVM.photos.value?.get(0).toString().removeSurrounding("[", "]")

                val userAvatarRef = storageRef.child("avatars/${eventEntVM.creator.value}")
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

    private fun searchEvent(coord1: Double, coord2: Double, callback: (ready: Boolean) -> Unit) {
        if (!isAdded || context == null) return

        val dbRefEvent = FirebaseDatabase.getInstance().getReference("current_events")
        val dbRefUser = FirebaseDatabase.getInstance().getReference("users")

        dbRefEvent.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (!isAdded || context == null) return

                for (eventSnapshot in dataSnapshot.children) {
                    val coordinates = listOfNotNull(
                        eventSnapshot.child("coordinates").child("latitude").value?.toString()?.toDoubleOrNull(),
                        eventSnapshot.child("coordinates").child("longitude").value?.toString()?.toDoubleOrNull()
                    )

                    if (coordinates.isNotEmpty() && coordinates[0] == coord1 && coordinates[1] == coord2) {
                        val type = eventSnapshot.child("type_of_event").value.toString()
                        val id = eventSnapshot.child("event_id").value.toString()
                        val maxPeople = eventSnapshot.child("max_people").value.toString().toIntOrNull() ?: 0
                        val title = eventSnapshot.child("title").value.toString()
                        val description = eventSnapshot.child("description").value.toString()
                        val creatorUid = eventSnapshot.child("creator_id").value.toString()
                        val photos = arrayListOf(eventSnapshot.child("photos").value.toString())
                        val freePlaces = maxPeople - (eventSnapshot.child("amount_reg_people").value.toString().toIntOrNull() ?: 0)
                        val timeOfCreation = eventSnapshot.child("time_of_creation").value.toString().toLongOrNull() ?: 0L
                        val dateOfMeeting = eventSnapshot.child("date_of_meeting").value.toString()
                        val amountRegPeople = eventSnapshot.child("amount_reg_people").value.toString().toIntOrNull() ?: 0
                        val age = eventSnapshot.child("age").value.toString()
                        val ageLang = when (age) {
                            "any_age" -> getString(R.string.any_age)
                            "more_18" -> getString(R.string.more_18)
                            "before_18" -> getString(R.string.before_18)
                            else -> ""
                        }

                        if (type == "ent") {
                            val sportType = eventSnapshot.child("sport_type").value.toString()
                            val langSportType = when (sportType) {
                                "Basketball" -> getString(R.string.basketball)
                                "Volleyball" -> getString(R.string.volleyball)
                                "Football" -> getString(R.string.football)
                                "Rugby" -> getString(R.string.rugby)
                                "Workout" -> getString(R.string.workout)
                                "Tennis" -> getString(R.string.tennis)
                                "Badminton" -> getString(R.string.badminton)
                                "Table tennis" -> getString(R.string.table_tennis)
                                "Gymnastics" -> getString(R.string.gymnastics)
                                "Fencing" -> getString(R.string.fencing)
                                "Jogging" -> getString(R.string.jogging)
                                "Curling" -> getString(R.string.curling)
                                "Hockey" -> getString(R.string.hockey)
                                "Ice skating" -> getString(R.string.ice_skating)
                                "Skiing" -> getString(R.string.skiing)
                                "Downhill skiing" -> getString(R.string.downhill_skiing)
                                "Snowboarding" -> getString(R.string.snowboarding)
                                "Table games" -> getString(R.string.table_games)
                                "Mobile games" -> getString(R.string.mobile_games)
                                "Chess" -> getString(R.string.chess)
                                "Programming" -> getString(R.string.programming)
                                else -> "0"
                            }

                            checkIfUserAlreadyFriend { ready ->
                                if (!isAdded || context == null) return@checkIfUserAlreadyFriend

                                var found = false
                                dbRefUser.addValueEventListener(object : ValueEventListener {
                                    override fun onDataChange(userSnapshot: DataSnapshot) {
                                        if (!isAdded || context == null) return

                                        for (userEventSnapshot in userSnapshot.children) {
                                            if (creatorUid == userEventSnapshot.child("uid").value) {
                                                val creatorUsername = userEventSnapshot.child("username").value.toString()
                                                eventEntVM.setInfo(
                                                    id, maxPeople, title, creatorUid, creatorUsername, photos,
                                                    arrayListOf("1"), freePlaces, ageLang, description, timeOfCreation,
                                                    dateOfMeeting, sportType, langSportType, amountRegPeople, ready
                                                )
                                                callback(true)
                                                found = true
                                                break
                                            }
                                        }

                                        if (!found) {
                                            eventEntVM.setInfo(
                                                id, maxPeople, title, creatorUid, getString(R.string.deleted_ac), photos,
                                                arrayListOf("1"), freePlaces, age, description, timeOfCreation,
                                                dateOfMeeting, sportType, langSportType, amountRegPeople, ready
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
                        break
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("FirebaseError", "Ошибка Firebase ${databaseError.message}")
            }
        })
    }


    fun deleteUserFromEvent(uid: String) {
        val dbRef_user = FirebaseDatabase.getInstance().getReference("users").child(auth.currentUser!!.uid).child("curRegEventsId").child(uid)
        val userInEventRef = dbrefEvent1.child(eventEntVM.id.value.toString()).child("reg_people_id").child(auth.currentUser!!.uid)
        val curPeople = dbrefEvent1.child(eventEntVM.id.value.toString()).child("amount_reg_people")
        val groupRef = FirebaseDatabase.getInstance().getReference("groups/${eventEntVM.id.value}/members/${auth.currentUser!!.uid}")
        val groupInProfile = FirebaseDatabase.getInstance().getReference("users/${auth.currentUser!!.uid}/groups/$uid")
        dbRef_user.removeValue().addOnSuccessListener {
            userInEventRef.removeValue()
                .addOnSuccessListener {
                    groupRef.removeValue().addOnSuccessListener {
                        curPeople.setValue(eventEntVM.amountRegPeople.value?.minus(1)).addOnSuccessListener {
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

    private fun setDialog(title: String, desc: String, btnText: String) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(title)
            .setMessage(desc)
            .setPositiveButton(btnText) { dialog, which ->
                dialog.dismiss()
            }
            .show()
    }


    private fun checkIfUserAlreadyReg(curUid: String, eventId: String, callback: (Boolean) -> Unit) {
        try {
            dbrefEvent1 = FirebaseDatabase.getInstance().getReference("current_events")
            dbrefEvent1.child(eventId).addListenerForSingleValueEvent(object : ValueEventListener {
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
            val dbrefUsers = FirebaseDatabase.getInstance().getReference("users/${auth.currentUser!!.uid}")
            val dbrefEvent = FirebaseDatabase.getInstance().getReference("current_events")
            val dbrefGroups = FirebaseDatabase.getInstance().getReference("groups/${eventEntVM.id.value}/members")
            val eventId = eventEntVM.id.value.toString()

            dbrefEvent.child(eventId).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if(dataSnapshot.exists()) {
                        val max = dataSnapshot.child("max_people").getValue(Int::class.java) ?: 0
                        val reg_people = dataSnapshot.child("amount_reg_people").getValue(Int::class.java) ?: 0

                        if (max - reg_people > 0) {
                            val regPeopleRef = dbrefEvent.child(eventId).child("reg_people_id").child(auth.currentUser!!.uid)

                            regPeopleRef.setValue(curUid)
                                .addOnSuccessListener {
                                    dbrefEvent.child(eventId).child("amount_reg_people").setValue(reg_people + 1)
                                        .addOnSuccessListener {
                                            dbrefUsers.child("curRegEventsId").child(eventId).setValue(eventId)
                                                .addOnSuccessListener {
                                                    dbrefGroups.child(auth.currentUser!!.uid).setValue(auth.currentUser!!.uid).addOnSuccessListener {
                                                        dbrefUsers.child("groups").child(eventEntVM.id.value.toString()).setValue(eventEntVM.id.value.toString()).addOnSuccessListener {
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
                                }
                                .addOnFailureListener { e ->
                                    callback(false)
                                    Log.e("FirebaseError", "Ошибка Firebase при добавлении пользователя к событию: ${e.message}")
                                }
                        } else {
                            callback(false)
                            Log.e("FirebaseError", "Достигнуто максимальное количество участников")
                        }
                    } else {
                        callback(false)
                        Log.e("FirebaseError", "Мероприятие с ID $eventId не найдено")
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