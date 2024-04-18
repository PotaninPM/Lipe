package com.example.lipe.your_profile.cur_events

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.lipe.databinding.FragmentYourEventsBinding
import com.example.lipe.your_profile.EventItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class YourEventsFragment : Fragment() {

    private var _binding: FragmentYourEventsBinding? = null
    private val binding get() = _binding!!

    private lateinit var storageRef : StorageReference

    private lateinit var adapter: YourEventsAdapter

    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentYourEventsBinding.inflate(inflater, container, false)

        auth = FirebaseAuth.getInstance()

        storageRef = FirebaseStorage.getInstance().reference

        adapter = YourEventsAdapter()
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
        val dbRef_your_events = FirebaseDatabase.getInstance().getReference("users/${auth.currentUser!!.uid}/yourCreatedEvents")
        val yourEvents = ArrayList<EventItem>()
        dbRef_your_events.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for(event in snapshot.children) {
                    val dbRef_cur_events = FirebaseDatabase.getInstance().getReference("current_events/${event.value}")
                    dbRef_cur_events.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            val title = dataSnapshot.child("title").value.toString()
                            val date_meeting = dataSnapshot.child("date_of_meeting").value.toString()
                            val status = dataSnapshot.child("status").value.toString()
                            val main_photo = arrayListOf(dataSnapshot.child("photos").value.toString()).get(0).removeSurrounding("[", "]")

                            var statusRus = ""

                            if(status == "ok") {
                                statusRus = "Подтверждён"
                            } else if(status == "processing") {
                                statusRus = "В обработке"
                            } else if(status == "failed") {
                                statusRus = "Будет удалён"
                            }

                            val photoRef = storageRef.child("event_images/$main_photo")

                            val token = photoRef.downloadUrl

                            token.addOnSuccessListener {uri ->
                                val imageUrl = uri.toString()
                                yourEvents.add(EventItem(imageUrl, title, date_meeting, statusRus))
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
    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}