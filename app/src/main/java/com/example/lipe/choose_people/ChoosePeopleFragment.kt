package com.example.lipe.choose_people

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.lipe.R
import com.example.lipe.databinding.FragmentChoosePeopleBinding
import com.example.lipe.databinding.FragmentPeopleGoToEventBinding
import com.example.lipe.notifications.RetrofitInstance
import com.example.lipe.people_go_to_event.PeopleGoAdapter
import com.example.lipe.people_go_to_event.PersoneGoItem
import com.example.lipe.viewModels.EventEntVM
import com.example.lipe.view_events.EventFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ChoosePeopleFragment(val eventUid: String) : DialogFragment() {

    private lateinit var auth: FirebaseAuth

    private val peopleGoAdapter by lazy { ChoosePeopleAdapter(viewLifecycleOwner.lifecycleScope) }

    private lateinit var binding: FragmentChoosePeopleBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentChoosePeopleBinding.inflate(inflater, container, false)

        auth = FirebaseAuth.getInstance()

        binding.recyclerViewPeopleGo.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = peopleGoAdapter
        }

        loadData(eventUid)

        val view = binding.root
        return view
    }

    private fun loadData(eventUid: String) {
        val dbRef_people_go = FirebaseDatabase.getInstance().getReference("current_events/$eventUid/reg_people_id")
        dbRef_people_go.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val newPeopleList = mutableListOf<PersoneGoItem>()

                snapshot.children.forEach { ratingSnapshot ->
                    val userUid = ratingSnapshot.key

                    val dbRef_user = FirebaseDatabase.getInstance().getReference("users/$userUid/username")
                    dbRef_user.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val nickname = snapshot.value.toString()
                            val storageRef = FirebaseStorage.getInstance().getReference("avatars/$userUid")

                            storageRef.downloadUrl.addOnSuccessListener { url ->
                                newPeopleList.add(PersoneGoItem(userUid.toString(), nickname, url.toString()))
                                peopleGoAdapter.updateRequests(newPeopleList)
                                Log.d("INFOG", newPeopleList.size.toString())
                            }.addOnFailureListener {
                                Log.e("INFOG", "Rating smth wrong")
                                peopleGoAdapter.updateRequests(newPeopleList)
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            peopleGoAdapter.updateRequests(newPeopleList)
                        }
                    })
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.finish.setOnClickListener {
            val selectedUsers = peopleGoAdapter.getSelectedPeople()
            addPointsToUsers(selectedUsers, 3)
            deleteEvent(eventUid)
            dismiss()
        }
    }

    private fun addPointsToUsers(selectedUsers: List<String>, points: Int) {
        val call: Call<Void> = RetrofitInstance.api.getPointsData(GetPointsData(selectedUsers, points))

        Log.d("INFOG", call.request().toString())

        call.enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Log.d("INFOG", "OK, notifications were sent")
                } else {
                    Log.d("INFOG", "${response.message()}")
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Log.d("INFOG", "${t.message}")
            }
        })

        val database = FirebaseDatabase.getInstance()
        val dbRefUsers = database.getReference("users")
        val dbRefRating = database.getReference("rating")

        selectedUsers.forEach { userUid ->
            val userPointsRef = dbRefUsers.child(userUid)
            userPointsRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val currentPoints = snapshot.child("points").getValue(Int::class.java) ?: 0
                    val newPoints = currentPoints + points
                    userPointsRef.child("points").setValue(newPoints) {e, _ ->
                        //sendNotificationToUser(snapshot.child("userToken").value.toString(), "Вам начислены баллы", "Вам начислено $pointsInt баллов. Общие баллы: $newPoints", "points")
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    print("Failed points $userUid: ${error.message}")
                }
            })
        }
        dbRefRating.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.children.forEach { it ->
                    val uid = it.child("userUid").value.toString()
                    if(selectedUsers.contains(uid)) {
                        val curPoints = it.child("points").value.toString().toInt()
                        dbRefRating.child(it.key.toString()).child("points").setValue(curPoints + points) { e, _ ->

                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }
    private fun deleteEvent(uid: String) {
        val dbRef_user = FirebaseDatabase.getInstance().getReference("users").child(auth.currentUser!!.uid).child("curRegEventsId").child(uid)
        val curPeople = FirebaseDatabase.getInstance().getReference("current_events").child(eventUid)
        val dbRef_group = FirebaseDatabase.getInstance().getReference("groups").child(eventUid)
        val reports = FirebaseDatabase.getInstance().getReference("reports/${eventUid}")

        dbRef_user.removeValue().addOnSuccessListener {
            curPeople.removeValue()
                .addOnSuccessListener {
                    dbRef_group.removeValue().addOnSuccessListener {
                        reports.removeValue().addOnSuccessListener {
//                        val eventFragment = parentFragment as? EventFragment
//                        eventFragment?.dismiss()
//
//                        binding.deleteOrLeave.visibility = View.GONE
//                        binding.btnRegToEvent.visibility = View.VISIBLE
                        }
                    }
                }
                .addOnFailureListener {
                    Log.e("INFOG", "ErLeaveEvent")
                }
        }
    }

//    override fun onStart() {
//        super.onStart()
//        dialog?.window?.setLayout(
//            ViewGroup.LayoutParams.MATCH_PARENT,
//            (resources.displayMetrics.heightPixels * 0.7).toInt()
//        )
//    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout((resources.displayMetrics.widthPixels * 0.9).toInt(), (resources.displayMetrics.heightPixels * 0.7).toInt())
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
    }
}