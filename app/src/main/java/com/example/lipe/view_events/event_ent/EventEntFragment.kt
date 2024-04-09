package com.example.lipe.view_events.event_ent

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.example.lipe.MapsFragment
import com.example.lipe.R
import com.example.lipe.databinding.FragmentEventEntBinding
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
import com.squareup.picasso.Picasso

class EventEntFragment : BottomSheetDialogFragment() {
    private lateinit var auth: FirebaseAuth

    private lateinit var dbRef: DatabaseReference

    private lateinit var dbRef_event: DatabaseReference

    private lateinit var dbRef_user: DatabaseReference

    private lateinit var storageRef : StorageReference

    private lateinit var appVM: AppVM
    private var _binding: FragmentEventEntBinding? = null
    private val binding get() = _binding!!

    private val eventEntVM: EventEntVM by activityViewModels()




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentEventEntBinding.inflate(inflater, container, false)

        auth = FirebaseAuth.getInstance()

        appVM = ViewModelProvider(requireActivity()).get(AppVM::class.java)

        storageRef = FirebaseStorage.getInstance().reference

        dbRef_event = FirebaseDatabase.getInstance().getReference("current_events")

        searchEvent(appVM.latitude, appVM.longtitude) {ready ->
            if(ready) {
                loadAllImages {ready->
                    if(ready) {
                        checkIfUserAlreadyReg(auth.currentUser!!.uid, eventEntVM.id.value.toString()) {ans ->
                            if(ans) {
                                binding.btnRegToEvent.visibility = View.GONE
                                binding.viewQr.visibility = View.VISIBLE
                                binding.deleteOrLeave.visibility = View.VISIBLE

                                binding.allEntEvent.visibility = View.VISIBLE
                                binding.loadingProgressBar.visibility = View.GONE
                            } else {
                                binding.allEntEvent.visibility = View.VISIBLE
                                binding.loadingProgressBar.visibility = View.GONE
                            }
                        }
                    }
                }
            }
        }

        binding.deleteOrLeave.setOnClickListener {
            if(auth.currentUser!!.uid == eventEntVM.creator.value) {
                deleteEvent(eventEntVM.id.value.toString())
            } else {
                deleteUserFromEvent(eventEntVM.id.value.toString())
            }
        }

        binding.viewQr.setOnClickListener {

        }

