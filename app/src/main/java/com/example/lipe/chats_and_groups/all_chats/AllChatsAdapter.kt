package com.example.lipe.chats_and_groups.all_chats

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.lipe.R
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
import com.squareup.picasso.Picasso
import java.util.ArrayList

class AllChatsAdapter : RecyclerView.Adapter<AllChatsAdapter.ChatsHolder>(){

    val chatsList = ArrayList<ChatItem>()

    private lateinit var storageRef: StorageReference
    private lateinit var dbRef: DatabaseReference
    inner class ChatsHolder(item: View): RecyclerView.ViewHolder(item) {

        val binding = ChatItemBinding.bind(item)

        fun bind(chat: ChatItem) = with(binding) {
            all.visibility = View.GONE
            avatar.visibility = View.GONE

            storageRef = FirebaseStorage.getInstance().reference
            dbRef = FirebaseDatabase.getInstance().reference

            val firstNameRef = dbRef.child("users").child(chat.partnerUid).child("firstName")
            val lastNameRef = dbRef.child("users").child(chat.partnerUid).child("lastName")

            firstNameRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val firstName = dataSnapshot.value.toString()
                    lastNameRef.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            val lastName = dataSnapshot.value.toString()

                            name.text = buildString {
                                append(firstName)
                                append(" ")
                                append(lastName)
                            }

                            all.visibility = View.VISIBLE
                            avatar.visibility = View.VISIBLE
                        }

                        override fun onCancelled(databaseError: DatabaseError) {

                        }
                    })
                }

                override fun onCancelled(databaseError: DatabaseError) {

                }
            })

            val photoRef = storageRef.child("avatars/${chat.partnerUid}")
            val photoUrl = photoRef.downloadUrl

            photoUrl.addOnSuccessListener { url ->
                val imageUrl = url.toString()
                Picasso.get().load(imageUrl).into(binding.avatar)
            }.addOnFailureListener {
                Log.d("INFOG", "Ошибка чаты аватар")
            }

            val lastMessage_ = FirebaseDatabase.getInstance().reference.child("chats/${chat.uid}/last_message")
            lastMessage_.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    lastMessage.text = snapshot.value.toString()
                    Log.d("INFOG", chat.uid)
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })

            all.setOnClickListener {
                val context = it.context
                if (context is AppCompatActivity) {
                    val bottomNavigationView = context.findViewById<BottomNavigationView>(R.id.bottom_navigation)
                    bottomNavigationView.visibility = View.GONE
                    val fragment = ChatFragment()
                    val fragmentManager = context.supportFragmentManager
                    fragmentManager.beginTransaction()
                        .replace(R.id.all_chats_and_groups, fragment)
                        .addToBackStack(null)
                        .commit()
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatsHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.chat_item, parent, false)
        return ChatsHolder(view)
    }
    override fun onBindViewHolder(holder: ChatsHolder, position: Int) {
        holder.bind(chatsList[position])
    }
    override fun getItemCount(): Int {
        return chatsList.size
    }

    fun removeRequest(position: Int) {
        if (position in 0 until chatsList.size) {
            chatsList.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    fun updateRequests(chats: List<ChatItem>) {
        chatsList.clear()
        chatsList.addAll(chats)
        notifyDataSetChanged()
    }
}