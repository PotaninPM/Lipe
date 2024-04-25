package com.example.lipe.chats_and_groups.chats

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.lipe.R
import com.example.lipe.databinding.FragmentChatsBinding
import com.example.lipe.databinding.FragmentFriendRequestsBinding
import com.example.lipe.friend_requests.Request
import com.example.lipe.friend_requests.RequestsAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class ChatsFragment : Fragment() {

    private var _binding: FragmentChatsBinding? = null
    private val binding get() = _binding!!

    private lateinit var storageRef : StorageReference

    private lateinit var auth: FirebaseAuth

    private lateinit var adapter: ChatsAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentChatsBinding.inflate(inflater, container, false)

        storageRef = FirebaseStorage.getInstance().reference
        auth = FirebaseAuth.getInstance()

        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = ChatsAdapter()
        binding.chatRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.chatRecyclerView.adapter = adapter

        setChats()
    }

    private fun setChats() {
        val dbRef_chats = FirebaseDatabase.getInstance().getReference("users/${auth.currentUser!!.uid}/chats")
        dbRef_chats.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val chats = ArrayList<Chat>()
                for(chats in dataSnapshot.children) {
                    val dbRef_chat_uid = FirebaseDatabase.getInstance().getReference("chats/${chats.value.toString()}")
                    dbRef_chat_uid.addListenerForSingleValueEvent(object: ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val username:String = snapshot.child("username").value.toString()
                            val uid: String = snapshot.child("uid").value.toString()
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

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}