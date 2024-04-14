package com.example.lipe.events_in_profile

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.lipe.databinding.FragmentCurEventsInProfileBinding
import com.example.lipe.friend_requests.Request
import com.example.lipe.friend_requests.RequestsAdapter
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso

class CurEventsInProfileFragment : Fragment() {

    private var _binding: FragmentCurEventsInProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var storageRef : StorageReference

    private lateinit var adapter: CurEventsAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCurEventsInProfileBinding.inflate(inflater, container, false)

        storageRef = FirebaseStorage.getInstance().reference

        adapter = CurEventsAdapter()
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
        val dbRef_cur_events = FirebaseDatabase.getInstance().getReference("current_events")
        dbRef_cur_events.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val curEvents = ArrayList<EventItem>()
                for (events in dataSnapshot.children) {
                    val title = events.child("title").value.toString()
                    val date_meeting = events.child("date_of_meeting").value.toString()
                    val status = events.child("status").value.toString()
                    val main_photo = arrayListOf(events.child("photos").value.toString()).get(0).removeSurrounding("[", "]")

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
                        curEvents.add(EventItem(imageUrl, title, date_meeting, statusRus))
                        adapter.updateRequests(curEvents)
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("FirebaseError","Ошибка Firebase ${databaseError.message}")
            }
        })
    }
    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}