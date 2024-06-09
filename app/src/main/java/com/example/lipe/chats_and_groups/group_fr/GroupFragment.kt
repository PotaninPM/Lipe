package com.example.lipe.chats_and_groups.group_fr

import android.content.res.Configuration
import android.graphics.Bitmap
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import coil.Coil
import coil.request.ImageRequest
import com.example.lipe.R
import com.example.lipe.chats_and_groups.ChatsAndGroupsFragment
import com.example.lipe.chats_and_groups.Message
import com.example.lipe.databinding.FragmentGroupBinding
import com.example.lipe.viewModels.GroupVM
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.vanniktech.emoji.EmojiManager
import com.vanniktech.emoji.EmojiPopup
import com.vanniktech.emoji.google.GoogleEmojiProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class GroupFragment(val groupUid: String) : Fragment() {

    private lateinit var binding: FragmentGroupBinding
    private lateinit var groupAdapter: GroupAdapter
    private lateinit var db: DatabaseReference

    private lateinit var auth: FirebaseAuth

    private val groupVM: GroupVM by activityViewModels()

    private lateinit var yourFirstName: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentGroupBinding.inflate(inflater, container, false)

        auth = FirebaseAuth.getInstance()

        val nightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
//        if (nightMode == Configuration.UI_MODE_NIGHT_YES) {
//            binding.allGroup.setBackgroundResource(R.drawable.fon_2)
//        } else {
//            binding.allGroup.setBackgroundResource(R.drawable.fon_light)
//        }

        FirebaseDatabase.getInstance().getReference("users/${auth.currentUser!!.uid}/firstAndLastName").addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var name = ""
                for(i in snapshot.value.toString()) {
                    if(i == ' ') {
                        break
                    } else {
                        name+=i
                    }
                }
                yourFirstName = name
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })

        binding.apply {
            lifecycleOwner = viewLifecycleOwner
            viewModel = groupVM

            fillGroupData()
        }

        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = FirebaseDatabase.getInstance().getReference("groups/${groupUid}/messages")

        binding.recyclerViewGroup.layoutManager = LinearLayoutManager(requireContext()).apply {
            reverseLayout = false
            stackFromEnd = true
        }

        EmojiManager.install(GoogleEmojiProvider())

        val emojiPopup = EmojiPopup.Builder.fromRootView(view).build(binding.messageInput)
        binding.emojiBtn.setOnClickListener {
            if (emojiPopup.isShowing) {
                emojiPopup.dismiss()
            } else {
                emojiPopup.toggle()
            }
        }

        groupAdapter = GroupAdapter(listOf(), auth.currentUser!!.uid, lifecycleScope)
        binding.recyclerViewGroup.adapter = groupAdapter

        val dbRef_chatLastMessage = FirebaseDatabase.getInstance().getReference("groups/${groupUid}")

        binding.sendBtn.setOnClickListener {
//            val secretKey = DeCryptMessages.generateKey()
            val messageText = binding.messageInput.text.toString().trim()
            if (messageText.isNotEmpty()) {
                val currentUser = FirebaseAuth.getInstance().currentUser
                val message = Message(
                    messageText,
                    currentUser?.uid ?: "",
                    System.currentTimeMillis()
                )

                val lastMessage = hashMapOf("name" to yourFirstName, "lastMessage" to messageText)
                dbRef_chatLastMessage.child("last_message").setValue(lastMessage).addOnSuccessListener {

                }

                db.push().setValue(message)
                binding.messageInput.text?.clear()
            }

        }

        binding.backBtn.setOnClickListener {
            val fragment = ChatsAndGroupsFragment()
            val fragmentManager = childFragmentManager
            fragmentManager.beginTransaction()
                .replace(R.id.all_group, fragment)
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
                groupAdapter.messages = messages
                groupAdapter.notifyDataSetChanged()
                binding.recyclerViewGroup.smoothScrollToPosition(messages.size)
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })


    }

    override fun onPause() {
        super.onPause()
        val bottomNav =
            (requireActivity() as AppCompatActivity).findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNav.visibility = View.VISIBLE
    }

    override fun onResume() {
        super.onResume()
        val bottomNav =
            (requireActivity() as AppCompatActivity).findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNav.visibility = View.GONE
    }

    private fun fillGroupData() {
        val myUid = auth.currentUser!!.uid
        val dbRef_find_group =
            FirebaseDatabase.getInstance().getReference("groups/${groupUid}")

        dbRef_find_group.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                if(isAdded) {
                    groupVM.setInfo(
                        snapshot.child("title").value.toString(),
                        groupUid,
                        snapshot.child("members").childrenCount.toString(),
                        snapshot.child("key").value.toString()
                    )
                    lifecycleScope.launch {
                        val bitmap: Bitmap = withContext(Dispatchers.IO) {
                            Coil.imageLoader(requireContext()).execute(
                                ImageRequest.Builder(requireContext())
                                    .data(snapshot.child("imageUid").value)
                                    .build()
                            ).drawable?.toBitmap()!!
                        }
                        binding.avatarChat.setImageBitmap(bitmap)
                    }
                }

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