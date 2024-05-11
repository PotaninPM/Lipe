package com.example.lipe.chats_and_groups.group_fr

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.lipe.CryptAlgo
import com.example.lipe.R
import com.example.lipe.chats_and_groups.Message

class GroupAdapter(var messages: List<Message>, var myUserId: String) :
    RecyclerView.Adapter<GroupAdapter.GroupViewHolder>() {
    inner class GroupViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val messageTextView: TextView = itemView.findViewById(R.id.sender_message_text)

        fun bind(message: Message) {
            messageTextView.text = CryptAlgo.encrypt(message.text)
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
            R.layout.message_item_receive
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