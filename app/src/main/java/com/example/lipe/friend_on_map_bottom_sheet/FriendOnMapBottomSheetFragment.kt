package com.example.lipe.friend_on_map_bottom_sheet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.lipe.all_profiles.friends.FriendItem
import com.example.lipe.all_profiles.friends.FriendsBottomSheetAdapter
import com.example.lipe.databinding.FragmentFriendOnMapBottomSheetBinding
import com.example.lipe.viewModels.AppVM
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class FriendOnMapBottomSheetFragment : BottomSheetDialogFragment() {

    private lateinit var binding: FragmentFriendOnMapBottomSheetBinding

    private lateinit var adapter: FriendOnBottomSheetAdapter

    private val appVM: AppVM by activityViewModels()

    private lateinit var storage: StorageReference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentFriendOnMapBottomSheetBinding.inflate(inflater, container, false)

        storage = FirebaseStorage.getInstance().reference

        adapter = FriendOnBottomSheetAdapter(lifecycleScope, appVM.type)
        binding.friendsRecView.layoutManager = LinearLayoutManager(requireContext())
        binding.friendsRecView.adapter = adapter

        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setPeople()
    }

    private fun setPeople() {
        val dbRef_your_friends = FirebaseDatabase.getInstance().getReference("users/${appVM.type}/friends")
        val yourFriends = ArrayList<FriendItem>()
        dbRef_your_friends.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for(friend in snapshot.children) {
                    val friendUid = friend.value.toString()
                    val friendDbRef = FirebaseDatabase.getInstance().getReference("users/$friendUid")
                    friendDbRef.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val name = snapshot.child("firstAndLastName").value.toString()
                            storage.child("avatars").child("$friendUid").downloadUrl.addOnSuccessListener { url ->
                                yourFriends.add(FriendItem(url.toString(), name, friendUid))
                                adapter.updateFriends(yourFriends)
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            TODO("Not yet implemented")
                        }
                    })
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }
}