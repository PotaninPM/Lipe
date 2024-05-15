package com.example.lipe.people_go_to_event

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import coil.ImageLoader
import coil.request.ImageRequest
import com.example.lipe.R
import com.example.lipe.databinding.FragmentPeopleGoToEventBinding
import com.example.lipe.rating_board.RatingItem
import com.example.lipe.viewModels.EventEntVM
import com.example.lipe.view_events.event_ent.EventEntFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PeopleGoToEventFragment : DialogFragment() {

    private var _binding: FragmentPeopleGoToEventBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth

    private val peopleGoAdapter by lazy { PeopleGoAdapter(viewLifecycleOwner.lifecycleScope) }

    private val eventVM: EventEntVM by activityViewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentPeopleGoToEventBinding.inflate(inflater, container, false)

        auth = FirebaseAuth.getInstance()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.recyclerViewPeopleGo.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = peopleGoAdapter
        }

        binding.close.setOnClickListener {
            dismiss()
        }

        loadData(eventVM.id.value.toString())
    }

    override fun onStart() {
        super.onStart()

        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            (resources.displayMetrics.heightPixels * 0.7).toInt()
        )
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
                // Обработка ошибок
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}