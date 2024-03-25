package com.example.lipe.view_events.event_ent

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.example.lipe.databinding.FragmentEventEntBinding
import com.example.lipe.viewModels.EventEntVM
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

class EventEntFragment : BottomSheetDialogFragment() {
    private lateinit var auth: FirebaseAuth

    private lateinit var dbRef: DatabaseReference

    private lateinit var dbRef_event: DatabaseReference

    private lateinit var dbRef_user: DatabaseReference

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

        //eventEntVM = ViewModelProvider(requireActivity()).get(EventEntVM::class.java)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            lifecycleOwner = viewLifecycleOwner
            viewModel = eventEntVM

            Log.d("INFOG", eventEntVM.latitude.toString())
            searchEvent(eventEntVM.latitude, eventEntVM.longtitude)
        }

//        eventEntVM.title1.observe(viewLifecycleOwner, Observer { title ->
//            // Обновление заголовка в вашем пользовательском интерфейсе
//            binding.title.text = title
//        })

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
                        //function to refuse of event
                    }
                }
            } else {
                Log.e("INFOG", "UID пользователя не найден")
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

    private fun checkForUpdates() {
//        eventEntVM.title1.observe(viewLifecycleOwner, Observer { title ->
//            binding.title.text = title
//        })
    }


    private fun checkIfUserAlreadyReg(curUid: String, eventId: Int, callback: (Boolean) -> Unit) {
        dbRef_event = FirebaseDatabase.getInstance().getReference("current_events")

        dbRef_event.child(eventId.toString()).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val regPeopleSnapshot = dataSnapshot.child("reg_people_id")
                var isUserRegistered = false
                for (childSnapshot in regPeopleSnapshot.children) {
                    val uid = childSnapshot.getValue(String::class.java)
                    if (uid == curUid) {
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

    private fun searchEvent(coord1: Double, coord2: Double) {
        dbRef_event = FirebaseDatabase.getInstance().getReference("current_events")
        dbRef_user = FirebaseDatabase.getInstance().getReference("users")

        dbRef_event.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for(eventSnapshot in dataSnapshot.children) {
                    val coordinates: List<Double>? = eventSnapshot.child("coordinates").getValue(object : GenericTypeIndicator<List<Double>>() {})

                    if (coordinates!![0] == coord1 && coordinates[1] == coord2) {
                        var type = eventSnapshot.child("type_of_event").value.toString()
                        if(type == "ent") {
                            var id: Int = Integer.valueOf(eventSnapshot.child("event_id").value.toString())
                            var maxPeople: Int = eventSnapshot.child("max_people").value.toString().toInt()
                            var title = eventSnapshot.child("title").value.toString()
                            val description = eventSnapshot.child("description").value.toString()
                            val creator_uid = eventSnapshot.child("creator_id").value.toString()
                            var photos = listOf("1")
                            var peopleGo = listOf("1")
                            var adress = eventSnapshot.child("adress").value.toString()
                            var freePlaces = maxPeople - eventSnapshot.child("amount_reg_people").value.toString().toInt()
                            var time_of_creation = eventSnapshot.child("time_of_creation").value.toString()
                            val date_of_meeeting = eventSnapshot.child("date_of_meet").value.toString()
                            var type_sport = eventSnapshot.child("sport_type").value.toString()
                            var amount_reg_people:Int = Integer.valueOf(eventSnapshot.child("amount_reg_people").value.toString())

                            dbRef_user.addValueEventListener(object : ValueEventListener {
                                override fun onDataChange(dataSnapshot: DataSnapshot) {
                                    for (eventSnapshot in dataSnapshot.children) {
                                        if(creator_uid == eventSnapshot.child("uid").value) {
                                            val creatorUsername = eventSnapshot.child("username").value.toString()
                                            eventEntVM.setInfo(id, maxPeople, title, creator_uid, creatorUsername, photos, peopleGo, adress, freePlaces, description, time_of_creation, date_of_meeeting, type_sport, amount_reg_people)
                                            break
                                        }
                                    }
                                }

                                override fun onCancelled(error: DatabaseError) {
                                    Log.e("INFOG", "ErrorEventEntFragm")
                                }
                            })
                        } else if(type == "eco") {
                            //TODO
                        }
                        break
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("FirebaseError","Ошибка Firebase ${databaseError.message}")
            }
        })
    }

    private fun regUserToEvent(curUid: String, callback: (Boolean) -> Unit) {
        dbRef = FirebaseDatabase.getInstance().getReference("users")
        dbRef_event = FirebaseDatabase.getInstance().getReference("current_events")

        dbRef_event.child(eventEntVM.id.value.toString()).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                    val newIndex = dataSnapshot.child("reg_people_id").childrenCount.toString()

                    var max: Int = eventEntVM.maxPeople.value!!
                    var reg_people: Int = eventEntVM.amount_reg_people.value!!
                    var event_id: Int = eventEntVM.id.value!!

                    if(max - reg_people >= 1) {

                        dbRef_event.child(event_id.toString()).child("reg_people_id").child(newIndex).setValue(curUid)
                            .addOnSuccessListener {
                                dbRef_event.child(event_id.toString()).child("amount_reg_people").setValue(
                                    eventEntVM.amount_reg_people.value!! + 1).addOnSuccessListener {
                                        dbRef_user.child(auth.currentUser?.uid.toString())
                                    callback(true)
                                }.addOnFailureListener {
                                        callback(false)
                                    }
                            }
                            .addOnFailureListener { e ->
                                callback(false)
                            }
                    }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("FirebaseError","Ошибка Firebase ${databaseError.message}")
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