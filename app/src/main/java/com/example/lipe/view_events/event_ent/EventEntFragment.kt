package com.example.lipe.view_events.event_ent

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import com.example.lipe.databinding.FragmentEventEntBinding
import com.example.lipe.viewModels.EventEntVM
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
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
        }

        binding.btnRegToEvent.setOnClickListener {
            val curUid = auth.currentUser?.uid
            if (curUid != null) {
                checkIfUserAlreadyReg(curUid, eventEntVM.id) { isUserAlreadyRegistered ->
                    if (!isUserAlreadyRegistered) {
                        regUserToEvent(curUid)
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

    private fun regUserToEvent(curUid: String) {
        dbRef = FirebaseDatabase.getInstance().getReference("users")
        dbRef_event = FirebaseDatabase.getInstance().getReference("current_events")

        dbRef_event.child(eventEntVM.id.toString()).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val event_id = Integer.valueOf(dataSnapshot.child("event_id").value.toString())
                    val maxPeople:Int = Integer.valueOf(dataSnapshot.child("max_people").value.toString())
                    val amountPeople:Int = Integer.valueOf(dataSnapshot.child("amount_reg_people").value.toString())

                    val newIndex = dataSnapshot.child("reg_people_id").childrenCount.toString()

                    if(event_id == eventEntVM.id && maxPeople - amountPeople >= 1) {

                        eventEntVM.freePlaces--
                        eventEntVM.amount_reg_people++

                        dbRef_event.child(event_id.toString()).child("reg_people_id").child(newIndex).setValue(curUid)
                            .addOnSuccessListener {
                                Log.d("INFOG", "Новый элемент успешно добавлен в массив")
                            }
                            .addOnFailureListener { e ->
                                Log.e("INFOG", "Ошибка при добавлении нового элемента: ${e.message}")
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
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}