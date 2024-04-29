package com.example.lipe.chats_and_groups.chat_fr

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.lipe.MapsFragment
import com.example.lipe.R
import com.example.lipe.chats_and_groups.Message
import com.example.lipe.chats_and_groups.all_chats.AllChatsFragment
import com.example.lipe.databinding.FragmentChatBinding
import com.example.lipe.viewModels.ChatVM
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class ChatFragment : Fragment() {

    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!
    private lateinit var chatAdapter: ChatAdapter
    private lateinit var db: DatabaseReference

    private val chatVM: ChatVM by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = FirebaseDatabase.getInstance().getReference("chats/300f91a7-9112-426a-ab01-25729c986e9f/messages")

        binding.recyclerViewChat.layoutManager = LinearLayoutManager(requireContext())
        chatAdapter = ChatAdapter(listOf())
        binding.recyclerViewChat.adapter = chatAdapter

        binding.sendBtn.setOnClickListener {
            val messageText = binding.messageInput.text.toString().trim()
            if(messageText.isNotEmpty()) {
                val currentUser = FirebaseAuth.getInstance().currentUser
                val message = Message(messageText, currentUser?.uid ?: "", System.currentTimeMillis())
                db.push().setValue(message)
                binding.messageInput.text?.clear()
            }
        }

        db.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val messages = mutableListOf<Message>()
                for (messageSnapshot in snapshot.children) {
                    val text = messageSnapshot.child("text").getValue(String::class.java)
                    val senderId = messageSnapshot.child("senderId").getValue(String::class.java)
                    val timestamp = messageSnapshot.child("time").getValue(Long::class.java)
                    val message = Message(text ?: "", senderId ?: "", timestamp ?: 0)
                    Log.d("INFOG", message.toString())
                    messages.add(message)
                }
                chatAdapter.messages = messages
                chatAdapter.notifyDataSetChanged()
                binding.recyclerViewChat.smoothScrollToPosition(messages.size)
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })


    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
