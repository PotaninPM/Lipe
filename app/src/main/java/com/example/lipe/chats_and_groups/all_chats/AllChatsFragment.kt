package com.example.lipe.chats_and_groups.all_chats

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.lipe.databinding.FragmentChatsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class AllChatsFragment : Fragment() {

    private lateinit var binding: FragmentChatsBinding

    private lateinit var storageRef : StorageReference

    private lateinit var auth: FirebaseAuth

    private lateinit var adapter: AllChatsAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentChatsBinding.inflate(inflater, container, false)

        adapter = AllChatsAdapter(viewLifecycleOwner.lifecycleScope)

        storageRef = FirebaseStorage.getInstance().reference
        auth = FirebaseAuth.getInstance()

        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = AllChatsAdapter(viewLifecycleOwner.lifecycleScope)
        binding.chatRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.chatRecyclerView.adapter = adapter

        setChats()
    }

    private fun setChats() {
        val yourUid = auth.currentUser?.uid
        val dbRef_chats = FirebaseDatabase.getInstance().getReference("users/${yourUid}/chats")
        dbRef_chats.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val chats_list = ArrayList<ChatItem>()
                for(chats in dataSnapshot.children) {
                    val dbRef_chat_uid = FirebaseDatabase.getInstance().getReference("chats/${chats.value.toString()}")
                    dbRef_chat_uid.addListenerForSingleValueEvent(object: ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val uid: String = snapshot.key.toString()
                            val memberUid1: String = snapshot.child("user1_uid").value.toString()
                            val memberUid2: String = snapshot.child("user2_uid").value.toString()
                            if(memberUid2 == yourUid) {
                                val chat = ChatItem(uid, yourUid, memberUid1)
                                chats_list.add(chat)
                                adapter.updateRequests(chats_list)
                            } else {
                                val chat = ChatItem(uid, yourUid!!, memberUid2)
                                chats_list.add(chat)
                                adapter.updateRequests(chats_list)
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
}