package com.example.lipe.chats_and_groups.group_fr

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.recyclerview.widget.RecyclerView
import coil.ImageLoader
import coil.request.ImageRequest
import com.example.lipe.CryptAlgo
import com.example.lipe.R
import com.example.lipe.chats_and_groups.Message
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class GroupAdapter(var messages: List<Message>, var myUserId: String, var lifeScope: LifecycleCoroutineScope) :
    RecyclerView.Adapter<GroupAdapter.GroupViewHolder>() {
    inner class GroupViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(message: Message) {

            if(message.senderId == myUserId) {
                val messageTextView: TextView = itemView.findViewById(R.id.sender_message_text)
                messageTextView.text = CryptAlgo.encrypt(message.text)
            } else {
                val messageTextViewGroup: TextView = itemView.findViewById(R.id.sender_message_text_group)
                val name_other: TextView = itemView.findViewById(R.id.sender_group)
                val other_avatar: ImageView = itemView.findViewById(R.id.avatar_group)

                FirebaseDatabase.getInstance().getReference("users/${message.senderId}/firstName").addValueEventListener(object: ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        name_other.text = snapshot.value.toString()

                        FirebaseStorage.getInstance().getReference("avatars/${message.senderId}").downloadUrl.addOnSuccessListener {url ->
                            lifeScope.launch {
                                val bitmap = withContext(Dispatchers.IO) {
                                    ImageLoader(itemView.context).execute(
                                        ImageRequest.Builder(itemView.context)
                                            .data(url)
                                            .build()
                                    ).drawable?.toBitmap()
                                }

                                other_avatar.setImageBitmap(bitmap)
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }

                })
                messageTextViewGroup.text = CryptAlgo.encrypt(message.text)
                name_other.text = "Миша Потанин"
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(viewType, parent, false)
        return GroupViewHolder(view)
    }
    override fun getItemViewType(position: Int): Int {
        val message = messages[position]
        return if (message.senderId == myUserId) {
            R.layout.message_item_send
        } else {
            R.layout.message_receive_group
        }
    }
    override fun onBindViewHolder(holder: GroupViewHolder, position: Int) {
        val message = messages[position]
        holder.bind(message)
    }

    override fun getItemCount(): Int {
        return messages.size
    }
}