package com.example.lipe.chats_and_groups

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.lipe.chats_and_groups.chats.ChatsFragment
import com.example.lipe.chats_and_groups.groups.GroupsFragment

class ChatsAndGroupsTabAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle) : FragmentStateAdapter(fragmentManager, lifecycle) {
    override fun getItemCount(): Int {
        return 2
    }

    override fun createFragment(position: Int): Fragment {
        return if(position == 0) {
            ChatsFragment()
        } else {
            GroupsFragment()
        }
    }
}