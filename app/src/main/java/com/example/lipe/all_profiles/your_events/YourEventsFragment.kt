package com.example.lipe.all_profiles.cur_events

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.lipe.databinding.FragmentYourEventsBinding
import com.example.lipe.all_profiles.EventItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class YourEventsFragment(val personUid: String) : Fragment() {

    private lateinit var binding: FragmentYourEventsBinding

    private lateinit var storageRef : StorageReference

    private lateinit var adapter: YourEventsAdapter

    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentYourEventsBinding.inflate(inflater, container, false)

        auth = FirebaseAuth.getInstance()

        storageRef = FirebaseStorage.getInstance().reference

        adapter = YourEventsAdapter(lifecycleScope)
        binding.recuclerviewInProfile.layoutManager = LinearLayoutManager(requireContext())
        binding.recuclerviewInProfile.adapter = adapter

        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setCurEvents()
    }

    private fun setCurEvents() {
        val dbRef_your_events = FirebaseDatabase.getInstance().getReference("users/${personUid}/yourCreatedEvents")
        val yourEvents = ArrayList<EventItem>()
        dbRef_your_events.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.childrenCount.toInt() != 0) {
                    binding.recuclerviewInProfile.visibility = View.VISIBLE
                    binding.noEvents.visibility = View.INVISIBLE
                } else {
                    binding.recuclerviewInProfile.visibility = View.INVISIBLE
                    binding.noEvents.visibility = View.VISIBLE
                }
                for(event in snapshot.children) {
                    Log.d("INFOG", event.value.toString())
                    val dbRef_cur_events = FirebaseDatabase.getInstance().getReference("current_events/${event.value}")
                    dbRef_cur_events.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            if(dataSnapshot.exists()) {
                                val title = dataSnapshot.child("title").value.toString()
                                val date_meeting =
                                    dataSnapshot.child("date_of_meeting").value.toString()
                                val status = dataSnapshot.child("status").value.toString()
                                val photos = dataSnapshot.child("photos").value.toString()

                                var statusRus = ""

                                if (status == "ok") {
                                    statusRus = "Подтверждён"
                                } else if (status == "processing") {
                                    statusRus = "В обработке"
                                } else if (status == "failed") {
                                    statusRus = "Будет удалён"
                                }

                                yourEvents.add(EventItem(photos, title, date_meeting, statusRus))
                                adapter.updateRequests(yourEvents)
                            }
                        }
                        override fun onCancelled(databaseError: DatabaseError) {
                            Log.e("FirebaseError","Ошибка Firebase ${databaseError.message}")
                        }
                    })
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }
}