package com.example.lipe.all_profiles.friends

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
import com.example.lipe.all_profiles.EventItem
import com.example.lipe.all_profiles.cur_events.YourEventsAdapter
import com.example.lipe.databinding.EventItemBinding
import com.example.lipe.databinding.FriendItemWithRemoveBinding
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.app
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.ArrayList

class FriendsBottomSheetAdapter(val lifecycleScope: LifecycleCoroutineScope, myUid: String): RecyclerView.Adapter<FriendsBottomSheetAdapter.FriendsBottomSheetHolder>() {

    val friends = ArrayList<FriendItem>()

    private val myUid = myUid
    inner class FriendsBottomSheetHolder(item: View): RecyclerView.ViewHolder(item) {

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
                val friend = FirebaseDatabase.getInstance().getReference("users/${friend.uid}/friends/${myUid}")
                yourFriends.removeValue().addOnSuccessListener {
                    friend.removeValue().addOnSuccessListener {
                        if (adapterPosition != RecyclerView.NO_POSITION) {
                            removeRequest(adapterPosition)
                        }
                    }
                }
            }
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendsBottomSheetAdapter.FriendsBottomSheetHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.friend_item_with_remove, parent, false)
        return FriendsBottomSheetHolder(view)
    }
    override fun onBindViewHolder(holder: FriendsBottomSheetAdapter.FriendsBottomSheetHolder, position: Int) {
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
