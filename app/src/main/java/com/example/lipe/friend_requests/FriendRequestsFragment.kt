package com.example.lipe.friend_requests

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.lipe.R
import com.example.lipe.chats_and_groups.ChatsAndGroupsFragment
import com.example.lipe.databinding.FragmentFriendRequestsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class FriendRequestsFragment : Fragment() {

    private lateinit var binding: FragmentFriendRequestsBinding

    private  lateinit var dbRef: DatabaseReference

    private lateinit var storageRef : StorageReference

    private lateinit var auth: FirebaseAuth

    private lateinit var adapter: RequestsAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentFriendRequestsBinding.inflate(inflater, container, false)

        storageRef = FirebaseStorage.getInstance().reference
        auth = FirebaseAuth.getInstance()

        val view = binding.root
        return view
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = RequestsAdapter(lifecycleScope)
        binding.recyclerReq.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerReq.adapter = adapter

        setRequests()

        binding.backBtn.setOnClickListener {
            replaceFragment(ChatsAndGroupsFragment())
        }
    }

    private fun setRequests() {
        val dbRef_user = FirebaseDatabase.getInstance().getReference("users/${auth.currentUser!!.uid}/query_friends")
        dbRef_user.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val requests = ArrayList<Request>()
                for(queries in dataSnapshot.children) {
                    val dbRef_username = FirebaseDatabase.getInstance().getReference("users/${queries.value.toString()}")
                    dbRef_username.addListenerForSingleValueEvent(object: ValueEventListener{
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val username:String = snapshot.child("username").value.toString()
                            val uid: String = snapshot.child("uid").value.toString()
                            getUserPhotoUrl(queries.value.toString()) {url ->
                                if(url != "-") {
                                    Log.d("INFOG", username)
                                    val request = Request(url, username, uid, auth.currentUser!!.uid)
                                    requests.add(request)
                                    adapter.updateRequests(requests)
                                }
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Log.e("FirebaseError","Ошибка Firebase ${error.message}")
                        }
                    })
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("FirebaseError","Ошибка Firebase ${databaseError.message}")
            }
        })
    }
    private fun getUserPhotoUrl(uid: String, callback: (url: String) -> Unit) {
        val photoRef = storageRef.child("avatars/$uid")

        val tokenTask = photoRef.downloadUrl

        tokenTask.addOnSuccessListener { url ->
            val imageUrl = url.toString()
            callback(imageUrl)
        }.addOnFailureListener {
            callback("-")
        }
    }
    private fun replaceFragment(fragment: Fragment) {
        val fragmentManager = childFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.all_fr_req, fragment)
        fragmentTransaction.addToBackStack(null)
        fragmentTransaction.commit()
    }
}