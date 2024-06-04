package com.example.lipe.chats_and_groups.chat_fr

import android.content.res.Configuration
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import coil.Coil
import coil.request.ImageRequest
import com.example.lipe.DeCryptMessages
import com.example.lipe.R
import com.example.lipe.chats_and_groups.ChatsAndGroupsFragment
import com.example.lipe.chats_and_groups.Message
import com.example.lipe.databinding.FragmentChatBinding
import com.example.lipe.viewModels.ChatVM
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.vanniktech.emoji.EmojiManager
import com.vanniktech.emoji.EmojiPopup
import com.vanniktech.emoji.google.GoogleEmojiProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ChatFragment(val chatUid: String) : Fragment() {

    private lateinit var binding: FragmentChatBinding
    private lateinit var chatAdapter: ChatAdapter
    private lateinit var db: DatabaseReference

    private lateinit var auth: FirebaseAuth

    private val chatVM: ChatVM by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentChatBinding.inflate(inflater, container, false)

        auth = FirebaseAuth.getInstance()

        val nightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
//        if (nightMode == Configuration.UI_MODE_NIGHT_YES) {
//            binding.allChat.setBackgroundResource(R.drawable.fon_2)
//        } else {
//            binding.allChat.setBackgroundResource(R.drawable.fon_light)
//        }

        binding.apply {
            lifecycleOwner = viewLifecycleOwner
            viewModel = chatVM

            val bottomNav =
                (requireActivity() as AppCompatActivity).findViewById<BottomNavigationView>(R.id.bottom_navigation)
            bottomNav.visibility = View.GONE

            fillOponentData()
        }

        val view = binding.root
        return view
    }

//    override fun onPause() {
//        super.onPause()
//        val bottomNav =
//            (requireActivity() as AppCompatActivity).findViewById<BottomNavigationView>(R.id.bottom_navigation)
//        bottomNav.visibility = View.VISIBLE
//    }

    override fun onResume() {
        super.onResume()
        val bottomNav =
            (requireActivity() as AppCompatActivity).findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNav.visibility = View.INVISIBLE
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = FirebaseDatabase.getInstance().getReference("chats/${chatUid}/messages")

        EmojiManager.install(GoogleEmojiProvider())

        val emojiPopup = EmojiPopup.Builder.fromRootView(view).build(binding.messageInput)
        binding.emojiBtn.setOnClickListener {
            if (emojiPopup.isShowing) {
                emojiPopup.dismiss()
                binding.emojiBtn.setImageResource(R.drawable.emoji_btn)
            } else {
                emojiPopup.toggle()
                binding.emojiBtn.setImageResource(R.drawable.keyboard_btn)
            }
        }

        binding.recyclerViewChat.layoutManager = LinearLayoutManager(requireContext()).apply {
            reverseLayout = false
            stackFromEnd = true
        }

        chatAdapter = ChatAdapter(listOf(), auth.currentUser!!.uid)
        binding.recyclerViewChat.adapter = chatAdapter

        val dbRef_chatLastMessage = FirebaseDatabase.getInstance().getReference("chats/${chatUid}/last_message")
        binding.sendBtn.setOnClickListener {
            val messageText = binding.messageInput.text.toString().trim()
            if (messageText.isNotEmpty()) {
                val currentUser = FirebaseAuth.getInstance().currentUser
                val secretKey = DeCryptMessages.generateKey()
                val message = Message(
                    DeCryptMessages.encrypt(messageText, secretKey),
                    currentUser?.uid ?: "",
                    System.currentTimeMillis()
                )

                dbRef_chatLastMessage.setValue(messageText).addOnSuccessListener {

                }

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
        }

        db.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val messages = mutableListOf<Message>()
                for (messageSnapshot in snapshot.children) {
                    val text = messageSnapshot.child("text").getValue(String::class.java)
                    val senderId = messageSnapshot.child("senderId").getValue(String::class.java)
                    val timestamp =
                        messageSnapshot.child("time").getValue(Long::class.java)
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
        val dbRef_find_oponent =
            FirebaseDatabase.getInstance().getReference("chats/${chatUid}")

        dbRef_find_oponent.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user1_uid = snapshot.child("user1_uid").value.toString()
                val user2_uid = snapshot.child("user2_uid").value.toString()

                val opponentUid = if (myUid == user1_uid) user2_uid else user1_uid

                val dbRef_oponent =
                    FirebaseDatabase.getInstance().getReference("users/${opponentUid}")

                val storage_oponent = FirebaseStorage.getInstance().getReference("avatars/${opponentUid}")
                storage_oponent.downloadUrl.addOnSuccessListener {url ->
                    lifecycleScope.launch {
                        val bitmap: Bitmap = withContext(Dispatchers.IO) {
                            Coil.imageLoader(requireContext()).execute(
                                ImageRequest.Builder(requireContext())
                                    .data(url)
                                    .build()
                            ).drawable?.toBitmap()!!
                        }
                        binding.avatarChat.setImageBitmap(bitmap)
                    }
                }

                dbRef_oponent.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val name = snapshot.child("firstAndLastName").value.toString()
                        val status = snapshot.child("status").value.toString()
                        val key = snapshot.child("key").value.toString()
                        chatVM.setInfo("$name", status, chatUid, key)
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.d("INFOG", "error")
                    }

                })

            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

    override fun onDestroyView() {
        super.onDestroyView()

        val bottomNav =
            (requireActivity() as AppCompatActivity).findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNav.visibility = View.VISIBLE
    }
}
