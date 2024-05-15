package com.example.lipe.view_events.event_ent

import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import coil.Coil
import coil.request.ImageRequest
import com.example.lipe.MapsFragment
import com.example.lipe.R
import com.example.lipe.all_profiles.other_profile.OtherProfileFragment
import com.example.lipe.choose_people.ChoosePeopleFragment
import com.example.lipe.databinding.FragmentEventEntBinding
import com.example.lipe.people_go_to_event.PeopleGoToEventFragment
import com.example.lipe.viewModels.AppVM
import com.example.lipe.viewModels.EventEntVM
import com.example.lipe.view_events.EventFragment
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.GenericTypeIndicator
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.getValue
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExecutorCoroutineDispatcher
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EventEntFragment : Fragment() {
    private lateinit var auth: FirebaseAuth

    private lateinit var dbRef: DatabaseReference

    private lateinit var dbRef_event: DatabaseReference

    private lateinit var dbRef_user: DatabaseReference

    private lateinit var storageRef : StorageReference

    private val appVM: AppVM by activityViewModels()
    private val binding get() = _binding!!

    private var _binding: FragmentEventEntBinding? = null

    private val eventEntVM: EventEntVM by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentEventEntBinding.inflate(inflater, container, false)

        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()

        storageRef = FirebaseStorage.getInstance().reference

        dbRef_event = FirebaseDatabase.getInstance().getReference("current_events")

        searchEvent(appVM.latitude, appVM.longtitude) { ready ->
            val sportType = appVM.type_sport
            val imageSport = when (sportType) {
                "Баскетбол" -> R.drawable.img_basketballimg
                "Воллейбол" -> R.drawable.volleyball_2
                "Футбол" -> R.drawable.football
                "Рэгби" -> R.drawable.rugby_ball
                "Воркаут" -> R.drawable.weights
                "Большой тенис" -> R.drawable.tennis
                "Бадминтон" -> R.drawable.shuttlecock
                "Пинпонг" -> R.drawable.table_tennis
                "Гимнастика" -> R.drawable.gymnastic_rings
                "Фехтование" -> R.drawable.fencing
                "Бег" -> R.drawable.running_shoe
                "Кёрлинг" -> R.drawable.curling
                "Хоккей" -> R.drawable.ice_hockey
                "Катание на коньках" -> R.drawable.ice_skate
                "Лыжная ходьба" -> R.drawable.skiing_1
                "Горные лыжи" -> R.drawable.skiing
                "Теннис" -> R.drawable.tennis
                "Сноуборд" -> R.drawable.snowboarding
                "Настольные игры" -> R.drawable.board_game
                "Мобильные игры" -> R.drawable.mobile_game
                "Шахматы" -> R.drawable.chess_2
                "Программирование" -> R.drawable.programming
                else -> 0
            }
            val user = auth.currentUser!!.uid
            binding?.typeSport?.setImageResource(imageSport)
            if (ready) {
                loadAllImages { ready ->
                    if (ready) {
                        checkIfUserAlreadyReg(
                            auth.currentUser!!.uid,
                            eventEntVM.id.value.toString()
                        ) { ans ->
                            if (ans) {
                                binding?.btnRegToEvent?.visibility = View.GONE

                                if (eventEntVM.creator.value == user) {
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

                                binding?.allEntEvent?.visibility = View.VISIBLE
                                binding?.loadingProgressBar?.visibility = View.GONE
                            } else {
                                binding?.allEntEvent?.visibility = View.VISIBLE
                                binding?.loadingProgressBar?.visibility = View.GONE
                            }
                        }
                    }
                }
            }
        }

        binding?.allEntEvent?.visibility = View.GONE
        binding?.loadingProgressBar?.visibility = View.VISIBLE

        binding?.creator?.setOnClickListener {

        }

        binding?.apply {
            lifecycleOwner = viewLifecycleOwner
            viewModel = eventEntVM

            listUsers?.setOnClickListener {
                showPeopleGoDialog(0)
            }

            deleteOrLeave?.setOnClickListener {
                if (auth.currentUser!!.uid == eventEntVM.creator.value) {
                    showPeopleGoDialog(1)
                    //deleteEvent(eventEntVM.id.value.toString())
                } else {
                    deleteUserFromEvent(eventEntVM.id.value.toString())

                    binding?.deleteOrLeave?.visibility = View.GONE
                    binding?.listUsers?.visibility = View.GONE

                    binding?.btnRegToEvent?.visibility = View.VISIBLE
                }
            }


            btnRegToEvent?.setOnClickListener {
                val curUid = auth.currentUser?.uid
                if (curUid != null) {
                    checkIfUserAlreadyReg(curUid, eventEntVM.id.value!!) { isUserAlreadyRegistered ->
                        if (!isUserAlreadyRegistered) {
                            regUserToEvent(curUid) { result ->
                                if (result == true) {
                                    setDialog(
                                        "Успешная регистрация",
                                        "Поздравляем, регистрация на событие прошла успешно",
                                        "Отлично!"
                                    )
                                } else {
                                    //fail
                                    setDialog(
                                        "Ошибка при регистрации",
                                        "Что-то пошло не так, попробуйте зарегистрироваться еще раз",
                                        "Хорошо"
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
            0 -> PeopleGoToEventFragment()
            1 -> ChoosePeopleFragment()
            else -> DialogFragment()
        }
        dialog.show(childFragmentManager, "PeopleGoDialog")
    }

    private fun sendFriendRequest() {
        val dbRef_user_query_friends = FirebaseDatabase.getInstance().getReference("users/${eventEntVM.creator.value.toString()}/query_friends")
        dbRef_user_query_friends.child(auth.currentUser!!.uid).setValue(auth.currentUser!!.uid)
    }

    private fun checkIfUserAlreadyFriend(callback: (ready: String) -> Unit) {
        val dbRef_user_friends = FirebaseDatabase.getInstance().getReference("users/${auth.currentUser!!.uid}/friends")
        val dbRef_user_query_friends_to_you = FirebaseDatabase.getInstance().getReference("users/${auth.currentUser!!.uid}/query_friends")
        val dbRef_user_query_friends_to_creator = FirebaseDatabase.getInstance().getReference("users/${eventEntVM.creator.value.toString()}/query_friends")

        var reg = "not"

        dbRef_user_friends.addValueEventListener(object: ValueEventListener {
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

//    private fun replaceFragment(fragment: Fragment) {
//        val fragmentManager = childFragmentManager
//        val fragmentTransaction = fragmentManager.beginTransaction()
//        fragmentTransaction.replace(R.id., fragment)
//        fragmentTransaction.commit()
//    }


    private fun loadAllImages(callback: (Boolean) -> Unit) {
        if(eventEntVM.creatorUsername.value == "Удаленный аккаунт") {
            binding.eventAvatar.setImageResource(R.drawable.block_user)
            callback(true)
        } else {
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
                        val title = eventSnapshot.child("title").value.toString()
                        val description = eventSnapshot.child("description").value.toString()
                        val creatorUid = eventSnapshot.child("creator_id").value.toString()
                        val photos = arrayListOf(eventSnapshot.child("photos").value.toString())
                        val freePlaces = maxPeople - eventSnapshot.child("amount_reg_people").value.toString().toInt()
                        val timeOfCreation = eventSnapshot.child("time_of_creation").value.toString()
                        val dateOfMeeting = eventSnapshot.child("date_of_meeting").value.toString()
                        val amountRegPeople = eventSnapshot.child("amount_reg_people").value.toString().toInt()
                        val age = eventSnapshot.child("age").value.toString()

                        when(type) {
                            "ent" -> {
                                val sportType = eventSnapshot.child("sport_type").value.toString()
                                checkIfUserAlreadyFriend { ready ->
                                    var found: Boolean = false
                                    dbRefUser.addValueEventListener(object : ValueEventListener {
                                        override fun onDataChange(userSnapshot: DataSnapshot) {
                                            for (userEventSnapshot in userSnapshot.children) {
                                                if (creatorUid == userEventSnapshot.child("uid").value) {
                                                    val creatorUsername =
                                                        userEventSnapshot.child("username").value.toString()
                                                    eventEntVM.setInfo(
                                                        id,
                                                        maxPeople,
                                                        title,
                                                        creatorUid,
                                                        creatorUsername,
                                                        photos,
                                                        arrayListOf("1"),
                                                        freePlaces,
                                                        age,
                                                        description,
                                                        timeOfCreation,
                                                        dateOfMeeting,
                                                        sportType,
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
                                                eventEntVM.setInfo(
                                                    id,
                                                    maxPeople,
                                                    title,
                                                    creatorUid,
                                                    "Удаленный аккаунт",
                                                    photos,
                                                    arrayListOf("1"),
                                                    freePlaces,
                                                    age,
                                                    description,
                                                    timeOfCreation,
                                                    dateOfMeeting,
                                                    sportType,
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

    fun deleteUserFromEvent(uid: String) {
        val dbRef_user = FirebaseDatabase.getInstance().getReference("users").child(auth.currentUser!!.uid).child("curRegEventsId").child(uid)
        val userInEventRef = dbRef_event.child(eventEntVM.id.value.toString()).child("reg_people_id").child(auth.currentUser!!.uid)
        val curPeople = dbRef_event.child(eventEntVM.id.value.toString()).child("amount_reg_people")
        val groupRef = FirebaseDatabase.getInstance().getReference("groups/${eventEntVM.id.value}/members/${auth.currentUser!!.uid}")
        val groupInProfile = FirebaseDatabase.getInstance().getReference("users/${auth.currentUser!!.uid}/groups/$uid")
        dbRef_user.removeValue().addOnSuccessListener {
            userInEventRef.removeValue()
                .addOnSuccessListener {
                    groupRef.removeValue().addOnSuccessListener {
                        curPeople.setValue(eventEntVM.amount_reg_people.value?.minus(1)).addOnSuccessListener {
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
            val dbRef_groups = FirebaseDatabase.getInstance().getReference("groups/${eventEntVM.id.value}/members")
            val event_id = eventEntVM.id.value.toString()

            dbRef_event.child(event_id).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if(dataSnapshot.exists()) {
                        val max = dataSnapshot.child("max_people").getValue(Int::class.java) ?: 0
                        val reg_people = dataSnapshot.child("amount_reg_people").getValue(Int::class.java) ?: 0

                        if (max - reg_people > 0) {
                            val regPeopleRef = dbRef_event.child(event_id).child("reg_people_id").child(auth.currentUser!!.uid)

                            regPeopleRef.setValue(curUid)
                                .addOnSuccessListener {
                                    dbRef_event.child(event_id).child("amount_reg_people").setValue(reg_people + 1)
                                        .addOnSuccessListener {
                                            dbRef_users.child("curRegEventsId").child(event_id).setValue(event_id)
                                                .addOnSuccessListener {
                                                    dbRef_groups.child(auth.currentUser!!.uid).setValue(auth.currentUser!!.uid).addOnSuccessListener {
                                                        binding.btnRegToEvent.visibility = View.GONE
                                                        binding.deleteOrLeave.visibility = View.VISIBLE
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

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}