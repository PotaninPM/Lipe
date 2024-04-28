package com.example.lipe.chats_and_groups.chats

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.lipe.R
import com.example.lipe.databinding.ChatItemBinding
import java.util.ArrayList

class ChatsAdapter : RecyclerView.Adapter<ChatsAdapter.ChatsHolder>(){

    val chatsList = ArrayList<ChatItem>()
    inner class ChatsHolder(item: View): RecyclerView.ViewHolder(item) {

        val binding = ChatItemBinding.bind(item)
        fun bind(chat: ChatItem) = with(binding) {
//            Picasso.get().load(chat.avatarUrl).into(avatar)
            name.text = chat.memberUid1
            lastMessage.text = chat.last_message
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