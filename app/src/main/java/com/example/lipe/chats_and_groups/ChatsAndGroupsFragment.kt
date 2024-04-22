package com.example.lipe.chats_and_groups

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.example.lipe.R
import com.example.lipe.databinding.FragmentChatsAndGroupsBinding
import com.google.android.material.tabs.TabLayout

class ChatsAndGroupsFragment : Fragment() {

    private var _binding: FragmentChatsAndGroupsBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter : ChatsAndGroupsTabAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentChatsAndGroupsBinding.inflate(inflater, container, false)
        adapter = ChatsAndGroupsTabAdapter(childFragmentManager, lifecycle)

        binding.tableLayout.addTab(binding.tableLayout.newTab().setText("Чаты"))
        binding.tableLayout.addTab(binding.tableLayout.newTab().setText("Группы"))

        binding.viewPager.adapter = adapter

        binding.tableLayout.addOnTabSelectedListener(object: TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                binding.viewPager.currentItem = tab!!.position
            }

            override fun onTabUnselected(p0: TabLayout.Tab?) {

            }

            override fun onTabReselected(p0: TabLayout.Tab?) {

            }

        })

        binding.viewPager.registerOnPageChangeCallback(object: ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                binding.tableLayout.selectTab(binding.tableLayout.getTabAt(position))
            }
        })

        binding.notificationChats.setOnClickListener {
            //view?.findNavController()?.navigate(R.id.action_chatsAndGroupsFragment_to_friendRequestsFragment)
        }
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}