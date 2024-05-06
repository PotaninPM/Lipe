package com.example.lipe.chats_and_groups.all_groups

import android.graphics.Bitmap
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.recyclerview.widget.RecyclerView
import coil.Coil
import coil.request.ImageRequest
import com.example.lipe.R
import com.example.lipe.chats_and_groups.all_chats.ChatItem
import com.example.lipe.chats_and_groups.chat_fr.ChatFragment
import com.example.lipe.databinding.ChatItemBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.ArrayList

class AllGroupsAdapter(lifecycleScope: LifecycleCoroutineScope) : RecyclerView.Adapter<AllGroupsAdapter.GroupsHolder>(){

    val groupsList = ArrayList<GroupItem>()

    private lateinit var storageRef: StorageReference
    private lateinit var dbRef: DatabaseReference

    private val lifecycleScope: LifecycleCoroutineScope = lifecycleScope
    inner class GroupsHolder(item: View): RecyclerView.ViewHolder(item) {

        val binding = ChatItemBinding.bind(item)

        fun bind(group: GroupItem) = with(binding) {
            all.visibility = View.GONE
            avatar.visibility = View.GONE

            storageRef = FirebaseStorage.getInstance().reference
            dbRef = FirebaseDatabase.getInstance().reference

            val lastNameRef = dbRef.child("groups/${group.uid}/last_message")
            lastNameRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    var last_message = dataSnapshot.value
                    if(last_message != null) {
                        lastMessage.text = last_message.toString()
                    } else {
                        lastMessage.visibility = View.GONE
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {

                }
            })
            name.text = group.title

            val photoRef = storageRef.child("event_images/${group.imageUid}")
            val photoUrl = photoRef.downloadUrl

            photoUrl.addOnSuccessListener { url ->
                lifecycleScope.launch {
                    val bitmap: Bitmap = withContext(Dispatchers.IO) {
                        Coil.imageLoader(binding.avatar.context).execute(
                            ImageRequest.Builder(binding.avatar.context)
                                .data(url)
                                .build()
                        ).drawable?.toBitmap()!!
                    }
                    binding.avatar.setImageBitmap(bitmap)
                }
                all.visibility = View.VISIBLE
                avatar.visibility = View.VISIBLE
            }

            all.setOnClickListener {
                val context = it.context
                if (context is AppCompatActivity) {
                    val bottomNavigationView = context.findViewById<BottomNavigationView>(R.id.bottom_navigation)
                    bottomNavigationView.visibility = View.GONE
                    val fragment = ChatFragment("-")
                    val fragmentManager = context.supportFragmentManager
                    fragmentManager.beginTransaction()
                        .replace(R.id.all_chats_and_groups, fragment)
                        .addToBackStack(null)
                        .commit()
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupsHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.chat_item, parent, false)
        return GroupsHolder(view)
    }
    override fun onBindViewHolder(holder: GroupsHolder, position: Int) {
        holder.bind(groupsList[position])
    }
    override fun getItemCount(): Int {
        return groupsList.size
    }

    fun removeRequest(position: Int) {
        if (position in 0 until groupsList.size) {
            groupsList.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    fun updateRequests(groups: List<GroupItem>) {
        groupsList.clear()
        groupsList.addAll(groups)
        notifyDataSetChanged()
    }
}