package com.example.lipe.chats_and_groups.chat_fr

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.lipe.CryptAlgo
import com.example.lipe.R
import com.example.lipe.chats_and_groups.Message
import com.example.lipe.viewModels.AppVM
import com.google.firebase.auth.FirebaseAuth

class ChatAdapter(var messages: List<Message>, var myUserId: String) :
    RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {
    inner class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val messageTextView: TextView = itemView.findViewById(R.id.sender_message_text)

        fun bind(message: Message) {
            messageTextView.text = CryptAlgo.encrypt(message.text)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(viewType, parent, false)
        return ChatViewHolder(view)
    }
    override fun getItemViewType(position: Int): Int {
        val message = messages[position]
        return if (message.senderId == myUserId) {
            R.layout.message_item_send
        } else {
            R.layout.message_item_receive
        }
    }
    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val message = messages[position]
        holder.bind(message)
    }

    override fun getItemCount(): Int {
        return messages.size
    }
}
