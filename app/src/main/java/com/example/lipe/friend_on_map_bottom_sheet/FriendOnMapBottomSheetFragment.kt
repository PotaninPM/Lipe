package com.example.lipe.friend_on_map_bottom_sheet

import android.graphics.Bitmap
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import coil.Coil
import coil.load
import coil.request.ImageRequest
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FriendOnMapBottomSheetFragment : DialogFragment() {

    private lateinit var binding: FragmentFriendOnMapBottomSheetBinding
    private lateinit var storage: StorageReference

    private val appVM: AppVM by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentFriendOnMapBottomSheetBinding.inflate(inflater, container, false)
        storage = FirebaseStorage.getInstance().reference

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setData()

        binding.closeFriend.setOnClickListener {
            dismiss()
        }
    }

    private fun setData() {
        val dbRef_user = FirebaseDatabase.getInstance().getReference("users/${appVM.type}")
        dbRef_user.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                binding.username.setText(snapshot.child("username").value.toString())
                binding.name.setText(snapshot.child("firstAndLastName").value.toString())
                binding.battery.setText(snapshot.child("batteryLevel").value.toString() + "%")

                lifecycleScope.launch {
                    val bitmap: Bitmap = withContext(Dispatchers.IO) {
                        Coil.imageLoader(requireContext()).execute(
                            ImageRequest.Builder(requireContext())
                                .data(snapshot.child("imageUrl").value.toString())
                                .build()
                        ).drawable?.toBitmap()!!
                    }
                    binding.avatar.setImageBitmap(bitmap)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.95).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog?.window?.attributes?.gravity = Gravity.TOP

        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
    }
}
