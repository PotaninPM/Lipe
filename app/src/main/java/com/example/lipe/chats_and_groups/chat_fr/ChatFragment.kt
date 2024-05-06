package com.example.lipe.chats_and_groups.chat_fr

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.lipe.CryptAlgo
import com.example.lipe.MapsFragment
import com.example.lipe.R
import com.example.lipe.chats_and_groups.ChatsAndGroupsFragment
import com.example.lipe.chats_and_groups.Message
import com.example.lipe.chats_and_groups.all_chats.AllChatsFragment
import com.example.lipe.databinding.FragmentChatBinding
import com.example.lipe.viewModels.ChatVM
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class ChatFragment(val chatUid: String) : Fragment() {

    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!
    private lateinit var chatAdapter: ChatAdapter
    private lateinit var db: DatabaseReference

    private lateinit var auth: FirebaseAuth

    private val chatVM: ChatVM by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatBinding.inflate(inflater, container, false)

        auth = FirebaseAuth.getInstance()

        fillOponentData()

        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.d("INFOG", chatUid)
        db = FirebaseDatabase.getInstance().getReference("chats/${chatUid}/messages")

        binding.recyclerViewChat.layoutManager = LinearLayoutManager(requireContext()).apply {
            reverseLayout = false
            stackFromEnd = true
        }

        chatAdapter = ChatAdapter(listOf(), auth.currentUser!!.uid)
        binding.recyclerViewChat.adapter = chatAdapter

        binding.sendBtn.setOnClickListener {
            val messageText = binding.messageInput.text.toString().trim()
            if(messageText.isNotEmpty()) {
                val currentUser = FirebaseAuth.getInstance().currentUser
                val message = Message(CryptAlgo.crypt(messageText), currentUser?.uid ?: "", System.currentTimeMillis())
                db.push().setValue(message)
                binding.messageInput.text?.clear()
            }

        }

        binding.backBtn.setOnClickListener {
            val fragment = ChatsAndGroupsFragment()
            val fragmentManager = childFragmentManager
            fragmentManager.beginTransaction()
                .replace(R.id.all_chat, fragment)
                .addToBackStack(null)
                .commit()

            val bottomNav = (requireActivity() as AppCompatActivity).findViewById<BottomNavigationView>(R.id.bottom_navigation)
            bottomNav.visibility = View.VISIBLE
        }

        db.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val messages = mutableListOf<Message>()
                for (messageSnapshot in snapshot.children) {
                    val text = messageSnapshot.child("text").getValue(String::class.java)
                    val senderId = messageSnapshot.child("senderId").getValue(String::class.java)
                    val timestamp = messageSnapshot.child("time").getValue(Long::class.java)
                    val message = Message(text ?: "", senderId ?: "", timestamp ?: 0)
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

    private fun fillOponentData() {
        val myUid = auth.currentUser!!.uid
        val dbRef_find_oponent = FirebaseDatabase.getInstance().getReference("chats/${chatUid}")

        dbRef_find_oponent.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.child("user1_uid").value != myUid) {
                    val dbRef_oponent = FirebaseDatabase.getInstance().getReference("users/${snapshot.value}")
                    dbRef_oponent.addValueEventListener(object: ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val firstName = snapshot.child("firstName").value.toString()
                            val lastName = snapshot.child("lastName").value.toString()
                            val status = snapshot.child("status").value.toString()

                            chatVM.setInfo(firstName + lastName, "online", chatUid)
                        }

                        override fun onCancelled(error: DatabaseError) {
                            TODO("Not yet implemented")
                        }

                    })
                } else {
                    val dbRef_oponent = FirebaseDatabase.getInstance().getReference("users/${snapshot.child("user2_uid").value}")
                    dbRef_oponent.addValueEventListener(object: ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val firstName = snapshot.child("firstName").value.toString()
                            val lastName = snapshot.child("lastName").value.toString()
                            val status = snapshot.child("status").value.toString()

                            chatVM.setInfo(firstName + lastName, "online", chatUid)
                        }

                        override fun onCancelled(error: DatabaseError) {
                            TODO("Not yet implemented")
                        }

                    })
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

    override fun onDestroyView() {
        super.onDestroyView()

        val bottomNav = (requireActivity() as AppCompatActivity).findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNav.visibility = View.VISIBLE

        _binding = null
    }
}
