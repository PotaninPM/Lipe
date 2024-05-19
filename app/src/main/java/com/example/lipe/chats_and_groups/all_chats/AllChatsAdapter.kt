package com.example.lipe.chats_and_groups.all_chats

import android.graphics.Bitmap
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import coil.Coil
import coil.request.ImageRequest
import com.example.lipe.R
import com.example.lipe.chats_and_groups.chat_fr.ChatFragment
import com.example.lipe.databinding.ChatItemBinding
import com.example.lipe.viewModels.AppVM
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

class AllChatsAdapter(lifecycleScope: LifecycleCoroutineScope) : RecyclerView.Adapter<AllChatsAdapter.ChatsHolder>(){

    val chatsList = ArrayList<ChatItem>()

    private lateinit var storageRef: StorageReference
    private lateinit var dbRef: DatabaseReference

    private val lifecycleScope: LifecycleCoroutineScope = lifecycleScope
    inner class ChatsHolder(item: View): RecyclerView.ViewHolder(item) {


        val binding = ChatItemBinding.bind(item)

        fun bind(chat: ChatItem) = with(binding) {

            all.visibility = View.GONE
            avatar.visibility = View.GONE

            storageRef = FirebaseStorage.getInstance().reference
            dbRef = FirebaseDatabase.getInstance().reference

            val nameRef = dbRef.child("users").child(chat.partnerUid).child("firstAndLastName")
            val status = dbRef.child("users").child(chat.partnerUid).child("status")

            val lastMessage_db = dbRef.child("chats").child(chat.uid).child("last_message")

            status.addValueEventListener(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    var status_user = snapshot.value.toString()
                    if(status_user == "online") {
                        binding.statusViewGroup.visibility = View.VISIBLE
                    } else {
                        binding.statusViewGroup.visibility = View.GONE
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })

            nameRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                      name.text = dataSnapshot.value.toString()

                      lastMessage_db.addValueEventListener(object: ValueEventListener {
                          override fun onDataChange(snapshot: DataSnapshot) {
                              lastMessage.text = snapshot.value.toString()
                          }

                          override fun onCancelled(error: DatabaseError) {
                              TODO("Not yet implemented")
                          }

                      })

                      all.visibility = View.VISIBLE
                      avatar.visibility = View.VISIBLE
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })

            val photoRef = storageRef.child("avatars/${chat.partnerUid}")
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
                    val fragment = ChatFragment(chat.uid)
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