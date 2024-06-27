package com.example.lipe.rating_board

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.core.graphics.drawable.toBitmap
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import coil.ImageLoader
import coil.request.ImageRequest
import com.example.lipe.BeginDialogFragment
import com.example.lipe.LeagueInfoFragment
import com.example.lipe.R
import com.example.lipe.databinding.FragmentRatingBinding
import com.example.lipe.viewModels.RatingVM
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RatingFragment : Fragment() {

    private lateinit var binding: FragmentRatingBinding

    private var rateList = mutableListOf<RatingItem>()
    private var originalRatingList = mutableListOf<RatingItem>()

    private lateinit var adapter: RatingAdapter

    private lateinit var auth: FirebaseAuth

    private val ratingVM: RatingVM by activityViewModels()

    private lateinit var ratingListener: ValueEventListener
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    data class User(val place: Int, val points: Int, val userUid: String)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if(isAdded) {
            adapter = RatingAdapter(lifecycleScope)
            binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
            binding.recyclerView.adapter = adapter

            binding.cardView.visibility = View.VISIBLE

            binding.recyclerView.setHasFixedSize(true)

            binding.league.setOnClickListener {
                LeagueInfoFragment.newInstance()
                    .show(childFragmentManager, "LeagueInfoFragment")
            }


            binding.searchView.addTextChangedListener {
                    if(it.isNullOrEmpty()) {
                        adapter.filter("", rateList)
                    } else {
                        adapter.filter(it.toString(), rateList)
                    }
            }


            binding.placeInRating.setOnClickListener {
                //adapter.filter()
            }

            addPeople()

            val db = FirebaseDatabase.getInstance().getReference("rating")
            db.addValueEventListener(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    db.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            if (dataSnapshot.exists()) {
                                val usersList = mutableListOf<User>()
                                dataSnapshot.children.forEach { userSnapshot ->
                                    val user = User(userSnapshot.child("place").value.toString().toInt(), userSnapshot.child("points").value.toString().toInt(), userSnapshot.child("userUid").value.toString())
                                    usersList.add(user)
                                }

                                val sortedUsers = usersList.sortedByDescending { it.points }
                                sortedUsers.forEachIndexed { index, user ->
                                    val userUid = dataSnapshot.children.elementAtOrNull(index)?.key
                                    if (userUid != null) {
                                        db.child(userUid).child("place").setValue(index + 1) { databaseError, _ ->
                                            if (databaseError != null) {
                                                //println("${databaseError.message}")
                                            } else {
                                                //println("Data saved successfully")
                                            }
                                        }
                                        db.child(userUid).child("points").setValue(user.points) { databaseError, _ ->
                                            if (databaseError != null) {
                                                //println("${databaseError.message}")
                                            } else {
                                                //println("Data saved successfully")
                                            }
                                        }
                                        db.child(userUid).child("userUid").setValue(user.userUid) { databaseError, _ ->
                                            if (databaseError != null) {
                                                //println("${databaseError.message}")
                                            } else {
                                                //println("Data saved successfully")
                                            }
                                        }
                                    }
                                }
                            } else {
                                println("No data found")
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            println("Failed to read value: ${error.toException()}")
                        }
                    })
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })
        }
    }
    private fun loadDataOfUser() {
        val user = auth.currentUser!!.uid

        val storage = FirebaseStorage.getInstance().getReference("avatars/$user")
        storage.downloadUrl.addOnSuccessListener {
            lifecycleScope.launch {
                try {
                    val bitmap = withContext(Dispatchers.IO) {
                        ImageLoader(requireContext()).execute(
                            ImageRequest.Builder(requireContext())
                                .data(it)
                                .build()
                        )
                    }.drawable?.toBitmap()

                    binding.userRatingAvatar.setImageBitmap(bitmap)
                } catch (e: Exception) {

                }

                val dbRef_user = FirebaseDatabase.getInstance().getReference("users/$user")
                dbRef_user.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val points = snapshot.child("points").value.toString()
                        val league = when {
                            points.toInt() in 0..199 -> binding.leagueImg.setImageResource(R.drawable.log)
                            points.toInt() in 200..499 -> binding.leagueImg.setImageResource(R.drawable.iron)
                            points.toInt() in 500..999 -> binding.leagueImg.setImageResource(R.drawable.gold_league)
                            points.toInt() >= 1000 -> binding.leagueImg.setImageResource(R.drawable.gem)
                            else -> "Unknown"
                        }
                        ratingVM.setInfo(
                            points,
                            snapshot.child("place_in_rating").value.toString(),
                            it.toString(),
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
        binding = FragmentRatingBinding.inflate(inflater, container, false)

        auth = FirebaseAuth.getInstance()

        if(isAdded) {
            binding.apply {
                lifecycleOwner = viewLifecycleOwner
                viewModel = ratingVM

                loadDataOfUser()

                originalRatingList.addAll(rateList)
            }
        }

        val view = binding.root
        return view
    }

    private fun addPeople() {
        val dbRef_rating = FirebaseDatabase.getInstance().getReference("rating")
        ratingListener = dbRef_rating.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val all = snapshot.childrenCount.toInt()
                var completedCount = 0
                val newRateList = mutableListOf<RatingItem>()
                snapshot.children.forEach { ratingSnapshot ->
                    val place = ratingSnapshot.child("place").value.toString()
                    val points = ratingSnapshot.child("points").value.toString()
                    val userUid = ratingSnapshot.child("userUid").value.toString()
                    val dbRef_user = FirebaseDatabase.getInstance().getReference("users/$userUid/username")
                    dbRef_user.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val nickname = snapshot.value.toString()
                            val storageRef = FirebaseStorage.getInstance().getReference("avatars/$userUid")
                            storageRef.downloadUrl.addOnSuccessListener { url ->
                                newRateList.add(RatingItem(userUid, url.toString(), place!!.toInt(), nickname, points.toInt()))
                                rateList.add(RatingItem(userUid, url.toString(), place!!.toInt(), nickname, points.toInt()))
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
    }
}