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
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso

class EventEntFragment : BottomSheetDialogFragment() {
    private lateinit var auth: FirebaseAuth

    private lateinit var dbRef: DatabaseReference

    private lateinit var dbRef_event: DatabaseReference

    private lateinit var dbRef_user: DatabaseReference

    private lateinit var storageRef : StorageReference

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

            storageRef = FirebaseStorage.getInstance().reference

            dbRef_event = FirebaseDatabase.getInstance().getReference("current_events")
            //searchEvent(eventEntVM.latitude, eventEntVM.longtitude) { ready ->
                //if (ready == true) {

                    val uid = eventEntVM.photos.value?.get(0).toString().removeSurrounding("[", "]")

                    Log.d("INFOG", uid)
                    val photoRef = storageRef.child("event_images/$uid")
                    val tokenTask = photoRef.downloadUrl

                    tokenTask.addOnSuccessListener { uri ->
                        val imageUrl = uri.toString()

                        Picasso.get().load(imageUrl).into(image);
                        Log.d("INFOG", imageUrl)
                    }.addOnFailureListener {

                    }
            binding.deleteOrLeave.setOnClickListener {
                if(auth.currentUser!!.uid == eventEntVM.creator.value) {
                    deleteEvent()
                } else {
                    deleteUserFromEvent(auth.currentUser!!.uid)
                }
            }

            binding.viewQr.setOnClickListener {

            }

            if(eventEntVM.creator.value == auth.currentUser!!.uid) {
                binding.btnRegToEvent.visibility = View.GONE
                binding.viewQr.visibility = View.VISIBLE
                binding.deleteOrLeave.visibility = View.VISIBLE
            } else {
                checkIfUserAlreadyReg(auth.currentUser!!.uid, eventEntVM.id.value!!) { ans ->
                    if(ans) {
                        binding.btnRegToEvent.visibility = View.GONE
                        binding.viewQr.visibility = View.VISIBLE
                        binding.deleteOrLeave.visibility = View.VISIBLE
                    }
                }
            }

                //}
            //}
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
                        //function to refuse of event
                    }
                }
            } else {
                Log.e("INFOG", "UID пользователя не найден")
            }
        }
    }

    private fun deleteEvent() {
        val userInEventRef = dbRef_event.child("current_events").child(eventEntVM.id.value.toString())
        userInEventRef.removeValue().addOnSuccessListener {
            Log.d("INFOG", "Событие удалено")
        }.addOnFailureListener {
            Log.d("INFOG", "Удаление произошло не успешно")
        }
    }

    fun deleteUserFromEvent(uid: String) {

        dbRef_user = FirebaseDatabase.getInstance().getReference("users").child(eventEntVM.creatorUsername.value.toString()).child("curRegEventsId").child(uid)

        val userInEventRef = dbRef_event.child("current_events").child(eventEntVM.id.value.toString()).child("reg_people_id").child(uid)

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