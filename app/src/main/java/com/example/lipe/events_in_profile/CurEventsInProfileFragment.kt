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

class CurEventsInProfileFragment : Fragment() {

    private var _binding: FragmentCurEventsInProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: CurEventsAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCurEventsInProfileBinding.inflate(inflater, container, false)

        adapter = CurEventsAdapter()
        binding.recuclerviewInProfile.layoutManager = LinearLayoutManager(requireContext())
        binding.recuclerviewInProfile.adapter = adapter

        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val events = ArrayList<EventItem>()
        val event = EventItem("https://upload.wikimedia.org/wikipedia/commons/0/0e/Felis_silvestris_silvestris.jpg", "Футбол", "12 октября 2024, 8:00 - 12:00", "Подтвержден")
        events.add(event)
        events.add(event)
        events.add(event)
        adapter.updateRequests(events)
    }

    private fun setRequests() {
//        val dbRef_user = FirebaseDatabase.getInstance().getReference("users/${auth.currentUser!!.uid}/query_friends")
//        dbRef_user.addValueEventListener(object : ValueEventListener {
//            override fun onDataChange(dataSnapshot: DataSnapshot) {
//                val requests = ArrayList<Request>()
//                for(queries in dataSnapshot.children) {
//                    val dbRef_username = FirebaseDatabase.getInstance().getReference("users/${queries.value.toString()}")
//                    dbRef_username.addListenerForSingleValueEvent(object: ValueEventListener {
//                        override fun onDataChange(snapshot: DataSnapshot) {
//                            val username:String = snapshot.child("username").value.toString()
//                            val uid: String = snapshot.child("uid").value.toString()
//                            getUserPhotoUrl(queries.value.toString()) {url ->
//                                if(url != "-") {
//                                    val request = Request(url, username, uid, auth.currentUser!!.uid)
//                                    requests.add(request)
//                                    adapter.updateRequests(requests)
//                                }
//                            }
//                        }
//
//                        override fun onCancelled(error: DatabaseError) {
//                            Log.e("FirebaseError","Ошибка Firebase ${error.message}")
//                        }
//                    })
//                }
//            }
//
//            override fun onCancelled(databaseError: DatabaseError) {
//                Log.e("FirebaseError","Ошибка Firebase ${databaseError.message}")
//            }
//        })
    }
    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}