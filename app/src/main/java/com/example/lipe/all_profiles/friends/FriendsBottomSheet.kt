package com.example.lipe.all_profiles.friends

import android.app.Dialog
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import coil.Coil
import coil.request.ImageRequest
import com.example.lipe.all_profiles.EventItem
import com.example.lipe.all_profiles.cur_events.YourEventsAdapter
import com.example.lipe.databinding.BottomSheetChangeYourInfoLayoutBinding
import com.example.lipe.databinding.FriendsBottomSheetBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FriendsBottomSheet : BottomSheetDialogFragment() {
    private lateinit var binding: FriendsBottomSheetBinding

    private lateinit var adapter: FriendsBottomSheetAdapter

    private lateinit var auth: FirebaseAuth

    private lateinit var storage: StorageReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FriendsBottomSheetBinding.inflate(inflater, container, false)

        auth = FirebaseAuth.getInstance()

        storage = FirebaseStorage.getInstance().reference

        adapter = FriendsBottomSheetAdapter(lifecycleScope, auth.currentUser!!.uid)
        binding.friendsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.friendsRecyclerView.adapter = adapter

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setFriends()

        binding.apply {
//            lifecycleOwner = viewLifecycleOwner
//            viewModel = profileVM
//
//            binding.saving.visibility = View.GONE
//            binding.statusSave.visibility = View.GONE

//            lifecycleScope.launch {
//                val bitmap: Bitmap = withContext(Dispatchers.IO) {
//                    Coil.imageLoader(requireContext()).execute(
//                        ImageRequest.Builder(requireContext())
//                            .data(profileVM.avatar.value)
//                            .build()
//                    ).drawable?.toBitmap()!!
//                }
//                binding.avatar.setImageBitmap(bitmap)
//            }
//
//            avatar.setOnClickListener {
//                selectImageAvatar.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
//            }
//
//            binding.saveAllChanges.setOnClickListener {
//                allEditProfile.visibility = View.GONE
//                saving.visibility = View.VISIBLE
//
//                updateUserData { ans ->
//                    if(ans == "ok") {
//                        dismiss()
//                    } else {
//                        binding.saving.visibility = View.GONE
//                        binding.statusSave.visibility = View.GONE
//                        allEditProfile.visibility = View.VISIBLE
//                    }
//                }
//            }
        }
    }

    private fun setFriends() {
        val dbRef_your_friends = FirebaseDatabase.getInstance().getReference("users/${auth.currentUser!!.uid}/friends")
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

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        dialog.setOnShowListener {
            val bottomSheet = dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheet?.let {
                val behavior = BottomSheetBehavior.from(it)
                behavior.peekHeight = (resources.displayMetrics.heightPixels * 0.6).toInt()
                it.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
            }
        }
        return dialog
    }

}