package com.example.lipe.chats_and_groups.all_groups

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.lipe.chats_and_groups.all_chats.ChatItem
import com.example.lipe.databinding.FragmentGroupsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class AllGroupsFragment : Fragment() {

    private lateinit var binding: FragmentGroupsBinding

    private lateinit var storageRef : StorageReference

    private lateinit var auth: FirebaseAuth

    private lateinit var adapter: AllGroupsAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentGroupsBinding.inflate(inflater, container, false)

        storageRef = FirebaseStorage.getInstance().reference
        auth = FirebaseAuth.getInstance()

        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = AllGroupsAdapter(viewLifecycleOwner.lifecycleScope)
        binding.chatRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.chatRecyclerView.adapter = adapter

        setGroups()
    }

    private fun setGroups() {
        val yourUid = auth.currentUser?.uid
        val dbRef_chats = FirebaseDatabase.getInstance().getReference("users/${yourUid}/groups")
        dbRef_chats.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if(dataSnapshot.exists()) {
                    val groups_list = ArrayList<GroupItem>()
                    for (groups in dataSnapshot.children) {
                        val dbRef_chat_uid = FirebaseDatabase.getInstance()
                            .getReference("groups/${groups.value.toString()}")
                        dbRef_chat_uid.addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                if(snapshot.exists()) {
                                    val uid: String = snapshot.key.toString()
                                    val title: String = snapshot.child("title").value.toString()
                                    val imageUid: String =
                                        snapshot.child("imageUid").value.toString()
                                    val group = GroupItem(uid, title, imageUid)
                                    groups_list.add(group)
                                    adapter.updateRequests(groups_list)
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {
                                Log.e("FirebaseError", "Ошибка Firebase ${error.message}")
                            }
                        })
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("FirebaseError","Ошибка Firebase ${databaseError.message}")
            }
        })
    }
}