        binding.btnRegToEvent.setOnClickListener {
            val curUid = auth.currentUser?.uid
            if (curUid != null) {
                checkIfUserAlreadyReg(curUid, eventEntVM.id.value!!) { isUserAlreadyRegistered ->
                    if (!isUserAlreadyRegistered) {
                        regUserToEvent(curUid) { result ->
                            if(result == true) {
                                setDialog("Успешная регистрация", "Поздравляем, регистрация на событие прошла успешно", "Отлично!")
                            } else {
                                //fail
                                setDialog("Ошибка при регистрации", "Что-то пошло не так, попробуйте зарегистрироваться еще раз","Хорошо")
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

        //eventEntVM = ViewModelProvider(requireActivity()).get(EventEntVM::class.java)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.allEntEvent.visibility = View.GONE
        binding.loadingProgressBar.visibility = View.VISIBLE

        binding.apply {
            lifecycleOwner = viewLifecycleOwner
            viewModel = eventEntVM

//            if(eventEntVM.creator.value == auth.currentUser!!.uid) {
//                binding.btnRegToEvent.visibility = View.GONE
//                binding.viewQr.visibility = View.VISIBLE
//                binding.deleteOrLeave.visibility = View.VISIBLE
//            } else {
//                checkIfUserAlreadyReg(auth.currentUser!!.uid, eventEntVM.id.value!!) { ans ->
//                    if(ans) {
//                        binding.btnRegToEvent.visibility = View.GONE
//                        binding.viewQr.visibility = View.VISIBLE
//                        binding.deleteOrLeave.visibility = View.VISIBLE
//                    }
//                }
//            }

                //}
            //}
        }
    }

    private fun loadAllImages(callback: (Boolean) -> Unit) {
        if(eventEntVM.creatorUsername.value == "Удаленный аккаунт") {
            Picasso.get().load(R.drawable.block_user).into(binding.eventAvatar)
            callback(true)
        } else {
            val uid = eventEntVM.photos.value?.get(0).toString().removeSurrounding("[", "]")

            val userAvatarRef = storageRef.child("avatars/${eventEntVM.creator.value}")

            val photoRef = storageRef.child("event_images/$uid")

            val tokenTask = photoRef.downloadUrl

            val tokenTask2 = userAvatarRef.downloadUrl

            tokenTask.addOnSuccessListener { uri ->
                val imageUrl = uri.toString()
                Picasso.get().load(imageUrl).into(binding.image)
                tokenTask2.addOnSuccessListener { uri ->
                    val imageUrl2 = uri.toString()
                    Picasso.get().load(imageUrl2).into(binding.eventAvatar)
                    callback(true)
                }.addOnFailureListener {

                }
                Thread.sleep(1000)

            }.addOnFailureListener {
                callback(false)
            }

        }

    }

    private fun searchEvent(coord1: Double, coord2: Double, callback: (ready: Boolean) -> Unit) {
        val dbRefEvent = FirebaseDatabase.getInstance().getReference("current_events")
        val dbRefUser = FirebaseDatabase.getInstance().getReference("users")

        dbRefEvent.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for(eventSnapshot in dataSnapshot.children) {
                    val coordinates: List<Double>? = eventSnapshot.child("coordinates").getValue(object : GenericTypeIndicator<List<Double>>() {})
                    if(coordinates != null && coordinates[0] == coord1 && coordinates[1] == coord2) {
                        val type = eventSnapshot.child("type_of_event").value.toString()
                        val id = eventSnapshot.child("event_id").value.toString()
                        val maxPeople = eventSnapshot.child("max_people").value.toString().toInt()
                        val title = eventSnapshot.child("title").value.toString()
                        val description = eventSnapshot.child("description").value.toString()
                        val creatorUid = eventSnapshot.child("creator_id").value.toString()
                        val photos = arrayListOf(eventSnapshot.child("photos").value.toString())
                        val address = eventSnapshot.child("adress").value.toString()
                        val freePlaces = maxPeople - eventSnapshot.child("amount_reg_people").value.toString().toInt()
                        val timeOfCreation = eventSnapshot.child("time_of_creation").value.toString()
                        val dateOfMeeting = eventSnapshot.child("date_of_meeting").value.toString()
                        val amountRegPeople = eventSnapshot.child("amount_reg_people").value.toString().toInt()
                        val age = eventSnapshot.child("age").value.toString()

                        when(type) {
                            "ent" -> {
                                val sportType = eventSnapshot.child("sport_type").value.toString()

                                var found: Boolean = false
                                dbRefUser.addValueEventListener(object : ValueEventListener {
                                    override fun onDataChange(userSnapshot: DataSnapshot) {
                                        for(userEventSnapshot in userSnapshot.children) {
                                            if (creatorUid == userEventSnapshot.child("uid").value) {
                                                val creatorUsername = userEventSnapshot.child("username").value.toString()
                                                val userPhoto = "1"
                                                eventEntVM.setInfo(id, maxPeople, title, creatorUid, creatorUsername, photos, arrayListOf("1"), address, freePlaces, age, description, timeOfCreation, dateOfMeeting, sportType, amountRegPeople)
                                                callback(true)
                                                found = true
                                                return
                                            }
                                        }

                                        if(found == false) {
                                            eventEntVM.setInfo(id, maxPeople, title, creatorUid, "Удаленный аккаунт", photos, arrayListOf("1"), address, freePlaces, age, description, timeOfCreation, dateOfMeeting, sportType, amountRegPeople)
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

    private fun deleteEvent(uid: String) {
        val dbRef_user = FirebaseDatabase.getInstance().getReference("users").child(auth.currentUser!!.uid).child("curRegEventsId").child(uid)
        val curPeople = dbRef_event.child(eventEntVM.id.value.toString())
        dbRef_user.removeValue().addOnSuccessListener {
            curPeople.removeValue()
                .addOnSuccessListener {
                    binding.deleteOrLeave.visibility = View.GONE
                    binding.viewQr.visibility = View.GONE
                    binding.btnRegToEvent.visibility = View.VISIBLE
                }
                .addOnFailureListener {
                    Log.e("INFOG", "ErLeaveEvent")
                }
        }
    }

    fun deleteUserFromEvent(uid: String) {
        val dbRef_user = FirebaseDatabase.getInstance().getReference("users").child(auth.currentUser!!.uid).child("curRegEventsId").child(uid)
        val userInEventRef = dbRef_event.child(eventEntVM.id.value.toString()).child("reg_people_id").child(auth.currentUser!!.uid)
        val curPeople = dbRef_event.child(eventEntVM.id.value.toString()).child("amount_reg_people").toString()
        Log.d("INFOG", curPeople)
        dbRef_user.removeValue().addOnSuccessListener {
            userInEventRef.removeValue()
                .addOnSuccessListener {
                    binding.deleteOrLeave.visibility = View.GONE
                    binding.viewQr.visibility = View.GONE
                    binding.btnRegToEvent.visibility = View.VISIBLE
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
    }


    private fun regUserToEvent(curUid: String, callback: (Boolean) -> Unit) {
        val dbRef_users = FirebaseDatabase.getInstance().getReference("users/${auth.currentUser!!.uid}")
        val dbRef_event = FirebaseDatabase.getInstance().getReference("current_events")

        val event_id = eventEntVM.id.value.toString()

        dbRef_event.child(event_id).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
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
                                                binding.btnRegToEvent.visibility = View.GONE
                                                binding.viewQr.visibility = View.VISIBLE
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
    }


    companion object {
        fun newInstance(): EventEntFragment {
            return EventEntFragment()
        }

        fun show(fragmentManager: FragmentManager) {
            newInstance().show(fragmentManager, "MyBottomFragment")
        }

        fun hideBottomSheet(bottomSheet: BottomSheetDialogFragment) {
            bottomSheet.dismiss()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}