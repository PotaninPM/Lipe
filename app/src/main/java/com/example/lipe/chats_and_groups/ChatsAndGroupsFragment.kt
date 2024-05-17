package com.example.lipe.chats_and_groups

import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.viewpager2.widget.ViewPager2
import coil.Coil
import coil.request.ImageRequest
import com.example.lipe.R
import com.example.lipe.databinding.FragmentChatsAndGroupsBinding
import com.example.lipe.friend_requests.FriendRequestsFragment
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ChatsAndGroupsFragment : Fragment() {

    private var _binding: FragmentChatsAndGroupsBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: ChatsAndGroupsTabAdapter

    private lateinit var auth: FirebaseAuth

    private lateinit var storage: StorageReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentChatsAndGroupsBinding.inflate(inflater, container, false)
        val view = binding.root

        adapter = ChatsAndGroupsTabAdapter(childFragmentManager, lifecycle)

        auth = FirebaseAuth.getInstance()
        storage = FirebaseStorage.getInstance().reference.child("avatars/${auth.currentUser!!.uid}")

        view.findViewById<TabLayout>(R.id.tableLayout).apply {
            addTab(newTab().setText("Чаты"))
            addTab(newTab().setText("Группы"))
            addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab?) {
                    binding.viewPager.currentItem = tab!!.position
                }

                override fun onTabUnselected(p0: TabLayout.Tab?) {}

                override fun onTabReselected(p0: TabLayout.Tab?) {}
            })
        }

        binding.viewPager.adapter = adapter

        val dbRef_user = FirebaseDatabase.getInstance().getReference("users/${auth.currentUser!!.uid}")
        dbRef_user.child("query_friends").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists() || snapshot.childrenCount.toInt() == 0) {
                    binding.indexNotif.visibility = View.GONE
                } else {
                    binding.indexNotif.visibility = View.VISIBLE
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })

        storage.downloadUrl.addOnSuccessListener { url ->
            lifecycleScope.launch {
                val bitmap: Bitmap = withContext(Dispatchers.IO) {
                    Coil.imageLoader(requireContext()).execute(
                        ImageRequest.Builder(requireContext())
                            .data(url)
                            .build()
                    ).drawable?.toBitmap()!!
                }
                binding.avatarChatGroup.setImageBitmap(bitmap)
            }
        }

        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                binding.tableLayout.selectTab(binding.tableLayout.getTabAt(position))
            }
        })

        binding.notificationChats.setOnClickListener {
            replaceFragment(FriendRequestsFragment())
        }

        return view
    }

    private fun replaceFragment(fragment: Fragment) {
        val fragmentManager = childFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.all_chats_and_groups, fragment)
        fragmentTransaction.addToBackStack(null)
        fragmentTransaction.commit()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}
