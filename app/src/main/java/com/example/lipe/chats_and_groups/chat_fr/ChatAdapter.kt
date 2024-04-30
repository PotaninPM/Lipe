package com.example.lipe.chats_and_groups.chat_fr

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.lipe.R
import com.example.lipe.chats_and_groups.Message

class ChatAdapter(var messages: List<Message>, private val myUserId: String) :
    RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

    inner class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val messageTextView: TextView = itemView.findViewById(R.id.sender_message_text)

        fun bind(message: Message) {
            messageTextView.text = message.text

            val layoutResId =
                R.layout.message_item_send
            val layoutInflater = LayoutInflater.from(itemView.context)
            layoutInflater.inflate(layoutResId, null, false)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.message_item_send, parent, false)
        return ChatViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val message = messages[position]
        holder.bind(message)
    }

    override fun getItemCount(): Int {
        return messages.size
    }
}
