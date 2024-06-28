package com.example.lipe.all_profiles.cur_events

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.lipe.R
import com.example.lipe.databinding.FragmentCurEventsInProfileBinding
import com.example.lipe.all_profiles.EventItem
import com.example.lipe.notifications.EventData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.util.Locale

interface OnEventClickListener {
    fun onEventClick(latitude: Double, longitude: Double)
}

class CurEventsInProfileFragment(val personUid: String) : Fragment(), OnEventClickListener {

    private lateinit var binding: FragmentCurEventsInProfileBinding

    private lateinit var storageRef : StorageReference

    private lateinit var adapter: CurEventsAdapter

    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCurEventsInProfileBinding.inflate(inflater, container, false)

        auth = FirebaseAuth.getInstance()

        storageRef = FirebaseStorage.getInstance().reference

        adapter = CurEventsAdapter(lifecycleScope, this)

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
        if (!isAdded || context == null) return

        val dbRefCurUserEvents = FirebaseDatabase.getInstance().getReference("users/${personUid}/curRegEventsId")
        val curEvents = ArrayList<EventItem>()

        dbRefCurUserEvents.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!isAdded || context == null) return

                if (snapshot.childrenCount.toInt() != 0) {
                    binding.recuclerviewInProfile.visibility = View.VISIBLE
                    binding.noEvents.visibility = View.INVISIBLE
                } else {
                    binding.recuclerviewInProfile.visibility = View.INVISIBLE
                    binding.noEvents.visibility = View.VISIBLE
                }

                for (event in snapshot.children) {
                    val dbRefCurEvents = FirebaseDatabase.getInstance().getReference("current_events/${event.value}")
                    dbRefCurEvents.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            if (!isAdded || context == null) return

                            val title = dataSnapshot.child("title").value.toString()
                            val dateMeeting = dataSnapshot.child("date_of_meeting").value.toString()
                            val status = dataSnapshot.child("status").value.toString()
                            val photos = dataSnapshot.child("photos").value.toString()
                            val coord: ArrayList<String> = arrayListOf(dataSnapshot.child("coordinates").child("latitude").value.toString(), dataSnapshot.child("coordinates").child("longitude").value.toString())

                            val statusRus = when (status) {
                                "ok" -> getString(R.string.confirmed)
                                "processing" -> "В обработке"
                                "failed" -> "Будет удалён"
                                else -> ""
                            }

                            curEvents.add(EventItem(photos, title, dateMeeting, statusRus, coord))
                            adapter.updateRequests(curEvents)
                        }

                        override fun onCancelled(databaseError: DatabaseError) {
                            Log.e("FirebaseError", "Ошибка Firebase ${databaseError.message}")
                        }
                    })
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseError", "Ошибка Firebase ${error.message}")
            }
        })
    }

    private fun getAddressFromCoordinates(latitude: Double, longitude: Double): String {
        val geocoder = Geocoder(requireContext(), Locale.getDefault())
        try {
            val addresses = geocoder.getFromLocation(latitude, longitude, 1)
            if (!addresses.isNullOrEmpty()) {
                val address = addresses[0].getAddressLine(0)
                return address
                Log.i("INFOG", "Address: $address")
            } else {
                Log.i("INFOG", "No address found")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("INFOG", "Geocoder failed", e)
        }
        return "-"
    }

    private fun copyToClipboard(text: String) {
        if(isAdded && context != null) {
            val clipboard =
                requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Event Address", text)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(
                requireContext(),
                getString(R.string.address_copied_to_clipboard), Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onEventClick(latitude: Double, longitude: Double) {
        copyToClipboard(getAddressFromCoordinates(latitude, longitude))
    }
}