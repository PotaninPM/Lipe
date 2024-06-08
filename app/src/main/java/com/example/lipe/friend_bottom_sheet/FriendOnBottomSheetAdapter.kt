package com.example.lipe.friend_bottom_sheet

import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.recyclerview.widget.RecyclerView
import coil.Coil
import coil.request.ImageRequest
import com.example.lipe.R
import com.example.lipe.all_profiles.friends.FriendItem
import com.example.lipe.all_profiles.friends.FriendsBottomSheet
import com.example.lipe.all_profiles.friends.FriendsBottomSheetAdapter
import com.example.lipe.chats_and_groups.chat_fr.ChatAdapter
import com.example.lipe.databinding.FriendItemWithRemoveBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.ArrayList

class FriendOnBottomSheetAdapter(val lifecycleScope: LifecycleCoroutineScope, myUid: String): RecyclerView.Adapter<FriendOnBottomSheetAdapter.FriendOnBottomSheetHolder>() {

    val friends = ArrayList<FriendItem>()

    private val myUid : String = myUid
    inner class FriendOnBottomSheetHolder(item: View): RecyclerView.ViewHolder(item) {

        val binding = FriendItemWithRemoveBinding.bind(item)
        fun bind(friend: FriendItem) = with(binding) {

            name.text = friend.name

            lifecycleScope.launch {
                val bitmap: Bitmap = withContext(Dispatchers.IO) {
                    Coil.imageLoader(itemView.context).execute(
                        ImageRequest.Builder(itemView.context)
                            .data(friend.avatarUrl)
                            .build()
                    ).drawable?.toBitmap()!!
                }
                avatar.setImageBitmap(bitmap)
            }



            binding.deleteFriend.setOnClickListener {
                val yourFriends = FirebaseDatabase.getInstance().getReference("users/${myUid}/friends/${friend.uid}")
                val friend_db = FirebaseDatabase.getInstance().getReference("users/${friend.uid}/friends/${myUid}")
                val amount = FirebaseDatabase.getInstance().getReference("users")
                yourFriends.removeValue().addOnSuccessListener {
                    friend_db.removeValue().addOnSuccessListener {
                        amount.child("${myUid}").child("friends_amount").addListenerForSingleValueEvent(object:
                            ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                amount.child("${myUid}").child("friends_amount").setValue(snapshot.value.toString().toInt() - 1)
                                amount.child(friend.uid).child("friends_amount").addListenerForSingleValueEvent(object:
                                    ValueEventListener {
                                    override fun onDataChange(snapshot2: DataSnapshot) {
                                        amount.child(friend.uid).child("friends_amount").setValue(snapshot2.value.toString().toInt() - 1)
                                        if (adapterPosition != RecyclerView.NO_POSITION) {
                                            removeRequest(adapterPosition)
                                        }
                                    }

                                    override fun onCancelled(error: DatabaseError) {
                                        TODO("Not yet implemented")
                                    }
                                })
                            }

                            override fun onCancelled(error: DatabaseError) {
                                TODO("Not yet implemented")
                            }

                        })
                    }
                }
            }
            binding.addToFriend.setOnClickListener {
                FirebaseDatabase.getInstance().getReference("users/${myUid}/friends/${friend.uid}")
            }
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendOnBottomSheetAdapter.FriendOnBottomSheetHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.friend_item_with_remove, parent, false)
        return FriendOnBottomSheetHolder(view)
    }

    override fun onBindViewHolder(holder: FriendOnBottomSheetAdapter.FriendOnBottomSheetHolder, position: Int) {
        holder.bind(friends[position])
    }
    override fun getItemCount(): Int {
        return friends.size
    }

    fun removeRequest(position: Int) {
        if (position in 0 until friends.size) {
            friends.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    fun updateFriends(friends_: List<FriendItem>) {
        friends.clear()
        friends.addAll(friends_)
        notifyDataSetChanged()
    }
}