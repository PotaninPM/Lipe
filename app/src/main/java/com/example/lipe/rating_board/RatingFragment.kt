package com.example.lipe.rating_board

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.lipe.databinding.FragmentRatingBinding
import com.example.lipe.friend_requests.RequestsAdapter
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage

class RatingFragment : Fragment() {

    private var _binding: FragmentRatingBinding? = null
    private val binding get() = _binding!!

    private var rateList = mutableListOf<RatingItem>()

    private lateinit var adapter: RatingAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = RatingAdapter(lifecycleScope)
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter

        binding.recyclerView.setHasFixedSize(true)

        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                newText?.let {
                    if(it.isEmpty()) {
                        adapter.updateRequests(rateList)
                    } else {
                        adapter.filter(it)
                    }

                }
                return true
            }
        })

        addPeople()
//        binding.searchView.setOnQueryTextListener(object: SearchView.OnQueryTextListener {
//            override fun onQueryTextSubmit(query: String?): Boolean {
//
//            }
//
//            override fun onQueryTextChange(newText: String?): Boolean {
//                filterList(newText)
//                return true
//            }
//
//        })
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentRatingBinding.inflate(inflater, container, false)

        val view = binding.root
        return view
    }

    private fun addPeople() {
        val dbRef_rating = FirebaseDatabase.getInstance().getReference("rating")
        dbRef_rating.orderByKey().addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                snapshot.children.sortedBy { it.child("place").value.toString().toInt() }.forEach { ratingSnapshot ->
                    val place = ratingSnapshot.child("place").value.toString()
                    val points = ratingSnapshot.child("points").value.toString()
                    val userUid = ratingSnapshot.child("userUid").value.toString()

                    val dbRef_user = FirebaseDatabase.getInstance().getReference("users/$userUid/username")
                    dbRef_user.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val nickname = snapshot.value.toString()
                            val storageRef = FirebaseStorage.getInstance().getReference("avatars/$userUid")

                            storageRef.downloadUrl.addOnSuccessListener { url ->
                                rateList.add(RatingItem(userUid, url.toString(), place.toInt(), nickname, points.toInt()))
                                adapter.updateRequests(rateList)
                            }.addOnFailureListener {
                                Log.e("INFOG", "Rating smth wrong")
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {

                        }
                    })
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Обработка ошибок
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}