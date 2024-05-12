package com.example.lipe.rating_board

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import coil.ImageLoader
import coil.request.ImageRequest
import com.example.lipe.databinding.FragmentRatingBinding
import com.example.lipe.viewModels.RatingVM
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RatingFragment : Fragment() {

    private var _binding: FragmentRatingBinding? = null
    private val binding get() = _binding!!

    private var rateList = mutableListOf<RatingItem>()

    private lateinit var adapter: RatingAdapter

    private lateinit var auth: FirebaseAuth

    private val ratingVM: RatingVM by activityViewModels()

    private lateinit var ratingListener: ValueEventListener
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

        binding.placeInRating.setOnClickListener {
            //adapter.filter()
        }

        addPeople()
    }

    private fun loadDataOfUser() {
        val user = auth.currentUser!!.uid

        val storage = FirebaseStorage.getInstance().getReference("avatars/$user")
        storage.downloadUrl.addOnSuccessListener {
            lifecycleScope.launch {
                val bitmap = withContext(Dispatchers.IO) {
                    ImageLoader(requireContext()).execute(
                        ImageRequest.Builder(requireContext())
                            .data(it)
                            .build()
                    )
                }.drawable?.toBitmap()

                binding.userRatingAvatar.setImageBitmap(bitmap)

                val dbRef_user = FirebaseDatabase.getInstance().getReference("users/$user")
                dbRef_user.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        ratingVM.setInfo(
                            snapshot.child("points").value.toString(),
                            snapshot.child("place_in_rating").value.toString(),
                            it.toString()
                        )
                    }

                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }
                })
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentRatingBinding.inflate(inflater, container, false)

        auth = FirebaseAuth.getInstance()

        binding.apply {
            lifecycleOwner = viewLifecycleOwner
            viewModel = ratingVM

            loadDataOfUser()
        }

        val view = binding.root
        return view
    }

    private fun addPeople() {
        val dbRef_rating = FirebaseDatabase.getInstance().getReference("rating")
        ratingListener = dbRef_rating.orderByKey().addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val all = snapshot.childrenCount.toInt()
                var completedCount = 0
                val newRateList = mutableListOf<RatingItem>()

                snapshot.children.forEach { ratingSnapshot ->
                    val place = ratingSnapshot.key
                    val points = ratingSnapshot.child("points").value.toString()
                    val userUid = ratingSnapshot.child("userUid").value.toString()

                    val dbRef_user = FirebaseDatabase.getInstance().getReference("users/$userUid/username")
                    dbRef_user.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val nickname = snapshot.value.toString()
                            val storageRef = FirebaseStorage.getInstance().getReference("avatars/$userUid")

                            storageRef.downloadUrl.addOnSuccessListener { url ->
                                newRateList.add(RatingItem(userUid, url.toString(), place!!.toInt(), nickname, points.toInt()))
                                completedCount++
                                if (completedCount == all) {
                                    newRateList.sortBy { it.place }
                                    adapter.updateRequests(newRateList)
                                    Log.d("INFOG", newRateList.size.toString())
                                }
                            }.addOnFailureListener {
                                Log.e("INFOG", "Rating smth wrong")
                                completedCount++
                                if (completedCount == all) {
                                    newRateList.sortBy { it.place }
                                    adapter.updateRequests(newRateList)
                                    Log.d("INFOG", newRateList.size.toString())
                                }
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            completedCount++
                            if (completedCount == all) {
                                newRateList.sortBy { it.place }
                                adapter.updateRequests(newRateList)
                                Log.d("INFOG", newRateList.size.toString())
                            }
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
        val dbRef_rating = FirebaseDatabase.getInstance().getReference("rating")
        dbRef_rating.removeEventListener(ratingListener)
        _binding = null
    }
}